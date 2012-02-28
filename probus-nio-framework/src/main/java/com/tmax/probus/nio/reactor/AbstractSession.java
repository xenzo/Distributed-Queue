/*
 * AbstractSession.java Version 1.0 Feb 28, 2012
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
package com.tmax.probus.nio.reactor;


import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.tmax.probus.nio.api.IMessageReader;
import com.tmax.probus.nio.api.IReactor;
import com.tmax.probus.nio.api.ISession;


/**
 * The Class AbstractSession.
 */
public abstract class AbstractSession implements ISession {
    /** The write queue_. */
    private final Queue<ByteBuffer> writeQueue_ = new LinkedBlockingQueue<ByteBuffer>();
    /** The read buffer lock_. */
    private final Lock readBufferLock_ = new ReentrantLock();
    /** The write queue lock_. */
    private final Lock writeQueueLock_ = new ReentrantLock();
    /** The channel_. */
    private final SocketChannel channel_;
    /** The read buffer_. */
    private ByteBuffer readBuffer_;
    /** The reactor_. */
    private IReactor reactor_;

    /**
     * Instantiates a new abstract session.
     * @param reactor the reactor
     * @param channel the channel
     */
    public AbstractSession(IReactor reactor, final SocketChannel channel) {
        reactor_ = reactor;
        channel_ = channel;
    }

    /** {@inheritDoc} */
    @Override public ByteBuffer acquireReadBuffer() {
        final Lock lock = readBufferLock_;
        lock.lock();
        if (readBuffer_ == null) readBuffer_ = createReadBuffer();
        return readBuffer_;
    }

    /** {@inheritDoc} */
    @Override public Queue<ByteBuffer> acquireWriteQueue() {
        final Lock lock = writeQueueLock_;
        lock.lock();
        return writeQueue_;
    }

    /** {@inheritDoc} */
    @Override public void afterAccept(final IReactor reactor) {
        reactor.register(channel_, SelectionKey.OP_READ);
    }

    /** {@inheritDoc} */
    @Override public void afterConnect(final IReactor reactor) {
        reactor.register(channel_, SelectionKey.OP_WRITE);
    }

    /** {@inheritDoc} */
    @Override public void afterRead(final IReactor reactor) {
        reactor.changeOpts(channel_, SelectionKey.OP_WRITE);
    }

    /** {@inheritDoc} */
    @Override public void afterWrite(final IReactor reactor) {
        reactor.changeOpts(channel_, SelectionKey.OP_READ);
    }

    /** {@inheritDoc} */
    @Override public boolean onMessageRead() {
        boolean result = false;
        final ByteBuffer readBuffer = acquireReadBuffer();
        try {
            final IMessageReader reader = getMessageReader();
            readBuffer.flip();
            result = reader.readBuffer(readBuffer);
            readBuffer.compact();
            return result;
        } finally {
            releaseReadBuffer();
        }
    }

    /** {@inheritDoc} */
    @Override public void releaseReadBuffer() {
        final Lock lock = readBufferLock_;
        lock.unlock();
    }

    /** {@inheritDoc} */
    @Override public void releaseWriteQueue() {
        final Lock lock = writeQueueLock_;
        lock.unlock();
    }

    /** {@inheritDoc} */
    @Override public void write(final byte[] msg) {
        write(msg, 0, msg.length);
    }

    /** {@inheritDoc} */
    @Override public void write(final byte[] msg, final int offset, final int length) {
        final ByteBuffer buffer = createWriteByteBuffer(msg, offset, length);
        final Queue<ByteBuffer> writeQueue = acquireWriteQueue();
        try {
            writeQueue.add(buffer);
            reactor_.getReadWriteProcessor().changeOpts(channel_, SelectionKey.OP_WRITE);
            reactor_.getReadWriteProcessor().wakeup();
        } finally {
            releaseWriteQueue();
        }
    }

    /**
     * Creates the write byte buffer.
     * @param msg the msg
     * @param offset the offset
     * @param length the length
     * @return the byte buffer
     */
    protected ByteBuffer createWriteByteBuffer(final byte[] msg, final int offset, final int length) {
        return ByteBuffer.wrap(msg, offset, length);
    }

    /**
     * Creates the read buffer.
     * @return the byte buffer
     */
    abstract protected ByteBuffer createReadBuffer();

    /**
     * Gets the message reader.
     * @return the message reader
     */
    abstract protected IMessageReader getMessageReader();
}
