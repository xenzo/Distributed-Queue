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
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import com.tmax.probus.nio.api.IConnectionEventListener;
import com.tmax.probus.nio.api.IEndPointInfo;
import com.tmax.probus.nio.api.IMessageEventListener;
import com.tmax.probus.nio.api.IMessageReader;
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
    /** The end point map_. */
    private Map<IEndPointInfo, Queue<ISession>> endPointMap_;

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
    @Override public ISession getSession(final SelectableChannel channel) {
        return channelSessionMap_.get(channel);
    }

    /** {@inheritDoc} */
    @Override public void init() {
        channelSessionMap_ = new ConcurrentHashMap<SelectableChannel, ISession>();
        super.init();
    }

    /** {@inheritDoc} */
    @Override public ISession newSession() {
        return null;
    }

    /** {@inheritDoc} */
    @Override public void putSession(final SelectableChannel channel, final ISession session) {
        channelSessionMap_.put(channel, session);
    }

    /** {@inheritDoc} */
    @Override public ISession removeSession(final SelectableChannel channel) {
        return channelSessionMap_.remove(channel);
    }

    /** {@inheritDoc} */
    @Override protected final SocketChannel accept(final SelectionKey key) throws IOException {
        final ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
        final SocketChannel channel = serverChannel.accept();
        channel.configureBlocking(false);
        if (isConnected(channel)) {
            final ISession session = newSession();
            session.setChannel(channel);
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
        session = newSession();
        session.setChannel(channel);
        putSession(channel, session);
        register(channel, SelectionKey.OP_CONNECT);
        if (logger.isLoggable(FINER)) logger.exiting(getClass().getName(), "connect(InetSocketAddress, InetSocketAddress)", "end - return value=" + session);
        return session;
    }

    protected IConnectionEventListener getConnectionEventListener(final SelectableChannel channel) {
        return getSession(channel).getConnectionEventListener();
    }

    protected IMessageEventListener getMessageEventListener(final SocketChannel channel) {
        return getSession(channel).getMessageEventListener();
    }

    /** {@inheritDoc} */
    @Override protected IMessageReader getMessageReader(final SocketChannel channel) {
        return getSession(channel).getMessageHandler();
    }

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
}
