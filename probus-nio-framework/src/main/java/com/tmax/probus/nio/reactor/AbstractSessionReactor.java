/*
 * AbstractSessionReactor.java Version 1.0 Mar 9, 2012
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
import java.net.InetSocketAddress;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import com.tmax.probus.nio.api.IConnectionEventListener;
import com.tmax.probus.nio.api.IMessageEventListener;
import com.tmax.probus.nio.api.IMessageHandler;
import com.tmax.probus.nio.api.ISession;
import com.tmax.probus.nio.api.ISessionManager;


/**
 * The Class AbstractSessionReactor.
 */
public abstract class AbstractSessionReactor extends AbstractReactor implements ISessionManager {
    /** Logger for this class. */
    private final transient Logger logger = Logger.getLogger("com.tmax.probus.nio.reactor");
    /** The channel session map_. */
    private Map<SelectableChannel, ISession> channelSessionMap_;

    /** {@inheritDoc} */
    @Override public void destroy() {
        super.destroy();
        if (channelSessionMap_ != null) {
            final Map<SelectableChannel, ISession> channelSessionMap = channelSessionMap_;
            channelSessionMap_ = null;
            channelSessionMap.clear();
        }
    }

    /** {@inheritDoc} */
    @Override public ISession getSession(final SocketChannel channel) {
        return channelSessionMap_.get(channel);
    }

    /** {@inheritDoc} */
    @Override public void init() {
        if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "init");
        channelSessionMap_ = new ConcurrentHashMap<SelectableChannel, ISession>();
        super.init();
        if (logger.isLoggable(FINER)) logger.exiting(getClass().getName(), "init");
    }

    /** {@inheritDoc} */
    @Override public void putSession(final SocketChannel channel, final ISession session) {
        if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "putSession(SocketChannel=" + channel + ", ISession=" + session + ")", "start");
        channelSessionMap_.put(channel, session);
        if (logger.isLoggable(FINER)) logger.exiting(getClass().getName(), "putSession(SocketChannel, ISession)", "end");
    }

    /** {@inheritDoc} */
    @Override public ISession removeSession(final SocketChannel channel) {
        return channelSessionMap_.remove(channel);
    }

    /** {@inheritDoc} */
    @Override protected final SocketChannel accept(final SelectionKey key) throws IOException {
        final ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
        final SocketChannel channel = serverChannel.accept();
        channel.configureBlocking(false);
        if (channel != null && channel.finishConnect()) {
            final ISession session = createSession(serverChannel, channel);
            putSession(channel, session);
            return channel;
        }
        return null;
    }

    /**
     * Bind.
     * @param localAddr the local addr
     * @return the server socket channel
     * @throws IOException Signals that an I/O exception has occurred.
     */
    protected ServerSocketChannel bind(final InetSocketAddress localAddr) throws IOException {
        if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "bind(InetSocketAddress=" + localAddr + ")", "start");
        ServerSocketChannel server = null;
        server = ServerSocketChannel.open();
        server.configureBlocking(false);
        server.socket().bind(localAddr);
        getAcceptDispatcher().register(server, SelectionKey.OP_ACCEPT);
        if (logger.isLoggable(FINER)) logger.exiting(getClass().getName(), "bind(InetSocketAddress)", "end - return value=" + server);
        return server;
    }

    /**
     * Connect.
     * @param remoteAddr the remote addr
     * @param localAddr the local addr
     * @return the i session
     * @throws IOException Signals that an I/O exception has occurred.
     */
    protected ISession connect(final InetSocketAddress remoteAddr, final InetSocketAddress localAddr)
            throws IOException {
        if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "connect(InetSocketAddress=" + remoteAddr + ", InetSocketAddress=" + localAddr + ")", "start");
        SocketChannel channel = null;
        ISession session = null;
        channel = SocketChannel.open();
        channel.configureBlocking(false);
        if (localAddr != null) channel.socket().bind(localAddr);
        channel.connect(remoteAddr);
        session = createSession(null, channel);
        putSession(channel, session);
        getConnectDispatcher().register(channel, SelectionKey.OP_CONNECT);
        if (logger.isLoggable(FINER)) logger.exiting(getClass().getName(), "connect(InetSocketAddress, InetSocketAddress)", "end - return value=" + session);
        return session;
    }

    /**
     * Creates the session.
     * @param serverChannel the server channel
     * @param channel the channel
     * @return the i session
     */
    protected abstract ISession createSession(final SelectableChannel serverChannel, final SocketChannel channel);

    /** {@inheritDoc} */
    @Override protected void handOffAfterAccept(final SocketChannel channel) {
        getSession(channel).afterAccept(this);
    }

    /** {@inheritDoc} */
    @Override protected void handOffAfterConnect(final SocketChannel channel) {
        getSession(channel).afterConnect(this);
    }

    /** {@inheritDoc} */
    @Override protected void handOffAfterRead(final SocketChannel channel) {
        getSession(channel).afterRead(this);
    }

    /** {@inheritDoc} */
    @Override protected void handOffAfterWrite(final SocketChannel channel) {
        getSession(channel).afterWrite(this);
    }

    /** {@inheritDoc} */
    @Override protected IConnectionEventListener getConnectionEventListener(SocketChannel channel) {
        return null;
    }

    /** {@inheritDoc} */
    @Override protected IMessageEventListener getMessageEventListener(SocketChannel channel) {
        return null;
    }

    /** {@inheritDoc} */
    @Override protected IMessageHandler getMessageHandler(SocketChannel channel) {
        return null;
    }
}
