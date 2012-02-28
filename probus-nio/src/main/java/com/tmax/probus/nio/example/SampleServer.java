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


import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import com.tmax.probus.nio.api.IAcceptor;
import com.tmax.probus.nio.api.IReactor;
import com.tmax.probus.nio.api.ISession;
import com.tmax.probus.nio.api.ISessionReactor;
import com.tmax.probus.nio.reactor.Acceptor;
import com.tmax.probus.nio.util.ByteBufferPool;


/**
 *
 */
public class SampleServer {
    /**
     * Logger for this class
     */
    final transient Logger logger = Logger.getLogger("com.tmax.probus.nio.example");
    private IAcceptor acceptor_;
    private ISessionReactor ioReactor_;
    private Executor executor_ = Executors.newFixedThreadPool(2);
    ByteBufferPool bufferPool_;

    /**
     *
     */
    public SampleServer() {
        ioReactor_ = new SampleIOReactor();
        acceptor_ = new SampleAcceptor();
        try {
            bufferPool_ = ByteBufferPool.newPool(10 * 1024 * 1024, 4 * 1024);
        } catch (IOException ex) {
        }
    }

    public void start() {
        executor_.execute(ioReactor_);
        executor_.execute(acceptor_);
    }

    public void listen(InetSocketAddress addr) {
        acceptor_.bind(addr, false);
    }

    class SampleAcceptor extends Acceptor {
        // (non-Javadoc)
        // @see com.tmax.probus.nio.reactor.AbstractReactor#getIoReactor()
        @Override public IReactor getIoReactor() {
            return ioReactor_;
        }

        // (non-Javadoc)
        // @see com.tmax.probus.nio.reactor.Acceptor#createSession(java.nio.channels.SelectableChannel, java.nio.channels.SocketChannel)
        @Override protected ISession createSession(ISessionReactor reactor, SelectableChannel serverChannel, SocketChannel channel) {
            SampleSession session = new SampleSession(reactor, channel, bufferPool_);
            session.init();
            ioReactor_.putSession(channel, session);
            return session;
        }
    }

    public static void main(String... args) {
        SampleServer server = new SampleServer();
        server.start();
        InetSocketAddress addr = new InetSocketAddress(8898);
        server.listen(addr);
    }
}
