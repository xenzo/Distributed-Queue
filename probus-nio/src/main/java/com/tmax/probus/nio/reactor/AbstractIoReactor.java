/*
 * IOReactor.java Version 1.0 Feb 10, 2012
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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Queue;
import java.util.logging.Logger;

import com.tmax.probus.nio.api.IInterestOptsStrategy;
import com.tmax.probus.nio.api.IMessageWrapper;
import com.tmax.probus.nio.api.IReactor;
import com.tmax.probus.nio.api.ISession;


/**
 * The Class IOReactor.
 */
public abstract class AbstractIoReactor extends AbstractReactor {
    /** The Constant MAX_RETRY_WRITE_CNT. */
    private static final int MAX_RETRY_WRITE_CNT = 2;
    /** Logger for this class. */
    private final transient Logger logger = Logger.getLogger("com.tmax.probus.nio");
    /** The strategy_. */
    private final IInterestOptsStrategy strategy_ = new IInterestOptsStrategy() {
        // (non-Javadoc)
        // @see com.tmax.probus.nio.IInterestOptsStrategy#afterAccept(com.tmax.probus.nio.IReactor, java.nio.channels.SelectionKey, java.lang.Object)
        @Override public void afterAccept(final IReactor ioReactor, final SelectableChannel channel, final Object attachment) {
            ioReactor.register(channel, SelectionKey.OP_READ, attachment);
        }

        // (non-Javadoc)
        // @see com.tmax.probus.nio.IInterestOptsStrategy#afterConnect(com.tmax.probus.nio.IReactor, java.nio.channels.SelectionKey, java.lang.Object)
        @Override public void afterConnect(final IReactor ioReactor, final SelectableChannel channel, final Object attachment) {
            ioReactor.register(channel, SelectionKey.OP_WRITE, attachment);
        }

        // (non-Javadoc)
        // @see com.tmax.probus.nio.IInterestOptsStrategy#afterRead(com.tmax.probus.nio.IReactor, java.nio.channels.SelectionKey)
        @Override public void afterRead(final IReactor ioReactor, final SelectableChannel channel, final Object attachment) {
            ioReactor.changeOpts(channel, SelectionKey.OP_WRITE);
        }

        // (non-Javadoc)
        // @see com.tmax.probus.nio.IInterestOptsStrategy#afterWrite(com.tmax.probus.nio.IReactor, java.nio.channels.SelectionKey)
        @Override public void afterWrite(final IReactor ioReactor, final SelectableChannel channel, final Object attachment) {
            ioReactor.changeOpts(channel, SelectionKey.OP_READ);
        }
    };

    /**
     * Close.
     * @param session the channel
     */
    protected void close(final ISession session) {
        final SocketChannel channel = session.getChannel();
        try {
            channel.socket().close();
            channel.close();
        } catch (final IOException ex) {
            logger.log(WARNING, "" + ex.getMessage(), ex);
        }
        session.onSessionClosed();
        deregister(channel);
    }

    /**
     * Creates the session.
     * @param serverChannel the server channel
     * @param channel the channel
     * @return the i session
     */
    protected ISession createSession(final SelectableChannel serverChannel, final SocketChannel channel) {
        return null;
    }

