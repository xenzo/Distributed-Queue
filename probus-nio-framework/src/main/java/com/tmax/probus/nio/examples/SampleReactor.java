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

import com.tmax.probus.nio.api.IConnectionEventListener;
import com.tmax.probus.nio.api.IMessageEventListener;
import com.tmax.probus.nio.api.IMessageHandler;
import com.tmax.probus.nio.api.ISelectorDispatcher;
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
    @Override public ISelectorDispatcher getAcceptDispatcher() {
        if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "getAcceptDispatcher");
        // XXX must do something
        if (logger.isLoggable(FINER)) logger.exiting(getClass().getName(), "getAcceptDispatcher");
        return null;
    }

    /** {@inheritDoc} */
    @Override public ISelectorDispatcher getConnectDispatcher() {
        if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "getConnectDispatcher");
        // XXX must do something
        if (logger.isLoggable(FINER)) logger.exiting(getClass().getName(), "getConnectDispatcher");
        return null;
    }

    /** {@inheritDoc} */
    @Override public ISelectorDispatcher getReadDispatcher() {
        if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "getReadDispatcher");
        // XXX must do something
        if (logger.isLoggable(FINER)) logger.exiting(getClass().getName(), "getReadDispatcher");
        return null;
    }

    /** {@inheritDoc} */
    @Override public ISelectorDispatcher getWriteDispatcher() {
        if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "getWriteDispatcher");
        // XXX must do something
        if (logger.isLoggable(FINER)) logger.exiting(getClass().getName(), "getWriteDispatcher");
        return null;
    }

    /** {@inheritDoc} */
    @Override public void addOps(SelectableChannel channel, int opts) {
        if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "addOpts");
        // XXX must do something
        if (logger.isLoggable(FINER)) logger.exiting(getClass().getName(), "addOpts");
    }

    /** {@inheritDoc} */
    @Override protected IConnectionEventListener getConnectionEventListener(SelectableChannel channel) {
        if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "getConnectionEventListener");
        // XXX must do something
        if (logger.isLoggable(FINER)) logger.exiting(getClass().getName(), "getConnectionEventListener");
        return null;
    }

    /** {@inheritDoc} */
    @Override protected IMessageEventListener getMessageEventListener(SocketChannel channel) {
        if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "getMessageEventListener");
        // XXX must do something
        if (logger.isLoggable(FINER)) logger.exiting(getClass().getName(), "getMessageEventListener");
        return null;
    }

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
}
