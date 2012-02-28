/*
 * SampleClient.java Version 1.0 Feb 21, 2012
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
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import com.tmax.probus.nio.api.IConnector;
import com.tmax.probus.nio.api.IReactor;
import com.tmax.probus.nio.api.ISession;
import com.tmax.probus.nio.api.ISessionReactor;
import com.tmax.probus.nio.reactor.Connector;
import com.tmax.probus.nio.util.ByteBufferPool;


/**
 *
 */
public class SampleClient {
    /**
     * Logger for this class
     */
    private final transient Logger logger = Logger.getLogger("com.tmax.probus.nio.example");
    private IConnector connector_;
    private ISessionReactor ioReactor_;
    private Executor executor_ = Executors.newFixedThreadPool(2);
    ByteBufferPool bufferPool_;

    /**
     *
     */
    public SampleClient() {
        ioReactor_ = new SampleIOReactor();
        connector_ = new SampleConnector();
        try {
            bufferPool_ = ByteBufferPool.newPool(10 * 1024 * 1024, 4 * 1024);
        } catch (IOException ex) {
        }
    }

    public void start() {
        executor_.execute(ioReactor_);
        executor_.execute(connector_);
    }

    public SocketChannel connect(InetSocketAddress address, InetAddress localAddress) {
        return connector_.connect(address, null, false);
    }

    public void send(SocketChannel channel, byte[] msg) {
        ISession session = ioReactor_.getSession(channel);
        session.writeMessage(msg);
    }

    class SampleConnector extends Connector {
        // (non-Javadoc)
        // @see com.tmax.probus.nio.reactor.AbstractReactor#getIoReactor()
        @Override public IReactor getIoReactor() {
            return ioReactor_;
        }

        /** {@inheritDoc} */
        @Override protected ISession createSession(ISessionReactor iReactor, SelectableChannel serverChannel, SocketChannel channel) {
            SampleSession session = new SampleSession(iReactor, channel, bufferPool_);
            session.init();
            ioReactor_.putSession(channel, session);
            return session;
        }
    }

    public static void main(String... args) {
        SampleClient client = new SampleClient();
        client.start();
        InetSocketAddress remoteAddr = new InetSocketAddress("localhost", 8898);
        InetAddress localAddr = null;
        try {
            localAddr = InetAddress.getLocalHost();
        } catch (UnknownHostException ex) {
            ex.printStackTrace();
        }
        SocketChannel channel = client.connect(remoteAddr, localAddr);
        try {
            Thread.sleep(5000);
        } catch (InterruptedException ex) {
        }
        client.send(channel, "00010abcde".getBytes());
        try {
            Thread.sleep(5000);
        } catch (InterruptedException ex) {
        }
    }
}