    // (non-Javadoc)
    // @see com.tmax.probus.nio.reactor.AbstractReactor#handleAccept(java.nio.channels.SelectionKey)
    @Override protected final void handleAccept(final SelectionKey key) throws IOException {
        if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "handleAccept");
        final SelectableChannel channel = processAccept(key);
        final ISession session = createSession((ServerSocketChannel) key.channel(), (SocketChannel) channel);
        IInterestOptsStrategy strategy = null;
        if (session == null || (strategy = session.getInterestOptsStrategy()) == null) strategy = strategy_;
        if (channel != null) strategy.afterAccept(getIoReactor(), channel, session);
        if (logger.isLoggable(FINER)) logger.exiting(getClass().getName(), "handleAccept");
    }

    // (non-Javadoc)
    // @see com.tmax.probus.nio.reactor.AbstractReactor#handleConnect(java.nio.channels.SelectionKey)
    @Override protected final void handleConnect(final SelectionKey key) throws IOException {
        if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "handleConnect");
        final SelectableChannel channel = processConnect(key);
        final ISession session = (ISession) key.attachment();
        IInterestOptsStrategy strategy = null;
        if (session == null || (strategy = session.getInterestOptsStrategy()) == null) strategy = strategy_;
        if (channel != null) strategy.afterConnect(getIoReactor(), channel, session);
        if (logger.isLoggable(FINER)) logger.exiting(getClass().getName(), "handleConnect");
    }

    // (non-Javadoc)
    // @see com.tmax.probus.nio.reactor.AbstractReactor#handleRead(java.nio.channels.SelectionKey)
    @Override protected final void handleRead(final SelectionKey key) throws IOException {
        if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "handleRead");
        final ISession session = (ISession) key.attachment();
        IInterestOptsStrategy strategy = null;
        if (session == null || (strategy = session.getInterestOptsStrategy()) == null) strategy = strategy_;
        if (processRead(key)) strategy.afterRead(this, key.channel(), key.attachment());
        if (logger.isLoggable(FINER)) logger.exiting(getClass().getName(), "handleRead");
    }

    // (non-Javadoc)
    // @see com.tmax.probus.nio.reactor.AbstractReactor#handleWrite(java.nio.channels.SelectionKey)
    @Override protected final void handleWrite(final SelectionKey key) throws IOException {
        if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "handleWrite");
        final ISession session = (ISession) key.attachment();
        IInterestOptsStrategy strategy = null;
        if (session == null || (strategy = session.getInterestOptsStrategy()) == null) strategy = strategy_;
        if (processWrite(key)) strategy.afterWrite(this, key.channel(), key.attachment());
        if (logger.isLoggable(FINER)) logger.exiting(getClass().getName(), "handleWrite");
    }

    /**
     * Process accept.
     * @param key the key
     * @return the selectable channel
     */
    protected SelectableChannel processAccept(final SelectionKey key) throws IOException {
        throw new UnsupportedOperationException();
    }

    /**
     * Process connect.
     * @param key the key
     * @return the selectable channel
     */
    protected SelectableChannel processConnect(final SelectionKey key) throws IOException {
        throw new UnsupportedOperationException();
    }

    /**
     * Process read.
     * @param key the key
     * @return true, if successful
     * @throws IOException
     */
    protected boolean processRead(final SelectionKey key) throws IOException {
        if (logger.isLoggable(FINER)) logger.entering("AbstractIoReactor", "processRead(SelectionKey=" + key + ")", "start");
        final ISession session = (ISession) key.attachment();
        final SocketChannel channel = session.getChannel();
        final ByteBuffer readBuffer_ = session.acquireReadBuffer();
        try {
            int nRead = 0, nLastRead = 0;
            while (readBuffer_.hasRemaining() && (nLastRead = channel.read(readBuffer_)) > 0)
                nRead += nLastRead;
            if (logger.isLoggable(FINER)) logger.logp(FINER, getClass().getName(), "handleRead", "nRead : " + nRead);
            if (nLastRead < 0) {
                session.onErrorOccured();
                close(session);
                if (logger.isLoggable(FINER)) logger.exiting("AbstractIoReactor", "processRead(SelectionKey)", "end - return value=" + false);
                return false;
            }
        } finally {
            session.releaseReadBuffer();
        }
        return session.onMessageRead();
    }

    /**
     * Process write.
     * @param key the key
     * @return true, if successful
     * @throws IOException
     */
    protected boolean processWrite(final SelectionKey key) throws IOException {
        if (logger.isLoggable(FINER)) logger.entering("AbstractIoReactor", "processWrite(SelectionKey=" + key + ")", "start");
        final ISession session = (ISession) key.attachment();
        final SocketChannel channel = session.getChannel();
        final Queue<IMessageWrapper> queue = session.acquireWriteQueue();
        try {
            if (queue == null || queue.isEmpty()) return true;
            while (!queue.isEmpty()) {
                final IMessageWrapper msg = queue.peek();
                int cnt = 0;
                while (cnt++ < MAX_RETRY_WRITE_CNT && msg.hasRemaining()) {
                    channel.write(msg.getByteBuffer());
                }
                if (msg.hasRemaining()) return false;
                queue.remove();
                session.processMessageSended(msg.getBytes());
                msg.release();
            }
        } finally {
            session.releaseWriteQueue();
        }
        if (logger.isLoggable(FINER)) logger.exiting("AbstractIoReactor", "processWrite(SelectionKey)", "end - return value=" + true);
        return true;
    }
}
