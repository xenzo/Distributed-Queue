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

import java.net.Socket;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SocketChannel;
import java.util.logging.Logger;

import com.tmax.probus.nio.api.ISelectorProcessor;
import com.tmax.probus.nio.api.ISession;


/**
 *
 */
public class Acceptor extends AbstractReactor {
    /**
     * Logger for this class
     */
    private final transient Logger logger = Logger.getLogger("com.tmax.probus.nio.reactor");
    private ISelectorProcessor acceptProcessor_, readWriteProcessor_;

    /**
     *
     */
    public Acceptor() {
        setSelectorTimeout(3000);
        acceptProcessor_ = createSelectorProcessor("ACCEPT_THREAD");
        readWriteProcessor_ = createSelectorProcessor("READ_THREAD");
    }

    /** {@inheritDoc} */
    @Override protected ISession createSession(SelectableChannel serverChannel, SocketChannel channel) {
        if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "createSession");
        // XXX must do something
        if (logger.isLoggable(FINER)) logger.exiting(getClass().getName(), "createSession");
        return null;
    }

    /** {@inheritDoc} */
    @Override public ISelectorProcessor getAcceptProcessor() {
        return acceptProcessor_;
    }

    /** {@inheritDoc} */
    @Override public ISelectorProcessor getConnectProcessor() {
        return null;
    }

    /** {@inheritDoc} */
    @Override public ISelectorProcessor getReadWriteProcessor() {
        return readWriteProcessor_;
    }

    /** {@inheritDoc} */
    @Override protected void initSocket(Socket socket) {
    }

    public static void main(String... args) {
        Acceptor acceptor = new Acceptor();
        acceptor.start();
    }
}
