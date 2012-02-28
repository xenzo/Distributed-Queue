/*
 * AbstractSession.java Version 1.0 Feb 15, 2012
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


import static java.util.logging.Level.*;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

import com.tmax.probus.nio.api.IMessageReader;
import com.tmax.probus.nio.api.IMessageWrapper;
import com.tmax.probus.nio.api.ISession;
import com.tmax.probus.nio.api.ISessionReactor;


/**
 * The Class AbstractSession.
 */
public abstract class AbstractSession implements ISession {
    /** Logger for this class. */
    private final transient Logger logger = Logger.getLogger("com.tmax.probus.nio.reactor");
    /** The write queue_. */
    private Queue<ByteBuffer> writeQueue_ = new LinkedBlockingQueue<ByteBuffer>();
    /** The read buffer lock_. */
    private final Lock readBufferLock_ = new ReentrantLock(true);
    /** The write queue lock_. */
    private final Lock writeQueueLock_ = new ReentrantLock(true);
    /** The read buffer wrapper_. */
    protected IMessageWrapper readBufferWrapper_;
    /** The channel_. */
    private SocketChannel channel_;
    private ISessionReactor reactor_;

    /**
     * Instantiates a new abstract session.
     * @param channel the channel
     * @param readBufferWrapper the read buffer wrapper
     */
    protected AbstractSession(ISessionReactor reactor, final SocketChannel channel, final IMessageWrapper readBufferWrapper) {
        reactor_ = reactor;
        channel_ = channel;
        readBufferWrapper_ = readBufferWrapper;
    }

    /** {@inheritDoc} */
    @Override public ByteBuffer acquireReadBuffer() {
        if (!readBufferWrapper_.isValid()) return null;
        readBufferLock_.lock();
        return readBufferWrapper_.getByteBuffer();
    }

    /** {@inheritDoc} */
    @Override public Queue<ByteBuffer> acquireWriteQueue() {
        if (writeQueue_ == null) return null;
        writeQueueLock_.lock();
        return writeQueue_;
    }

    /** {@inheritDoc} */
    @Override public void addWriteMessage(final ByteBuffer carrier) {
        if (writeQueue_ != null) writeQueue_.offer(carrier);
    }

    /** {@inheritDoc} */
    @Override public void destroy() {
        if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "destroy");
        channel_ = null;
        final Queue<ByteBuffer> writeQueue = writeQueue_;
        writeQueue_ = null;
        writeQueue.clear();
        final IMessageWrapper readBufferWrapper = readBufferWrapper_;
        readBufferWrapper_ = null;
        readBufferWrapper.release();
        if (logger.isLoggable(FINER)) logger.exiting(getClass().getName(), "destroy");
    }

    /** {@inheritDoc} */
    @Override public SocketChannel getChannel() {
        return channel_;
    }

    /** {@inheritDoc} */
    @Override public boolean onMessageRead() {
        if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "processMessageRead");
        final Lock lock = readBufferLock_;
        lock.lock();
        try {
            readBufferWrapper_.flip();
            final boolean result = getMessageReader().readBuffer(readBufferWrapper_.getByteBuffer());
            readBufferWrapper_.compact();
            if (logger.isLoggable(FINER)) logger.exiting(getClass().getName(), "processMessageRead");
            return result;
        } finally {
            lock.unlock();
        }
    }

    /** {@inheritDoc} */
    @Override public void onSessionClosed() {
        if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "processSessionClosed");
        // XXX must do something
        if (logger.isLoggable(FINER)) logger.exiting(getClass().getName(), "processSessionClosed");
    }

    /** {@inheritDoc} */
    @Override public void onSessionOpened() {
        if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "onSessionOpened");
        // XXX must do something
        if (logger.isLoggable(FINER)) logger.exiting(getClass().getName(), "onSessionOpened");
    }

    /** {@inheritDoc} */
    @Override public void processMessageReceived(final byte[] message) {
        if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "processMessageReceived");
        // XXX must do something
        if (logger.isLoggable(FINER)) logger.exiting(getClass().getName(), "processMessageReceived");
    }

    /** {@inheritDoc} */
    @Override public void processMessageSended(final byte[] message) {
        if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "processMessageSended");
        // XXX must do something
        if (logger.isLoggable(FINER)) logger.exiting(getClass().getName(), "processMessageSended");
    }

    /** {@inheritDoc} */
    @Override public void releaseReadBuffer() {
        readBufferLock_.unlock();
    }

    /** {@inheritDoc} */
    @Override public void releaseWriteQueue() {
        writeQueueLock_.unlock();
    }

    /** {@inheritDoc} */
    @Override public void writeMessage(final byte[] msg) {
        writeMessage(msg, 0, msg.length);
    }

    /** {@inheritDoc} */
    @Override public void writeMessage(final byte[] msg, final int offset, final int length) {
        if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "writeMessage");
        final ByteBuffer messageWrapper = createWriteMessageWrapper(msg, offset, length);
        addWriteMessage(messageWrapper);
        reactor_.changeOpts(channel_, SelectionKey.OP_WRITE);
        reactor_.wakeup();
        if (logger.isLoggable(FINER)) logger.exiting(getClass().getName(), "writeMessage");
    }

    /**
     * Creates the byte buffer carrier.
     * @param msg the msg
     * @param offset the offset
     * @param length the length
     * @return the i byte buffer carrier
     */
    protected ByteBuffer createWriteMessageWrapper(byte[] msg, int offset, int length) {
        return ByteBuffer.wrap(msg, offset, length);
    }

    /**
     * Gets the message reader.
     * @return the message reader
     */
    abstract protected IMessageReader getMessageReader();
}
