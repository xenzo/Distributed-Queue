/*
 * SimpleConnector.java Version 1.0 Feb 9, 2012
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
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.logging.Logger;

import com.tmax.probus.nio.api.IConnector;
import com.tmax.probus.nio.api.ISession;


/**
 * The Class Connector.
 */
public class Connector extends AbstractIoReactor implements IConnector {
    /** Logger for this class. */
    private final transient Logger logger = Logger.getLogger("com.tmax.probus.nio.client");
    /** The connect selector_. */
    private Selector connectSelector_;

    // (non-Javadoc)
    // @see com.tmax.probus.nio.client.Connector#connect()
    @Override public SocketChannel connect(final InetSocketAddress remoteAddr, final InetAddress localAddr, final boolean isBlocking) {
        if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "connect");
        SocketChannel socketChannel = null;
        try {
            socketChannel = SocketChannel.open();
            socketChannel.configureBlocking(isBlocking);
            if (localAddr != null) socketChannel.socket().bind(new InetSocketAddress(localAddr, 0));
            socketChannel.connect(remoteAddr);
            register(socketChannel, SelectionKey.OP_CONNECT);
        } catch (final IOException ex) {
            logger.log(WARNING, "" + ex.getMessage(), ex);
        }
        return socketChannel;
    }

    // (non-Javadoc)
    // @see com.tmax.probus.nio.AbstractReactor#getSelector()
    @Override protected Selector getSelector() {
        return connectSelector_;
    }

    // (non-Javadoc)
    // @see com.tmax.probus.nio.AbstractReactor#handleConnect(java.nio.channels.SelectionKey)
    @Override protected SocketChannel processConnect(final SelectionKey key) {
        if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "handleConnect");
        final SocketChannel channel = (SocketChannel) key.channel();
        try {
            if (channel.finishConnect()) {
                key.interestOps(0);
                initSocket(channel.socket());
                return channel;
            }
        } catch (final IOException ex) {
            logger.log(WARNING, "" + ex.getMessage(), ex);
            return null;
        }
        if (logger.isLoggable(FINER)) logger.exiting(getClass().getName(), "handleConnect");
        return null;
    }

    // (non-Javadoc)
    // @see com.tmax.probus.nio.reactor.AbstractIoReactor#processAccept(java.nio.channels.SelectionKey)
    @Override protected SelectableChannel processAccept(SelectionKey key) {
        throw new UnsupportedOperationException();
    }

    // (non-Javadoc)
    // @see com.tmax.probus.nio.IReactor#init()
    @Override protected void init() {
        super.init();
        try {
            connectSelector_ = SelectorProvider.provider().openSelector();
        } catch (final IOException ex) {
            logger.log(WARNING, "" + ex.getMessage(), ex);
        }
    }

    /**
     * Inits the socket.
     * @param socket the socket
     */
    protected void initSocket(final Socket socket) {
        if (logger.isLoggable(FINER)) logger.entering("Acceptor", "initSocket(Socket=" + socket + ")", "start");
        if (logger.isLoggable(FINER)) logger.exiting("Acceptor", "initSocket(Socket)", "end");
    }

    // (non-Javadoc)
    // @see com.tmax.probus.nio.reactor.AbstractReactor#createSession(java.nio.channels.ServerSocketChannel, java.nio.channels.SocketChannel)
    @Override protected ISession createSession(ServerSocketChannel serverChannel, SocketChannel channel) {
        if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "createSession");
        // XXX must do something
        return null;
    }
}
