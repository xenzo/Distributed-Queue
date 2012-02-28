/*
 * SampleSession.java Version 1.0 Feb 14, 2012
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
package com.tmax.probus.nio.example;


import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import com.tmax.probus.nio.api.IInterestOptsStrategy;
import com.tmax.probus.nio.api.IMessageReader;
import com.tmax.probus.nio.api.IMessageWrapper;
import com.tmax.probus.nio.api.IReactor;
import com.tmax.probus.nio.api.ISessionReactor;
import com.tmax.probus.nio.reactor.AbstractMessageReader;
import com.tmax.probus.nio.reactor.AbstractSession;
import com.tmax.probus.nio.util.ByteBufferPool;


/**
 * The Class SampleSession.
 */
class SampleSession extends AbstractSession {
    /** The strategy_. */
    IInterestOptsStrategy strategy_;
    /** The message reader_. */
    IMessageReader messageReader_;
    /** The buffer pool_. */
    ByteBufferPool bufferPool_;

    /**
     * Instantiates a new sample session.
     * @param channel the channel
     * @param bufferPool the buffer pool
     */
    public SampleSession(ISessionReactor reactor, final SocketChannel channel, final ByteBufferPool bufferPool) {
        this(reactor, channel, bufferPool, bufferPool.getBuffer());
    }

    /**
     * Instantiates a new sample session.
     * @param channel the channel
     * @param bufferPool the buffer pool
     * @param readBufferWrapper the read buffer wrapper
     */
    protected SampleSession(ISessionReactor reactor, final SocketChannel channel, final ByteBufferPool bufferPool, final IMessageWrapper readBufferWrapper) {
        super(reactor, channel, readBufferWrapper);
        bufferPool_ = bufferPool;
        strategy_ = new SampleOptsStrategy();
        messageReader_ = new SampleMessageReader();
    }

    // (non-Javadoc)
    // @see com.tmax.probus.nio.api.ISession#getInterestOptsStrategy()
    /** {@inheritDoc} */
    @Override public IInterestOptsStrategy getInterestOptsStrategy() {
        return strategy_;
    }

    /** {@inheritDoc} */
    @Override public void init() {
        readBufferWrapper_.init(1024);
    }

    // (non-Javadoc)
    // @see com.tmax.probus.nio.api.ISession#onErrorOccured()
    /** {@inheritDoc} */
    @Override public void onErrorOccured() {
    }

    // (non-Javadoc)
    // @see com.tmax.probus.nio.reactor.AbstractSession#getMessageMaker()
    /** {@inheritDoc} */
    @Override protected IMessageReader getMessageReader() {
        return messageReader_;
    }

    /**
     * The Class SampleMessageReader.
     */
    class SampleMessageReader extends AbstractMessageReader {
        // (non-Javadoc)
        // @see com.tmax.probus.nio.reactor.AbstractMessageMaker#computeMessageLength(byte[])
        /** {@inheritDoc} */
        @Override protected int computeMessageLength(final byte[] header) {
            return Integer.parseInt(new String(header).trim());
        }

        // (non-Javadoc)
        // @see com.tmax.probus.nio.reactor.AbstractMessageMaker#getHeaderLength()
        /** {@inheritDoc} */
        @Override protected int getHeaderLength() {
            return 5;
        }

        // (non-Javadoc)
        // @see com.tmax.probus.nio.reactor.AbstractMessageReader#onMessageReceivedComplete(byte[])
        /** {@inheritDoc} */
        @Override protected void onMessageReceivedComplete(final byte[] msg) {
            System.out.println(">>> " + new String(msg));
            processMessageReceived(msg);
        }
    }

    /**
     * The Class SampleOptsStrategy.
     */
    class SampleOptsStrategy implements IInterestOptsStrategy {
        // (non-Javadoc)
        // @see com.tmax.probus.nio.api.IInterestOptsStrategy#afterAccept(com.tmax.probus.nio.api.IReactor, java.nio.channels.SelectableChannel, java.lang.Object)
        /** {@inheritDoc} */
        @Override public void afterAccept(final IReactor ioReactor, final SelectableChannel channel, final Object attachment) {
            ioReactor.register(channel, SelectionKey.OP_READ, attachment);
        }

        // (non-Javadoc)
        // @see com.tmax.probus.nio.api.IInterestOptsStrategy#afterConnect(com.tmax.probus.nio.api.IReactor, java.nio.channels.SelectableChannel, java.lang.Object)
        /** {@inheritDoc} */
        @Override public void afterConnect(final IReactor ioReactor, final SelectableChannel channel, final Object attachment) {
            ioReactor.register(channel, SelectionKey.OP_WRITE, attachment);
        }

        // (non-Javadoc)
        // @see com.tmax.probus.nio.api.IInterestOptsStrategy#afterRead(com.tmax.probus.nio.api.IReactor, java.nio.channels.SelectableChannel, java.lang.Object)
        /** {@inheritDoc} */
        @Override public void afterRead(final IReactor ioReactor, final SelectableChannel channel, final Object attachment) {
            ioReactor.changeOpts(channel, SelectionKey.OP_WRITE);
        }

        // (non-Javadoc)
        // @see com.tmax.probus.nio.api.IInterestOptsStrategy#afterWrite(com.tmax.probus.nio.api.IReactor, java.nio.channels.SelectableChannel, java.lang.Object)
        /** {@inheritDoc} */
        @Override public void afterWrite(final IReactor ioReactor, final SelectableChannel channel, final Object attachment) {
            ioReactor.changeOpts(channel, SelectionKey.OP_READ);
        }
    }
}
