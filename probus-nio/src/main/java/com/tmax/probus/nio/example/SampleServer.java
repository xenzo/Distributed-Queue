/*
 * SampleAcceptor.java Version 1.0 Feb 13, 2012
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
package com.tmax.probus.nio.example;


import static java.util.logging.Level.*;

import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import com.tmax.probus.nio.api.IAcceptor;
import com.tmax.probus.nio.api.IInterestOptsStrategy;
import com.tmax.probus.nio.api.IReactor;


/**
 *
 */
public class SampleServer {
    /**
     * Logger for this class
     */
    final transient Logger logger = Logger.getLogger("com.tmax.probus.nio.example");
    private IAcceptor acceptor_;
    private IReactor ioReactor_;
    private IInterestOptsStrategy strategy_;
    private Executor executor_ = Executors.newFixedThreadPool(2);

    /**
     *
     */
    public SampleServer() {
        strategy_ = new SampleInterestOptsStrategy();
        ioReactor_ = new SampleIOReactor();
        executor_.execute(ioReactor_);
        acceptor_ = new SampleAcceptor(ioReactor_, strategy_);
        executor_.execute(acceptor_);
    }

    class SampleInterestOptsStrategy implements IInterestOptsStrategy {
        // (non-Javadoc)
        // @see com.tmax.probus.nio.IInterestOptsStrategy#afterAccept(com.tmax.probus.nio.IReactor, java.nio.channels.SelectionKey, java.lang.Object)
        @Override public void afterAccept(IReactor ioReactor, SelectableChannel channel, Object attachment) {
            if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "afterAccept");
            ioReactor.register(channel, SelectionKey.OP_READ, attachment);
            if (logger.isLoggable(FINER)) logger.exiting(getClass().getName(), "afterAccept");
        }

        // (non-Javadoc)
        // @see com.tmax.probus.nio.IInterestOptsStrategy#afterConnect(com.tmax.probus.nio.IReactor, java.nio.channels.SelectionKey, java.lang.Object)
        @Override public void afterConnect(IReactor ioReactor, SelectableChannel channel, Object attachment) {
            if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "afterConnect");
            ioReactor.register(channel, SelectionKey.OP_WRITE, attachment);
            if (logger.isLoggable(FINER)) logger.exiting(getClass().getName(), "afterConnect");
        }

        // (non-Javadoc)
        // @see com.tmax.probus.nio.IInterestOptsStrategy#afterRead(com.tmax.probus.nio.IReactor, java.nio.channels.SelectionKey)
        @Override public void afterRead(IReactor ioReactor, SelectableChannel channel, Object attachment) {
            if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "afterRead");
            ioReactor.changeOpts(channel, SelectionKey.OP_WRITE);
            if (logger.isLoggable(FINER)) logger.exiting(getClass().getName(), "afterRead");
        }

        // (non-Javadoc)
        // @see com.tmax.probus.nio.IInterestOptsStrategy#afterWrite(com.tmax.probus.nio.IReactor, java.nio.channels.SelectionKey)
        @Override public void afterWrite(IReactor ioReactor, SelectableChannel channel, Object attachment) {
            if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "afterWrite");
            ioReactor.changeOpts(channel, SelectionKey.OP_READ);
            if (logger.isLoggable(FINER)) logger.exiting(getClass().getName(), "afterWrite");
        }
    }
}
