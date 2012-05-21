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
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import com.tmax.probus.nio.api.IConnectionEventListener;
import com.tmax.probus.nio.api.IMessageEventListener;
import com.tmax.probus.nio.api.IMessageIoHandler;
import com.tmax.probus.nio.api.ISession;
import com.tmax.probus.nio.api.IEndPointHandler;
import com.tmax.probus.nio.api.ISessionManager;


/** The Class AbstractSessionReactor. */
public abstract class AbstractSessionReactor extends AbstractReactor implements ISessionManager {
    /** Logger for this class */
    private final transient Logger logger = Logger.getLogger("com.tmax.probus.nio.reactor");
    /** The channel session map */
    private Map<SelectableChannel, ISession> channelSessionMap_;
    /** The session handler map */
    private Map<SocketAddress, IEndPointHandler> sessionHandlerMap_;

    /** {@inheritDoc} */
    @Override public ISession createSession(final SelectableChannel channel) {
        final ISession session = newSession(channel);
        SocketChannel c = (SocketChannel) channel;
        Socket socket = c.socket();
        final IEndPointHandler sessionHandler = sessionHandlerMap_.get(channel);
        try {
            if (sessionHandler != null) sessionHandler.sessionCreated(session);
            putSession(channel, session);
        } catch (final Exception ex) {
            logger.log(WARNING, "" + ex.getMessage(), ex);
        }
        return session;
    }

    /** {@inheritDoc} */
    @Override public void destroy() {
        super.destroy();
        if (channelSessionMap_ != null) {
            final Map<SelectableChannel, ISession> channelSessionMap = channelSessionMap_;
            channelSessionMap_ = null;
            for (ISession session : channelSessionMap.values())
                session.destroy();
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
    @Override public void putSession(final SelectableChannel channel, final ISession session) {
        channelSessionMap_.put(channel, session);
    }

    /** {@inheritDoc} */
    @Override public ISession removeSession(final SelectableChannel channel) {
        return channelSessionMap_.remove(channel);
    }

    /** {@inheritDoc} */
    @Override protected final SocketChannel accept(final SelectionKey key) throws IOException {
        SocketChannel channel = super.accept(key);
        createSession(channel);
        return channel;
    }

    /**
     * Bind.
     * @param localAddr the local addr
     * @return the server socket channel
     * @throws IOException Signals that an I/O exception has occurred.
     */
    protected ServerSocketChannel bind(final InetSocketAddress localAddr, IEndPointHandler sessionHandler)
            throws IOException {
        if (logger.isLoggable(FINER))
            logger.entering(getClass().getName(), "bind(InetSocketAddress=" + localAddr + ")", "start");
        ServerSocketChannel server = null;
        server = ServerSocketChannel.open();
        server.configureBlocking(false);
        server.socket().bind(localAddr);
        sessionHandlerMap_.put(localAddr, sessionHandler);
        getAcceptDispatcher().register(server, SelectionKey.OP_ACCEPT);
        if (logger.isLoggable(FINER))
            logger.exiting(getClass().getName(), "bind(InetSocketAddress)", "end - return value=" + server);
        return server;
    }

    /**
     * Connect.
     * @param remoteAddr the remote addr
     * @param localAddr the local addr
     * @return the i session
     * @throws IOException Signals that an I/O exception has occurred.
     */
    protected ISession connect(final InetSocketAddress remoteAddr, final InetSocketAddress localAddr,
            IEndPointHandler sessionHandler) throws IOException {
        if (logger.isLoggable(FINER)) logger.entering(getClass().getName(),
            "connect(InetSocketAddress=" + remoteAddr + ", InetSocketAddress=" + localAddr + ")", "start");
        SocketChannel channel = SocketChannel.open();
        channel.configureBlocking(false);
        channel.socket().bind(localAddr);
        channel.connect(remoteAddr);
        //
        sessionHandlerMap_.put(remoteAddr, sessionHandler);
        //
        ISession session = createSession(channel);
        putSession(channel, session);
        register(channel, SelectionKey.OP_CONNECT);
        if (logger.isLoggable(FINER))
            logger.exiting(getClass().getName(), "connect(InetSocketAddress, InetSocketAddress)",
                "end - return value=" + session);
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

    /**
     * New session.
     * @param channel the channel
     * @return the i session
     */
    protected ISession newSession(final SelectableChannel channel) {
        return null;
    }
}
