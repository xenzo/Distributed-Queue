/*
 * SampleServer.java Version 1.0 May 4, 2013
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
package com.tmaxsoft.probus.dq.nio.server;


import java.nio.channels.SelectableChannel;
import java.nio.channels.ServerSocketChannel;
import java.util.logging.Logger;

import com.tmaxsoft.nio.SessionSupportedReactor;
import com.tmaxsoft.nio.api.ISelector;
import com.tmaxsoft.nio.api.ISession;
import static java.util.logging.Level.*;


/**
 * The Class SampleServer.
 */
public class SampleServer extends SessionSupportedReactor {
    /**
     *
     */
    SampleServer() {
        super();
    }

    /** Logger for this class */
    private final transient Logger logger = Logger.getLogger("com.tmaxsoft.probus.dq.nio.server");

    /** @InheritDoc */
    @Override public ISession removeSession(final SelectableChannel channel) {
        return null;
    }

    /** @InheritDoc */
    @Override public ISession putSession(final SelectableChannel channel, final ISession session) {
        return null;
    }

    /** @InheritDoc */
    @Override public ISession getSession(final SelectableChannel aChannel) {
        return null;
    }

    /** @InheritDoc */
    @Override public ISelector getAcceptSelector() {
        return null;
    }

    /** @InheritDoc */
    @Override public ISelector getConnectSelector() {
        return null;
    }

    /** @InheritDoc */
    @Override public ISelector getReadSelector() {
        return null;
    }

    /** @InheritDoc */
    @Override public ISelector getWriteSelector() {
        return null;
    }

    /** @InheritDoc */
    @Override protected int getSelectorFailLimit() {
        return 0;
    }

    /** @InheritDoc */
    @Override protected long getSelectorTimeOut() {
        return 0;
    }

    /** @InheritDoc */
    @Override protected ISession newSession(ISelector selector, SelectableChannel channel, ServerSocketChannel server) {
        return null;
    }

    /** @InheritDoc */
    @Override public void destroy() {
        if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "destroy");
        // XXX must do something
        if (logger.isLoggable(FINER)) logger.exiting(getClass().getName(), "destroy");
    }

    /** @InheritDoc */
    @Override public void init() {
        if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "init");
        // XXX must do something
        if (logger.isLoggable(FINER)) logger.exiting(getClass().getName(), "init");
    }

    /** @InheritDoc */
    @Override public void start() {
        if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "start");
        // XXX must do something
        if (logger.isLoggable(FINER)) logger.exiting(getClass().getName(), "start");
    }

    /** @InheritDoc */
    @Override public void stop() {
        if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "stop");
        // XXX must do something
        if (logger.isLoggable(FINER)) logger.exiting(getClass().getName(), "stop");
    }

    /** @InheritDoc */
    @Override public boolean isRunning() {
        if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "isRunning");
        // XXX must do something
        if (logger.isLoggable(FINER)) logger.exiting(getClass().getName(), "isRunning");
        return false;
    }
}
