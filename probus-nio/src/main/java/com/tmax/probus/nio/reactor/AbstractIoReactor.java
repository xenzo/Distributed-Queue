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
import java.nio.channels.SocketChannel;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

import com.tmax.probus.nio.api.IInterestOptsStrategy;
import com.tmax.probus.nio.api.IReactor;
import com.tmax.probus.nio.api.ISession;
import com.tmax.probus.nio.api.ISessionReactor;


/**
 * The Class IOReactor.
 */
public abstract class AbstractIoReactor extends AbstractReactor implements ISessionReactor {
    /** The Constant MAX_RETRY_WRITE_CNT. */
    private static final int MAX_RETRY_WRITE_CNT = 2;
    /** Logger for this class. */
    private final transient Logger logger = Logger.getLogger("com.tmax.probus.nio");
    /** The sessions to close_. */
    private Queue<ISession> sessionsToClose_ = new LinkedBlockingQueue<ISession>();
    /** The strategy_. */
    private final IInterestOptsStrategy strategy_ = new IInterestOptsStrategy() {
        /** {@inheritDoc} */
        @Override public void afterAccept(final IReactor ioReactor, final SelectableChannel channel, final Object attachment) {
            ioReactor.register(channel, SelectionKey.OP_READ, attachment);
        }

        /** {@inheritDoc} */
        @Override public void afterConnect(final IReactor ioReactor, final SelectableChannel channel, final Object attachment) {
            ioReactor.register(channel, SelectionKey.OP_WRITE, attachment);
        }

        /** {@inheritDoc} */
        @Override public void afterRead(final IReactor ioReactor, final SelectableChannel channel, final Object attachment) {
            ioReactor.changeOpts(channel, SelectionKey.OP_WRITE);
        }

        /** {@inheritDoc} */
        @Override public void afterWrite(final IReactor ioReactor, final SelectableChannel channel, final Object attachment) {
            ioReactor.changeOpts(channel, SelectionKey.OP_READ);
        }
    };

    /**
     * Close.
     * @param session the channel
     */
    protected void close(final ISession session) {
        sessionsToClose_.add(session);
    }

    /**
     * Creates the session.
     * @param iReactor the i reactor
     * @param serverChannel the server channel
     * @param channel the channel
     * @return the i session
     */
    protected ISession createSession(final ISessionReactor iReactor, final SelectableChannel serverChannel, final SocketChannel channel) {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override protected void destroy() {
        super.destroy();
        final Queue<ISession> queue = sessionsToClose_;
        sessionsToClose_ = null;
        queue.clear();
    }

    /** {@inheritDoc} */
    @Override protected final void handleAccept(final SelectionKey key) throws IOException {
        if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "handleAccept");
        final SelectableChannel channel = processAccept(key);
        final ISession session = createSession((ISessionReactor) getIoReactor(), key.channel(), (SocketChannel) channel);
        IInterestOptsStrategy strategy = null;
        if (session == null || (strategy = session.getInterestOptsStrategy()) == null) strategy = strategy_;
        if (channel != null) strategy.afterAccept(getIoReactor(), channel, session);
        if (logger.isLoggable(FINER)) logger.exiting(getClass().getName(), "handleAccept");
    }

    /** {@inheritDoc} */
    @Override protected final void handleConnect(final SelectionKey key) throws IOException {
        if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "handleConnect");
        final SelectableChannel channel = processConnect(key);
        final ISession session = createSession((ISessionReactor) getIoReactor(), key.channel(), (SocketChannel) channel);
        IInterestOptsStrategy strategy = null;
        if (session == null || (strategy = session.getInterestOptsStrategy()) == null) strategy = strategy_;
        if (channel != null) strategy.afterConnect(getIoReactor(), channel, session);
        if (logger.isLoggable(FINER)) logger.exiting(getClass().getName(), "handleConnect");
    }

    /** {@inheritDoc} */
    @Override protected final void handleRead(final SelectionKey key) throws IOException {
        if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "handleRead");
        final SelectableChannel channel = key.channel();
        final ISession session = getSession(channel);
        IInterestOptsStrategy strategy = null;
        if (session == null || (strategy = session.getInterestOptsStrategy()) == null) strategy = strategy_;
        if (processRead(key)) strategy.afterRead(this, channel, key.attachment());
        if (logger.isLoggable(FINER)) logger.exiting(getClass().getName(), "handleRead");
    }

    /** {@inheritDoc} */
    @Override protected final void handleWrite(final SelectionKey key) throws IOException {
        if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "handleWrite");
        final SelectableChannel channel = key.channel();
        final ISession session = getSession(channel);
        IInterestOptsStrategy strategy = null;
        if (session == null || (strategy = session.getInterestOptsStrategy()) == null) strategy = strategy_;
        if (processWrite(key)) strategy.afterWrite(this, channel, key.attachment());
        if (logger.isLoggable(FINER)) logger.exiting(getClass().getName(), "handleWrite");
    }

    /**
     * Process accept.
     * @param key the key
     * @return the selectable channel
     * @throws IOException Signals that an I/O exception has occurred.
     */
    protected SelectableChannel processAccept(final SelectionKey key) throws IOException {
        throw new UnsupportedOperationException();
    }

    /**
     * Process connect.
     * @param key the key
     * @return the selectable channel
     * @throws IOException Signals that an I/O exception has occurred.
     */
    protected SelectableChannel processConnect(final SelectionKey key) throws IOException {
        throw new UnsupportedOperationException();
    }

    /**
     * Process pending jobs. {@inheritdoc}
     */
    @Override protected void processPendingJobs() {
        super.processPendingJobs();
        while (!sessionsToClose_.isEmpty()) {
            final ISession session = sessionsToClose_.poll();
            final SocketChannel channel = session.getChannel();
            session.onSessionClosed();
            session.destroy();
            deregister(channel);
        }
    }

    /**
     * Process read.
     * @param key the key
     * @return true, if successful
     * @throws IOException Signals that an I/O exception has occurred.
     */
    protected boolean processRead(final SelectionKey key) throws IOException {
        if (logger.isLoggable(FINER)) logger.entering("AbstractIoReactor", "processRead(SelectionKey=" + key + ")", "start");
        final SocketChannel channel = (SocketChannel) key.channel();
        final ISession session = getSession(channel);
        final ByteBuffer readBuffer_ = session.acquireReadBuffer();
        try {
            int nRead = 0, nLastRead = 0;
            System.out.println(readBuffer_.hasRemaining());
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
     * @throws IOException Signals that an I/O exception has occurred.
     */
    protected boolean processWrite(final SelectionKey key) throws IOException {
        if (logger.isLoggable(FINER)) logger.entering("AbstractIoReactor", "processWrite(SelectionKey=" + key + ")", "start");
        final ISession session = (ISession) key.attachment();
        final SocketChannel channel = session.getChannel();
        final Queue<ByteBuffer> queue = session.acquireWriteQueue();
        try {
            if (queue == null || queue.isEmpty()) return true;
            while (!queue.isEmpty()) {
                final ByteBuffer msg = queue.peek();
                System.out.println(new String(msg.array()));
                int cnt = 0;
                while (cnt++ < MAX_RETRY_WRITE_CNT && msg.hasRemaining()) {
                    channel.write(msg);
                }
                if (msg.hasRemaining()) return false;
                queue.remove();
                session.processMessageSended(msg.array());
            }
        } finally {
            session.releaseWriteQueue();
        }
        if (logger.isLoggable(FINER)) logger.exiting("AbstractIoReactor", "processWrite(SelectionKey)", "end - return value=" + true);
        return true;
    }
}
