/*
 * IReactor.java Version 1.0 Feb 24, 2012
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
package com.tmax.probus.nio.api;


import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SocketChannel;


/**
 * The Interface IReactor.
 */
public interface IReactor {
    /**
     * Bind.
     * @param localAddr the local addr
     */
    void bind(InetSocketAddress localAddr);

    /**
     * Change opts.
     * @param channel the channel
     * @param opts the opts
     */
    void changeOpts(SelectableChannel channel, int opts);

    /**
     * Connect.
     * @param remoteAddr the remote addr
     * @param localAddr the local addr
     * @return the i session
     */
    ISession connect(InetSocketAddress remoteAddr, InetAddress localAddr);

    /**
     * Deregister.
     * @param channel the channel
     */
    void deregister(SelectableChannel channel);

    /**
     * Gets the accept processor.
     * @return the accept processor
     */
    ISelectorProcessor getAcceptProcessor();

    /**
     * Gets the connect processor.
     * @return the connect processor
     */
    ISelectorProcessor getConnectProcessor();

    /**
     * Gets the read processor.
     * @return the read processor
     */
    ISelectorProcessor getReadWriteProcessor();

    /**
     * Gets the session.
     * @param channel the channel
     * @return the session
     */
    ISession getSession(SocketChannel channel);

    /**
     * Put session.
     * @param channel the channel
     * @param session the session
     */
    void putSession(SocketChannel channel, ISession session);

    /**
     * Register.
     * @param channel the channel
     * @param opts the opts
     */
    void register(SelectableChannel channel, int opts);

    /**
     * Removes the session.
     * @param channel the channel
     * @return the i session
     */
    ISession removeSession(SocketChannel channel);

    /**
     * Start.
     */
    void start();

    /**
     * Stop.
     */
    void stop();

    /**
     * Unbind.
     * @param localAddr the local addr
     */
    void unbind(InetSocketAddress localAddr);
}
