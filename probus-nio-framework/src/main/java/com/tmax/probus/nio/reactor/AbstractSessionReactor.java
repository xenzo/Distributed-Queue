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
import java.net.ServerSocket;
import java.net.SocketAddress;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.tmax.probus.nio.api.IConnectionEventListener;
import com.tmax.probus.nio.api.IMessageEventListener;
import com.tmax.probus.nio.api.IMessageIoHandler;
import com.tmax.probus.nio.api.ISelectorDispatcher;
import com.tmax.probus.nio.api.ISession;
import com.tmax.probus.nio.api.ISessionHandler;
import com.tmax.probus.nio.api.ISessionManager;


/** The Class AbstractSessionReactor. */
public abstract class AbstractSessionReactor extends AbstractReactor implements ISessionManager {
    /** The channel session map. */
    private Map<SelectableChannel, ISession> channelSessionMap_;
    /** The session handler map. */
    private Map<SocketAddress, ISessionHandler> sessionHandlerMap_;

    /** {@inheritDoc} */
    @Override public ISession createSession(final SelectableChannel channel) {
        final ISessionHandler sessionHandler = getSessionHandler(channel);
        if (sessionHandler == null) throw new IllegalArgumentException("");
        final ISession session = newSession(channel);
        try {
            sessionHandler.sessionCreated(session);
            putSession(channel, session);
        } catch (final Exception ex) {
            logger.log(WARNING, "" + ex.getMessage(), ex);
        }
        return session;
    }

    /** {@inheritDoc} */
    @Override public void destroy() {
        if (channelSessionMap_ != null) {
            final Map<SelectableChannel, ISession> tmpSessionMap = channelSessionMap_;
            channelSessionMap_ = null;
            for (final ISession session : tmpSessionMap.values())
                session.destroy();
            tmpSessionMap.clear();
        }
        if (sessionHandlerMap_ != null) {
            final Map<SocketAddress, ISessionHandler> tmpSessionHandlerMap = sessionHandlerMap_;
            sessionHandlerMap_ = null;
            for (final ISessionHandler handler : tmpSessionHandlerMap.values())
                handler.destroy();
            tmpSessionHandlerMap.clear();
        }
        super.destroy();
    }

    /** {@inheritDoc} */
    @Override public ISession getSession(final SelectableChannel channel) {
        return channelSessionMap_.get(channel);
    }

    /** {@inheritDoc} */
    @Override public void init() {
        channelSessionMap_ = new ConcurrentHashMap<SelectableChannel, ISession>();
        sessionHandlerMap_ = new ConcurrentHashMap<SocketAddress, ISessionHandler>();
        super.init();
    }

    /** {@inheritDoc} */
    @Override public void putSession(final SelectableChannel channel, final ISession session) {
        channelSessionMap_.put(channel, session);
    }

    /** {@inheritDoc} */
    @Override public ISession removeSession(final SelectableChannel channel) {
        return channelSessionMap_.remove(channel);
    }

    /**
     * Adds the session handler.
     * @param address the address
     * @param handler the handler
     * @return the i session handler
     */
    protected ISessionHandler addSessionHandler(final SocketAddress address, final ISessionHandler handler) {
        return sessionHandlerMap_.put(address, handler);
    }

    /**
     * Bind.
     * @param localAddr the local addr
     * @param sessionHandler the session handler
     * @return the server socket channel
     * @throws IOException Signals that an I/O exception has occurred.
     */
    protected ServerSocketChannel bind(final InetSocketAddress localAddr, final ISessionHandler sessionHandler)
            throws IOException {
        if (logger.isLoggable(FINER))
            logger.entering(getClass().getName(), "bind(InetSocketAddress=" + localAddr + ")", "start");
        ServerSocketChannel server = ServerSocketChannel.open();
        server.configureBlocking(false);
        initServerSocket(server.socket());
        server.socket().bind(localAddr);
        sessionHandlerMap_.put(localAddr, sessionHandler);
        register(server, SelectionKey.OP_ACCEPT);
        if (logger.isLoggable(FINER))
            logger.exiting(getClass().getName(), "bind(InetSocketAddress)", "end - return value=" + server);
        return server;
    }

