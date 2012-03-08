/*
 * Acceptor.java Version 1.0 Feb 25, 2012
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
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import com.tmax.probus.nio.api.IAcceptor;
import com.tmax.probus.nio.api.ISelectorDispatcher;
import com.tmax.probus.nio.api.ISession;


/**
 * The Class Acceptor.
 */
public class Acceptor extends AbstractReactor implements IAcceptor {
    /** Logger for this class. */
    private final transient Logger logger = Logger.getLogger("com.tmax.probus.nio.reactor");
    /** The read write processor_. */
    private final ISelectorDispatcher acceptProcessor_, readWriteProcessor_;
    /** The server socket channel map_. */
    private Map<InetSocketAddress, ServerSocketChannel> serverSocketChannelMap_ = new ConcurrentHashMap<InetSocketAddress, ServerSocketChannel>();

    /**
     * Instantiates a new acceptor.
     */
    public Acceptor() {
        setSelectorTimeout(3000);
        acceptProcessor_ = createSelectorProcessor("ACCEPT_THREAD");
        readWriteProcessor_ = createSelectorProcessor("READ_THREAD");
    }

    /**
     * The main method.
     * @param args the arguments
     */
    public static void main(final String... args) {
        final Acceptor acceptor = new Acceptor();
        acceptor.init();
        acceptor.openServer(8088);
    }

    /** {@inheritDoc} */
    @Override public void closeServer(final int port) {
        closeServer("localhost", port);
    }

    /** {@inheritDoc} */
    @Override public void closeServer(final String ip, final int port) {
        final InetSocketAddress localAddr = new InetSocketAddress(ip, port);
        final ServerSocketChannel serverSocketChannel = serverSocketChannelMap_.remove(localAddr);
        try {
            if (serverSocketChannel != null) closeChannel(serverSocketChannel);
        } catch (final IOException ex) {
            logger.log(WARNING, "" + ex.getMessage(), ex);
        }
    }

    /** {@inheritDoc} */
    @Override public void destroy() {
        super.destroy();
        final Map<InetSocketAddress, ServerSocketChannel> map = serverSocketChannelMap_;
        serverSocketChannelMap_ = null;
        map.clear();
    }

    /** {@inheritDoc} */
    @Override public ISelectorDispatcher getAcceptDispatcher() {
        return acceptProcessor_;
    }

    /** {@inheritDoc} */
    @Override public ISelectorDispatcher getConnectDispatcher() {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override public ISelectorDispatcher getReadWriteDispatcher() {
        return readWriteProcessor_;
    }

    /** {@inheritDoc} */
    @Override public void openServer(final int port) {
        openServer("localhost", port);
    }

    /** {@inheritDoc} */
    @Override public void openServer(final String ip, final int port) {
        final InetSocketAddress localAddr = new InetSocketAddress(ip, port);
        try {
            final ServerSocketChannel serverSocketChannel = bind(localAddr);
            serverSocketChannelMap_.put(localAddr, serverSocketChannel);
        } catch (final IOException ex) {
            logger.log(WARNING, "" + ex.getMessage(), ex);
        }
    }

    /** {@inheritDoc} */
    @Override protected ISession createSession(final SelectableChannel serverChannel, final SocketChannel channel) {
        if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "createSession");
        // XXX must do something
        if (logger.isLoggable(FINER)) logger.exiting(getClass().getName(), "createSession");
        return null;
    }
}
