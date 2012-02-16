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
import java.nio.channels.SocketChannel;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

import com.tmax.probus.nio.api.IInterestOptsStrategy;
import com.tmax.probus.nio.api.IMessageMaker;
import com.tmax.probus.nio.api.IMessageWrapper;
import com.tmax.probus.nio.api.ISession;


/**
 * The Class AbstractSession.
 */
public abstract class AbstractSession implements ISession {
    /** Logger for this class. */
    private final transient Logger logger = Logger.getLogger("com.tmax.probus.nio.reactor");
    /** The write queue_. */
    private Queue<IMessageWrapper> writeQueue_ = new LinkedBlockingQueue<IMessageWrapper>();
    /** The read buffer lock_. */
    private final Lock readBufferLock_ = new ReentrantLock(true);
    /** The write queue lock_. */
    private final Lock writeQueueLock_ = new ReentrantLock(true);
    /** The strategy_. */
    private final IInterestOptsStrategy strategy_;
    /** The read buffer wrapper_. */
    private IMessageWrapper readBufferWrapper_;
    /** The maker_. */
    private IMessageMaker maker_;
    /** The channel_. */
    private SocketChannel channel_;

    /**
     * Instantiates a new abstract session.
     * @param channel the channel
     * @param strategy the strategy
     * @param readBufferWrapper the read buffer wrapper
     * @param readBufferSize the read buffer size
     */
    public AbstractSession(final SocketChannel channel, final IInterestOptsStrategy strategy, final IMessageWrapper readBufferWrapper) {
        channel_ = channel;
        strategy_ = strategy;
        readBufferWrapper_ = readBufferWrapper;
        if (!readBufferWrapper_.isValid()) throw new IllegalArgumentException();
    }

    // (non-Javadoc)
    // @see com.tmax.probus.nio.api.ISession#acquireBuffer()
    @Override public ByteBuffer acquireReadBuffer() {
        if (!readBufferWrapper_.isValid()) return null;
        readBufferLock_.lock();
        return readBufferWrapper_.getByteBuffer();
    }

    // (non-Javadoc)
    // @see com.tmax.probus.nio.api.ISession#acquireWriteQueue()
    @Override public Queue<IMessageWrapper> acquireWriteQueue() {
        if (writeQueue_ == null) return null;
        writeQueueLock_.lock();
        return writeQueue_;
    }

    // (non-Javadoc)
    // @see com.tmax.probus.nio.api.ISession#addWriteMessage(com.tmax.probus.nio.api.IMessageWrapper)
    @Override public void addWriteMessage(final IMessageWrapper carrier) {
        if (writeQueue_ != null) writeQueue_.offer(carrier);
    }

    // (non-Javadoc)
    // @see com.tmax.probus.nio.api.ISession#destroy()
    @Override public void destroy() {
        if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "destroy");
        channel_ = null;
        final Queue<IMessageWrapper> writeQueue = writeQueue_;
        writeQueue_ = null;
        writeQueue.clear();
        final IMessageWrapper readBufferWrapper = readBufferWrapper_;
        readBufferWrapper_ = null;
        readBufferWrapper.release();
        if (logger.isLoggable(FINER)) logger.exiting(getClass().getName(), "destroy");
    }

    // (non-Javadoc)
    // @see com.tmax.probus.nio.api.ISession#getChannel()
    @Override public SocketChannel getChannel() {
        return channel_;
    }

    // (non-Javadoc)
    // @see com.tmax.probus.nio.api.ISession#getInterestOptsStrategy()
    @Override public IInterestOptsStrategy getInterestOptsStrategy() {
        return strategy_;
    }

    // (non-Javadoc)
    // @see com.tmax.probus.nio.api.ISession#processMessageRead(java.nio.ByteBuffer)
    @Override public void onMessageRead() {
        if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "processMessageRead");
        final Lock lock = readBufferLock_;
        lock.lock();
        try {
            readBufferWrapper_.flip();
            maker_.putData(readBufferWrapper_.getByteBuffer());
            readBufferWrapper_.compact();
        } finally {
            lock.unlock();
        }
        if (logger.isLoggable(FINER)) logger.exiting(getClass().getName(), "processMessageRead");
    }

    // (non-Javadoc)
    // @see com.tmax.probus.nio.api.ISession#processMessageReceived(java.nio.ByteBuffer)
    @Override public void processMessageReceived(final byte[] message) {
        if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "processMessageReceived");
        // XXX must do something
        if (logger.isLoggable(FINER)) logger.exiting(getClass().getName(), "processMessageReceived");
    }

    // (non-Javadoc)
    // @see com.tmax.probus.nio.api.ISession#processMessageSended(java.nio.ByteBuffer)
    @Override public void processMessageSended(final byte[] message) {
        if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "processMessageSended");
        // XXX must do something
        if (logger.isLoggable(FINER)) logger.exiting(getClass().getName(), "processMessageSended");
    }

    // (non-Javadoc)
    // @see com.tmax.probus.nio.api.ISession#processMessageWrite(java.nio.ByteBuffer)
    @Override public void onMessageWrite(final ByteBuffer readBuffer) {
        if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "processMessageWrite");
        // XXX must do something
        if (logger.isLoggable(FINER)) logger.exiting(getClass().getName(), "processMessageWrite");
    }

    // (non-Javadoc)
    // @see com.tmax.probus.nio.api.ISession#processSessionClosed()
    @Override public void onSessionClosed() {
        if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "processSessionClosed");
        // XXX must do something
        if (logger.isLoggable(FINER)) logger.exiting(getClass().getName(), "processSessionClosed");
    }

    // (non-Javadoc)
    // @see com.tmax.probus.nio.api.ISession#onSessionOpened()
    @Override public void onSessionOpened() {
        if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "onSessionOpened");
        // XXX must do something
        if (logger.isLoggable(FINER)) logger.exiting(getClass().getName(), "onSessionOpened");
    }

    // (non-Javadoc)
    // @see com.tmax.probus.nio.api.ISession#releaseBuffer()
    @Override public void releaseReadBuffer() {
        readBufferLock_.unlock();
    }

    // (non-Javadoc)
    // @see com.tmax.probus.nio.api.ISession#releaseWriteQueue()
    @Override public void releaseWriteQueue() {
        writeQueueLock_.unlock();
    }

    // (non-Javadoc)
    // @see com.tmax.probus.nio.api.ISession#writeMessage(byte[])
    @Override public void writeMessage(final byte[] msg) {
        writeMessage(msg, 0, msg.length);
    }

    // (non-Javadoc)
    // @see com.tmax.probus.nio.api.ISession#writeMessage(byte[], int, int)
    @Override public void writeMessage(final byte[] msg, final int offset, final int length) {
        if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "writeMessage");
        final IMessageWrapper messageWrapper = createWriteMessageWrapper(msg, offset, length);
        addWriteMessage(messageWrapper);
        if (logger.isLoggable(FINER)) logger.exiting(getClass().getName(), "writeMessage");
    }

    /**
     * Creates the byte buffer carrier.
     * @param msg the msg
     * @param offset the offset
     * @param length the length
     * @return the i byte buffer carrier
     */
    abstract protected IMessageWrapper createWriteMessageWrapper(byte[] msg, int offset, int length);
}
