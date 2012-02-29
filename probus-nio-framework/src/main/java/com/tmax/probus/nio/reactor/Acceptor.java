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
import java.net.Socket;
import java.nio.channels.SelectableChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import com.tmax.probus.nio.api.IAcceptor;
import com.tmax.probus.nio.api.ISelectorProcessor;
import com.tmax.probus.nio.api.ISession;


/**
 *
 */
public class Acceptor extends AbstractReactor implements IAcceptor {
    /**
     * Logger for this class
     */
    private final transient Logger logger = Logger.getLogger("com.tmax.probus.nio.reactor");
    private final ISelectorProcessor acceptProcessor_, readWriteProcessor_;
    private Map<InetSocketAddress, ServerSocketChannel> serverSocketChannelMap_ = new ConcurrentHashMap<InetSocketAddress, ServerSocketChannel>();

    /**
     *
     */
    public Acceptor() {
        setSelectorTimeout(3000);
        acceptProcessor_ = createSelectorProcessor("ACCEPT_THREAD");
        readWriteProcessor_ = createSelectorProcessor("READ_THREAD");
    }

    public static void main(final String... args) {
        final Acceptor acceptor = new Acceptor();
        acceptor.init();
    }

    /** {@inheritDoc} */
    @Override public void destroy() {
        super.destroy();
        Map<InetSocketAddress, ServerSocketChannel> map = serverSocketChannelMap_;
        serverSocketChannelMap_ = null;
        map.clear();
    }

    /** {@inheritDoc} */
    @Override public void closeServer(final InetSocketAddress localAddr) {
        ServerSocketChannel serverSocketChannel = serverSocketChannelMap_.remove(localAddr);
        try {
            if (serverSocketChannel != null) closeChannel(serverSocketChannel);
        } catch (IOException ex) {
            logger.log(WARNING, "" + ex.getMessage(), ex);
        }
    }

    /** {@inheritDoc} */
    @Override public ISelectorProcessor getAcceptProcessor() {
        return acceptProcessor_;
    }

    /** {@inheritDoc} */
    @Override public ISelectorProcessor getConnectProcessor() {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override public ISelectorProcessor getReadWriteProcessor() {
        return readWriteProcessor_;
    }

    /**
     * {@inheritDoc}
     * @param port
     * @param ip
     */
    @Override public void openServer(String ip, int port) {
        try {
            InetSocketAddress localAddr = new InetSocketAddress(ip, port);
            ServerSocketChannel serverSocketChannel = bind(localAddr);
            serverSocketChannelMap_.put(localAddr, serverSocketChannel);
        } catch (IOException ex) {
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

    /** {@inheritDoc} */
    @Override protected void initSocket(final Socket socket) {
    }
}
