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
import com.tmax.probus.nio.reactor.AbstractMessageReader;
import com.tmax.probus.nio.reactor.AbstractSession;


class SampleSession extends AbstractSession {
    IInterestOptsStrategy strategy_;
    IMessageReader messageReader_;

    /**
     * @param channel
     * @param strategy
     * @param readBufferWrapper
     */
    protected SampleSession(SocketChannel channel, IMessageWrapper readBufferWrapper) {
        super(channel, readBufferWrapper);
        strategy_ = new SampleOptsStrategy();
        messageReader_ = new SampleMessageReader();
    }

    // (non-Javadoc)
    // @see com.tmax.probus.nio.api.ISession#onErrorOccured()
    @Override public void onErrorOccured() {
    }

    // (non-Javadoc)
    // @see com.tmax.probus.nio.reactor.AbstractSession#createWriteMessageWrapper(byte[], int, int)
    @Override protected IMessageWrapper createWriteMessageWrapper(byte[] msg, int offset, int length) {
        return null;
    }

    // (non-Javadoc)
    // @see com.tmax.probus.nio.reactor.AbstractSession#getMessageMaker()
    @Override protected IMessageReader getMessageReader() {
        return messageReader_;
    }

    // (non-Javadoc)
    // @see com.tmax.probus.nio.api.ISession#getInterestOptsStrategy()
    @Override public IInterestOptsStrategy getInterestOptsStrategy() {
        return strategy_;
    }

    class SampleMessageReader extends AbstractMessageReader {
        // (non-Javadoc)
        // @see com.tmax.probus.nio.reactor.AbstractMessageMaker#computeMessageLength(byte[])
        @Override protected int computeMessageLength(byte[] header) {
            return Integer.parseInt(new String(header).trim());
        }

        // (non-Javadoc)
        // @see com.tmax.probus.nio.reactor.AbstractMessageMaker#getHeaderLength()
        @Override protected int getHeaderLength() {
            return 5;
        }

        // (non-Javadoc)
        // @see com.tmax.probus.nio.reactor.AbstractMessageReader#onMessageReceivedComplete(byte[])
        @Override protected void onMessageReceivedComplete(byte[] msg) {
            processMessageReceived(msg);
        }
    }

    class SampleOptsStrategy implements IInterestOptsStrategy {
        // (non-Javadoc)
        // @see com.tmax.probus.nio.api.IInterestOptsStrategy#afterAccept(com.tmax.probus.nio.api.IReactor, java.nio.channels.SelectableChannel, java.lang.Object)
        @Override public void afterAccept(IReactor ioReactor, SelectableChannel channel, Object attachment) {
            ioReactor.register(channel, SelectionKey.OP_READ, attachment);
        }

        // (non-Javadoc)
        // @see com.tmax.probus.nio.api.IInterestOptsStrategy#afterConnect(com.tmax.probus.nio.api.IReactor, java.nio.channels.SelectableChannel, java.lang.Object)
        @Override public void afterConnect(IReactor ioReactor, SelectableChannel channel, Object attachment) {
            ioReactor.register(channel, SelectionKey.OP_WRITE, attachment);
        }

        // (non-Javadoc)
        // @see com.tmax.probus.nio.api.IInterestOptsStrategy#afterRead(com.tmax.probus.nio.api.IReactor, java.nio.channels.SelectableChannel, java.lang.Object)
        @Override public void afterRead(IReactor ioReactor, SelectableChannel channel, Object attachment) {
            ioReactor.changeOpts(channel, SelectionKey.OP_WRITE);
        }

        // (non-Javadoc)
        // @see com.tmax.probus.nio.api.IInterestOptsStrategy#afterWrite(com.tmax.probus.nio.api.IReactor, java.nio.channels.SelectableChannel, java.lang.Object)
        @Override public void afterWrite(IReactor ioReactor, SelectableChannel channel, Object attachment) {
            ioReactor.changeOpts(channel, SelectionKey.OP_READ);
        }
    }
}
