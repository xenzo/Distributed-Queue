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
import java.net.InetAddress;
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

    /** {@inheritDoc} */
    @Override public ISession connectToServer(final InetSocketAddress remoteAddr, final InetAddress localAddr) {
        try {
            ISession session = connect(remoteAddr, localAddr);
        } catch (IOException ex) {
            logger.log(WARNING, "" + ex.getMessage(), ex);
        }
        return null;
    }

    /** {@inheritDoc} */
    @Override public ISelectorProcessor getAcceptProcessor() {
        if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "getAcceptProcessor");
        // XXX must do something
        if (logger.isLoggable(FINER)) logger.exiting(getClass().getName(), "getAcceptProcessor");
        return null;
    }

    /** {@inheritDoc} */
    @Override public ISelectorProcessor getConnectProcessor() {
        if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "getConnectProcessor");
        // XXX must do something
        if (logger.isLoggable(FINER)) logger.exiting(getClass().getName(), "getConnectProcessor");
        return null;
    }

    /** {@inheritDoc} */
    @Override public ISelectorProcessor getReadWriteProcessor() {
        if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "getReadWriteProcessor");
        // XXX must do something
        if (logger.isLoggable(FINER)) logger.exiting(getClass().getName(), "getReadWriteProcessor");
        return null;
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
