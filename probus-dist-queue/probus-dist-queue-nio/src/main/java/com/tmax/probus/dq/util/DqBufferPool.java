/*
 * DqBufferPool.java Version 1.0 Jan 27, 2012
 * *
 * Copyright (c) 2010 by Tmax Soft co., Ltd.
 * All rights reserved.
 * *
 * This software is the confidential and proprietary information of
 * Tmax Soft co.,Ltd("Confidential Information").
 * You shall not disclose such Confidential Information
 * and shall use it only in accordance with the terms of the license agreement
 * entered into with Tmax Soft co., Ltd.
 */
package com.tmax.probus.dq.util;


import static java.util.logging.Level.*;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel.MapMode;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;


/**
 *
 */
public final class DqBufferPool {
    /**
     * Logger for this class
     */
    private final transient Logger logger = Logger.getLogger("com.tmax.probus.dq.nio");
    private static final String TMP_FILE_PREFIX = "probus_buffer";
    private static final String TMP_FILE_SUFFIX = ".buff";
    private final Map<Integer, List<ByteBuffer>> pool_ = new ConcurrentHashMap<Integer, List<ByteBuffer>>();
    private final Lock dummyLock_ = new ReentrantLock();
    private final Lock poolLock_ = new ReentrantLock();
    private final int UNIT_SIZE;
    private final ByteBuffer fileDummy_;
    private final ByteBuffer directDummy_;

    private DqBufferPool(final File file, final int maxSize, final int blockSize)
            throws IOException {
        RandomAccessFile f = null;
        try {
            f = new RandomAccessFile(file, "rw");
            fileDummy_ = f.getChannel().map(MapMode.READ_WRITE, 0, maxSize / 2);
        } finally {
            if (f != null) f.close();
        }
        directDummy_ = ByteBuffer.allocateDirect(maxSize - maxSize / 2);
        UNIT_SIZE = blockSize;
    }

    private DqBufferPool(final int maxSize, final int blockSize) throws IOException {
        this(File.createTempFile(TMP_FILE_PREFIX, TMP_FILE_SUFFIX), maxSize, blockSize);
    }

    private DqBufferPool(final String path, final int maxSize, final int blockSize)
            throws IOException {
        this(File.createTempFile(TMP_FILE_PREFIX, TMP_FILE_SUFFIX, new File(path)), maxSize, blockSize);
    }

    public static DqBufferPool newPool(final int maxSize, final int blockSize)
            throws IOException {
        return new DqBufferPool(maxSize, blockSize);
    }

    public static DqBufferPool newPool(final String path, final int maxSize, final int blockSize)
            throws IOException {
        return new DqBufferPool(path, maxSize, blockSize);
    }

    public final void dispose() {
        pool_.clear();
    }

    public final IDqByteBuffer getBuffer() {
        return new DqByteBuffer();
    }

    /**
     * @param idx
     * @return
     */
    private final ByteBuffer createDirectBuffer(final int idx) {
        final Lock lock = dummyLock_;
        lock.lock();
        try {
            if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "createDirectBuffer");
            final int size = idx * UNIT_SIZE;
            final int limit = directDummy_.position() + size;
            if (limit > directDummy_.capacity()) return null;
            directDummy_.limit(limit);
            final ByteBuffer slice = directDummy_.slice();
            directDummy_.position(limit);
            return slice;
        } finally {
            lock.unlock();
        }
    }

    /**
     * @param idx
     * @return
     */
    private final ByteBuffer createFileBuffer(final int idx) {
        final Lock lock = dummyLock_;
        lock.lock();
        try {
            if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "createFileBuffer");
            final int limit = fileDummy_.position() + idx * UNIT_SIZE;
            if (limit > fileDummy_.capacity() || limit < 0) return null;
            fileDummy_.limit(limit);
            final ByteBuffer slice = fileDummy_.slice();
            fileDummy_.position(limit);
            return slice;
        } finally {
            lock.unlock();
        }
    }

    /**
     * @param idx
     * @return
     */
    private ByteBuffer createHeapBuffer(final int idx) {
        if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "createHeapBuffer");
        return ByteBuffer.allocate(idx);
    }

    private final void free(final ByteBuffer buffer) {
        if (!buffer.isDirect()) return;
        final List<ByteBuffer> list = getRepo(buffer.capacity() / UNIT_SIZE);
        synchronized (list) {
            list.add(buffer);
        }
    }

    /**
     * @param length
     */
    private final ByteBuffer get(final int length) {
        final int idx = length % UNIT_SIZE > 0 ? length / UNIT_SIZE + 1 : length / UNIT_SIZE;
        final List<ByteBuffer> list = getRepo(idx);
        ByteBuffer buffer;
        synchronized (list) {
            if (list.isEmpty()) {
                if ((buffer = createDirectBuffer(idx)) == null)
                    if ((buffer = createFileBuffer(idx)) == null)
                        buffer = createHeapBuffer(idx);
            } else {
                buffer = list.remove(0);
            }
        }
        buffer.clear();
        buffer.limit(length);
        return buffer;
    }

    /**
     * @param idx
     * @return
     */
    private List<ByteBuffer> getRepo(final int idx) {
        List<ByteBuffer> list;
        final Lock lock = poolLock_;
        lock.lock();
        try {
            list = pool_.get(idx);
            if (list == null) {
                list = new LinkedList<ByteBuffer>();
                pool_.put(idx, list);
            }
        } finally {
            lock.unlock();
        }
        return list;
    }

    private class DqByteBuffer implements IDqByteBuffer {
        private ByteBuffer buffer_;

        /**
         * @return the buffer
         */
        @Override public final ByteBuffer getByteBuffer() {
            return buffer_;
        }

        @Override public final void init(final int size) {
            if (buffer_ != null) free(buffer_);
            buffer_ = get(size);
        }

        @Override public final void release() {
            if (buffer_ != null) free(buffer_);
        }
    }
}
