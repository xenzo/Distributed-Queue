/*
 * SampleReactor.java Version 1.0 May 4, 2012
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
package com.tmax.probus.nio.examples;


import static java.util.logging.Level.*;

import java.nio.channels.SelectableChannel;
import java.nio.channels.SocketChannel;
import java.util.logging.Logger;

import com.tmax.probus.nio.api.IMessageHandler;
import com.tmax.probus.nio.reactor.AbstractReactor;


/**
 *
 */
public class SampleReactor extends AbstractReactor {
    /**
     * Logger for this class
     */
    private final transient Logger logger = Logger.getLogger("com.tmax.probus.nio.examples");

    /** {@inheritDoc} */
    @Override protected IMessageHandler getMessageHandler(SocketChannel channel) {
        if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "getMessageHandler");
        // XXX must do something
        if (logger.isLoggable(FINER)) logger.exiting(getClass().getName(), "getMessageHandler");
        return null;
    }

    /** {@inheritDoc} */
    @Override protected void handOffAfterAccept(SocketChannel channel) {
        if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "handOffAfterAccept");
        // XXX must do something
        if (logger.isLoggable(FINER)) logger.exiting(getClass().getName(), "handOffAfterAccept");
    }

    /** {@inheritDoc} */
    @Override protected void handOffAfterConnect(SocketChannel channel) {
        if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "handOffAfterConnect");
        // XXX must do something
        if (logger.isLoggable(FINER)) logger.exiting(getClass().getName(), "handOffAfterConnect");
    }

    /** {@inheritDoc} */
    @Override protected void handOffAfterRead(SocketChannel channel) {
        if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "handOffAfterRead");
        // XXX must do something
        if (logger.isLoggable(FINER)) logger.exiting(getClass().getName(), "handOffAfterRead");
    }

    /** {@inheritDoc} */
    @Override protected void handOffAfterWrite(SocketChannel channel) {
        if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "handOffAfterWrite");
        // XXX must do something
        if (logger.isLoggable(FINER)) logger.exiting(getClass().getName(), "handOffAfterWrite");
    }

    /** {@inheritDoc} */
    @Override protected void onConnectionClosed(SelectableChannel channel) {
        if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "onConnectionClosed");
        // XXX must do something
        if (logger.isLoggable(FINER)) logger.exiting(getClass().getName(), "onConnectionClosed");
    }

    /** {@inheritDoc} */
    @Override protected void onConnectionConnected(SelectableChannel channel) {
        if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "onConnectionConnected");
        // XXX must do something
        if (logger.isLoggable(FINER)) logger.exiting(getClass().getName(), "onConnectionConnected");
    }

    /** {@inheritDoc} */
    @Override protected void onMessageReceived(SocketChannel channel, byte[] msg) {
        if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "onMessageReceived");
        // XXX must do something
        if (logger.isLoggable(FINER)) logger.exiting(getClass().getName(), "onMessageReceived");
    }
}
