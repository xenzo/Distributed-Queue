/*
 * Acceptor.java Version 1.0 Feb 9, 2012
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
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import com.tmax.probus.nio.api.IAcceptor;
import com.tmax.probus.nio.api.ISession;


/**
 * The Class Acceptor.
 */
public class Acceptor extends AbstractIoReactor implements IAcceptor {
    /** The accept selector_. */
    private Selector acceptSelector_;
    /** Logger for this class. */
    private final transient Logger logger = Logger.getLogger("com.tmax.probus.nio.server");
    /** The server socket map_. */
    private Map<SocketAddress, ServerSocketChannel> serverSocketMap_ = new ConcurrentHashMap<SocketAddress, ServerSocketChannel>();

    /** {@inheritDoc} */
    @Override public void bind(final InetSocketAddress localAddr, final boolean isBlocking) {
        if (logger.isLoggable(FINER)) logger.entering("Acceptor", "bind(SocketAddress=" + localAddr + ", boolean=" + isBlocking + ")", "start");
        ServerSocketChannel server;
        try {
            server = ServerSocketChannel.open();
            server.configureBlocking(isBlocking);
            server.socket().bind(localAddr);
            register(server, SelectionKey.OP_ACCEPT);
            serverSocketMap_.put(localAddr, server);
        } catch (final IOException ex) {
            logger.logp(SEVERE, "Acceptor", "bind(SocketAddress, boolean)", "", ex);
        }
        if (logger.isLoggable(FINER)) logger.exiting("Acceptor", "bind(SocketAddress, boolean)", "end");
    }

    /** {@inheritDoc} */
    @Override public void unbind(final InetSocketAddress localAddr) {
        if (logger.isLoggable(FINER)) logger.entering("Acceptor", "unbind(SocketAddress=" + localAddr + ")", "start");
        final ServerSocketChannel serverSocketChannel = serverSocketMap_.get(localAddr);
        try {
            serverSocketChannel.socket().close();
            serverSocketChannel.close();
            deregister(serverSocketChannel);
        } catch (final IOException ex) {
            logger.logp(SEVERE, "Acceptor", "unbind(SocketAddress)", "", ex);
        }
        if (logger.isLoggable(FINER)) logger.exiting("Acceptor", "unbind(SocketAddress)", "end");
    }

    /** {@inheritDoc} */
    @Override protected void destroy() {
        super.destroy();
        final Map<SocketAddress, ServerSocketChannel> socketMap = serverSocketMap_;
        serverSocketMap_ = null;
        socketMap.clear();
    }

    /** {@inheritDoc} */
    @Override protected Selector getSelector() {
        return acceptSelector_;
    }

    /** {@inheritDoc} */
    @Override protected final SocketChannel processAccept(final SelectionKey key) {
        if (logger.isLoggable(FINER)) logger.entering("Acceptor", "handleAccept(SelectionKey=" + key + ")", "start");
        SocketChannel channel;
        try {
            final ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
            channel = serverChannel.accept();
            channel.configureBlocking(serverChannel.isBlocking());
            if (channel.finishConnect()) {
                key.interestOps(0);
                initSocket(channel.socket());
            }
        } catch (final IOException ex) {
            logger.logp(SEVERE, "Acceptor", "handleAccept(SelectionKey)", "", ex);
            return null;
        }
        if (logger.isLoggable(FINER)) logger.exiting("Acceptor", "handleAccept(SelectionKey)", "end");
        return channel;
    }

    /** {@inheritDoc} */
    @Override protected SelectableChannel processConnect(SelectionKey key) {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override protected void init() {
        if (logger.isLoggable(FINER)) logger.entering("Acceptor", "init()", "start");
        super.init();
        try {
            acceptSelector_ = SelectorProvider.provider().openSelector();
        } catch (final IOException ex) {
            logger.logp(SEVERE, "Acceptor", "init()", "", ex);
        }
        if (logger.isLoggable(FINER)) logger.exiting("Acceptor", "init()", "end");
    }

    /**
     * Inits the socket.
     * @param socket the socket
     */
    protected void initSocket(final Socket socket) {
        if (logger.isLoggable(FINER)) logger.entering("Acceptor", "initSocket(Socket=" + socket + ")", "start");
        if (logger.isLoggable(FINER)) logger.exiting("Acceptor", "initSocket(Socket)", "end");
    }

    /** {@inheritDoc} */
    @Override public ISession getSession(SelectableChannel channel) {
        return null;
    }

    /** {@inheritDoc} */
    @Override public void putSession(final SelectableChannel channel, final ISession session) {
    }

    /** {@inheritDoc} */
    @Override public ISession removeSession(final SelectableChannel channel) {
        return null;
    }
}
