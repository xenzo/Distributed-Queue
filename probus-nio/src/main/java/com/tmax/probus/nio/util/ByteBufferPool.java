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
package com.tmax.probus.nio.util;


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

import com.tmax.probus.nio.api.IMessageWrapper;


/**
 * The Class ByteBufferPool.
 */
public final class ByteBufferPool {
    /** Logger for this class. */
    private final transient Logger logger = Logger.getLogger("com.tmax.probus.nio.util");
    /** The Constant TMP_FILE_PREFIX. */
    private static final String TMP_FILE_PREFIX = "probus_buffer";
    /** The Constant TMP_FILE_SUFFIX. */
    private static final String TMP_FILE_SUFFIX = ".buff";
    /** The pool_. */
    private final Map<Integer, List<ByteBuffer>> pool_ = new ConcurrentHashMap<Integer, List<ByteBuffer>>();
    /** The dummy lock_. */
    private final Lock dummyLock_ = new ReentrantLock();
    /** The pool lock_. */
    private final Lock poolLock_ = new ReentrantLock();
    /** The UNI t_ size. */
    private final int UNIT_SIZE;
    /** The file dummy_. */
    private final ByteBuffer fileDummy_;
    /** The direct dummy_. */
    private final ByteBuffer directDummy_;

    /**
     * Instantiates a new byte buffer pool.
     * @param path the path
     * @param maxSize the max size
     * @param blockSize the block size
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private ByteBufferPool(final String path, final int maxSize, final int blockSize)
            throws IOException {
        File file = null;
        if (path == null) {
            file = File.createTempFile(TMP_FILE_PREFIX, TMP_FILE_SUFFIX);
        } else {
            final File folder = new File(path);
            if (!folder.isDirectory()) throw new IllegalArgumentException();
            if (!folder.exists()) folder.mkdir();
            file = File.createTempFile(TMP_FILE_PREFIX, TMP_FILE_SUFFIX, folder);
        }
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

    /**
     * New pool.
     * @param maxSize the max size
     * @param blockSize the block size
     * @return the byte buffer pool
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static ByteBufferPool newPool(final int maxSize, final int blockSize)
            throws IOException {
        return new ByteBufferPool(null, maxSize, blockSize);
    }

    /**
     * New pool.
     * @param path the path
     * @param maxSize the max size
     * @param blockSize the block size
     * @return the byte buffer pool
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static ByteBufferPool newPool(final String path, final int maxSize, final int blockSize)
            throws IOException {
        return new ByteBufferPool(path, maxSize, blockSize);
    }

    /**
     * Dispose.
     */
    public final void dispose() {
        pool_.clear();
    }

    /**
     * Gets the buffer.
     * @return the buffer
     */
    public final IMessageWrapper getBuffer() {
        return new ByteBufferWrapper();
    }

    /**
     * Creates the direct buffer.
     * @param idx the idx
     * @return the byte buffer
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
     * Creates the file buffer.
     * @param idx the idx
     * @return the byte buffer
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
     * Creates the heap buffer.
     * @param idx the idx
     * @return the byte buffer
     */
    private ByteBuffer createHeapBuffer(final int idx) {
        if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "createHeapBuffer");
        return ByteBuffer.allocate(idx);
    }

    /**
     * Free.
     * @param buffer the buffer
     */
    private final void free(final ByteBuffer buffer) {
        if (!buffer.isDirect()) return;
        buffer.clear();
        final List<ByteBuffer> list = getRepo(buffer.capacity() / UNIT_SIZE);
        synchronized (list) {
            list.add(buffer);
        }
    }

    /**
     * Gets the.
     * @param length the length
     * @return the byte buffer
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
     * Gets the repo.
     * @param idx the idx
     * @return the repo
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

    /**
     * The Class ByteBufferWrapper.
     */
    private class ByteBufferWrapper implements IMessageWrapper {
        /** The buffer_. */
        private ByteBuffer buffer_;

        // (non-Javadoc)
        // @see com.tmax.probus.nio.api.IMessageWrapper#clear()
        @Override public void clear() {
            if (buffer_ != null) buffer_.clear();
        }

        // (non-Javadoc)
        // @see com.tmax.probus.nio.api.IMessageWrapper#compact()
        @Override public void compact() {
            if (buffer_ != null) buffer_.compact();
        }

        // (non-Javadoc)
        // @see com.tmax.probus.nio.api.IMessageWrapper#flip()
        @Override public void flip() {
            if (buffer_ != null) buffer_.flip();
        }

        /**
         * Gets the byte buffer.
         * @return the buffer
         */
        @Override public final ByteBuffer getByteBuffer() {
            return buffer_;
        }

        // (non-Javadoc)
        // @see com.tmax.probus.nio.api.IMessageWrapper#getBytes()
        @Override public byte[] getBytes() {
            if (buffer_.limit() <= 0) return null;
            buffer_.rewind();
            final byte[] b = new byte[buffer_.limit()];
            buffer_.get(b);
            return b;
        }

        // (non-Javadoc)
        // @see com.tmax.probus.nio.api.IMessageWrapper#hasRemaining()
        @Override public boolean hasRemaining() {
            if (buffer_ == null) return false;
            return buffer_.hasRemaining();
        }

        // (non-Javadoc)
        // @see com.tmax.probus.nio.api.IMessageWrapper#init(int)
        @Override public final void init(final int size) {
            if (buffer_ != null) free(buffer_);
            buffer_ = get(size);
        }

        // (non-Javadoc)
        // @see com.tmax.probus.nio.api.IMessageWrapper#release()
        @Override public final void release() {
            if (buffer_ != null) free(buffer_);
            buffer_ = null;
        }

        // (non-Javadoc)
        // @see com.tmax.probus.nio.api.IMessageWrapper#remaining()
        @Override public int remaining() {
            if (buffer_ != null) return buffer_.remaining();
            return -1;
        }

        // (non-Javadoc)
        // @see com.tmax.probus.nio.api.IMessageWrapper#rewind()
        @Override public void rewind() {
            if (buffer_ != null) buffer_.rewind();
        }

        @Override public boolean isValid() {
            return buffer_ != null;
        }
    }
}