    /**
     * Connect.
     * @param remoteAddr the remote addr
     * @param localAddr the local addr
     * @param sessionHandler the session handler
     * @return the i session
     * @throws IOException Signals that an I/O exception has occurred.
     */
    protected ISession connect(final InetSocketAddress remoteAddr, final InetSocketAddress localAddr,
            final ISessionHandler sessionHandler) throws IOException {
        if (logger.isLoggable(FINER)) logger.entering(getClass().getName(),
            "connect(InetSocketAddress=" + remoteAddr + ", InetSocketAddress=" + localAddr + ")", "start");
        final SocketChannel channel = SocketChannel.open();
        channel.configureBlocking(false);
        channel.socket().bind(localAddr);
        channel.connect(remoteAddr);
        //
        sessionHandlerMap_.put(remoteAddr, sessionHandler);
        //
        final ISession session = createSession(channel);
        register(channel, SelectionKey.OP_CONNECT);
        if (logger.isLoggable(FINER)) logger.exiting(getClass().getName(), "connect(InetSocketAddress, InetSocketAddress)",
            "end - return session=" + session);
        return session;
    }

    /**
     * Gets the connection event listener.
     * @param channel the channel
     * @return the connection event listener
     */
    protected IConnectionEventListener getConnectionEventListener(final SelectableChannel channel) {
        return getSession(channel).getConnectionEventListener();
    }

    /**
     * Gets the message event listener.
     * @param channel the channel
     * @return the message event listener
     */
    protected IMessageEventListener getMessageEventListener(final SocketChannel channel) {
        return getSession(channel).getMessageEventListener();
    }

    /** {@inheritDoc} */
    @Override protected IMessageIoHandler getMessageHandler(final SocketChannel channel) {
        return getSession(channel).getMessageHandler();
    }

    /**
     * Gets the session handler.
     * @param channel the channel
     * @return the session handler
     */
    protected ISessionHandler getSessionHandler(final SelectableChannel channel) {
        if (channel instanceof SocketChannel) {
            final SocketChannel socketChannel = (SocketChannel) channel;
            ISessionHandler handler = sessionHandlerMap_.get(socketChannel.socket().getLocalAddress());
            if (handler == null) handler = sessionHandlerMap_.get(socketChannel.socket().getRemoteSocketAddress());
            return handler;
        }
        return null;
    }

    /**
     * Gets the session handler.
     * @param address the address
     * @return the session handler
     */
    protected ISessionHandler getSessionHandler(final SocketAddress address) {
        return sessionHandlerMap_.get(address);
    }

    /** {@inheritDoc} */
    @Override protected void handOffAfterAccept(final ISelectorDispatcher dispatcher, final SocketChannel channel) {
        getSession(channel).afterAccept(this);
    }

    /** {@inheritDoc} */
    @Override protected void handOffAfterConnect(final ISelectorDispatcher dispatcher, final SocketChannel channel) {
        getSession(channel).afterConnect(this);
    }

    /** {@inheritDoc} */
    @Override protected void handOffAfterRead(final ISelectorDispatcher dispatcher, final SocketChannel channel) {
        getSession(channel).afterRead(this);
    }

    /** {@inheritDoc} */
    @Override protected void handOffAfterWrite(final ISelectorDispatcher dispatcher, final SocketChannel channel) {
        getSession(channel).afterWrite(this);
    }

    /**
     * Inits the server socket.
     * @param socket the socket
     */
    abstract protected void initServerSocket(ServerSocket socket);

    /**
     * New session.
     * @param channel the channel
     * @return the session
     */
    abstract protected ISession newSession(final SelectableChannel channel);

    /**
     * Removes the session handler.
     * @param address the address
     * @return the i session handler
     */
    protected ISessionHandler removeSessionHandler(final SocketAddress address) {
        return sessionHandlerMap_.remove(address);
    }
}
