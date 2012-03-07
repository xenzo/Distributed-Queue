/*
 * Connector.java Version 1.0 Mar 1, 2012
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
import java.nio.channels.SocketChannel;
import java.util.logging.Logger;

import com.tmax.probus.nio.api.IConnector;
import com.tmax.probus.nio.api.ISelectorProcessor;
import com.tmax.probus.nio.api.ISession;


/**
 * The Class Connector.
 */
public class Connector extends AbstractReactor implements IConnector {
    /** Logger for this class. */
    private final transient Logger logger = Logger.getLogger("com.tmax.probus.nio.reactor");
    ISelectorProcessor connectProcessor_, readWriteProcessor_;

    /**
     *
     */
    public Connector() {
        setSelectorTimeout(3000);
        connectProcessor_ = createSelectorProcessor("CONNECT_THREAD");
        readWriteProcessor_ = createSelectorProcessor("WRITE_THREAD");
    }

    public static void main(final String... args) {
        final Connector connector = new Connector();
        connector.init();
    }

    /** {@inheritDoc} */
    @Override public ISession connectToServer(final String remoteIp, final int remotePort) {
        return connectToServer(remoteIp, remotePort, "localhost", 0);
    }

    /** {@inheritDoc} */
    @Override public ISession connectToServer(final String remoteIp, final int remotePort, final String localIp) {
        return connectToServer(remoteIp, remotePort, localIp, 0);
    }

    /** {@inheritDoc} */
    @Override public ISession connectToServer(final String remoteIp, final int remotePort, final String localIp, final int localPort) {
        ISession session = null;
        final InetSocketAddress remoteAddr = new InetSocketAddress(remoteIp, remotePort);
        final InetSocketAddress localAddr = new InetSocketAddress(localIp, localPort);
        try {
            session = connect(remoteAddr, localAddr);
        } catch (final IOException ex) {
            logger.log(WARNING, "" + ex.getMessage(), ex);
        }
        return session;
    }

    /** {@inheritDoc} */
    @Override public ISelectorProcessor getAcceptProcessor() {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override public ISelectorProcessor getConnectProcessor() {
        return connectProcessor_;
    }

    /** {@inheritDoc} */
    @Override public ISelectorProcessor getReadWriteProcessor() {
        return readWriteProcessor_;
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
        if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "initSocket");
        // XXX must do something
        if (logger.isLoggable(FINER)) logger.exiting(getClass().getName(), "initSocket");
    }
}
