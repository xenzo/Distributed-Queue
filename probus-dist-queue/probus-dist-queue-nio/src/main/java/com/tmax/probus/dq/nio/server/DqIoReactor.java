/*
 * DqIoReactor.java Version 1.0 Jan 27, 2012
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
package com.tmax.probus.dq.nio.server;


import static java.util.logging.Level.*;

import java.io.IOException;
import java.net.Socket;
import java.nio.channels.SelectionKey;
import java.util.logging.Logger;

import com.tmax.nio.AbstractReactor;
import com.tmax.nio.api.IReactHandler;
import com.tmax.nio.api.IReactor;
import com.tmax.probus.dq.DqNode;
import com.tmax.probus.dq.api.IDqIoEventListener;
import com.tmax.probus.dq.api.IDqSession;


/**
 *
 */
public class DqIoReactor extends AbstractReactor {
    /**
     * Logger for this class
     */
    private final transient Logger logger = Logger.getLogger("com.tmax.probus.dq.nio");
    /** The listener_. */
    private final IDqIoEventListener listener_;
    /** The node_. */
    private final DqNode node_;

    /**
     * @param node
     * @throws IOException
     */
    public DqIoReactor(final DqNode node) throws IOException {
        node_ = node;
        listener_ = getEventListener();
    }

    // (non-Javadoc)
    // @see com.tmax.probus.dq.nio.DqReactorBase#createSelectionHandler()
    @Override
    public IReactHandler createSelectionHandler() {
        return new ReadHandler();
    }

    /**
     * Gets the node.
     * @return the node
     */
    public DqNode getNode() {
        return node_;
    }

    public void initSocket(final Socket socket) {
        if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "initSocket");
        // XXX must do something
        if (logger.isLoggable(FINER)) logger.exiting(getClass().getName(), "initSocket");
    }

    // (non-Javadoc)
    // @see com.tmax.probus.dq.nio.AbstractDqReactor#createSession()
    @Override
    protected IDqSession createSession() {
        if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "createSession");
        // XXX must do something
        return null;
    }

    // (non-Javadoc)
    // @see com.tmax.probus.dq.nio.AbstractDqIoReactor#getEventListener()
    protected IDqIoEventListener getEventListener() {
        return new DqIoEventListener(getNode());
    }

    // (non-Javadoc)
    // @see com.tmax.probus.dq.nio.AbstractDqReactor#getIoReactor()
    @Override
    protected IReactor getIoReactor() {
        if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "getIoReactor");
        // XXX must do something
        return getNode().clientInfo().getIoReactor();
    }

    // (non-Javadoc)
    // @see com.tmax.probus.dq.nio.DqReactorBase#getSelectTimeout()
    @Override
    protected long getSelectTimeout() {
        return getNode().serverInfo().getSelectTimeout();
    }

    /**
     * The Class ReadHandler.
     */
    private class ReadHandler implements IReactHandler {
        // (non-Javadoc)
        // @see com.tmax.probus.dq.api.IDqReactor.IDqReactorHandler#handleAccept(java.nio.channels.SelectionKey)
        @Override
        public void handleAccept(final SelectionKey key) {
            if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "handleAccept");
            // XXX must do something
            if (logger.isLoggable(FINER)) logger.exiting(getClass().getName(), "handleAccept");
        }

        // (non-Javadoc)
        // @see com.tmax.probus.dq.api.IDqReactor.IDqReactorHandler#handleRead(java.nio.channels.SelectionKey)
        @Override
        public void handleRead(final SelectionKey key) {
            if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "handleRead");
            // XXX must do something
            listener_.messageReceived(key);
            if (logger.isLoggable(FINER)) logger.exiting(getClass().getName(), "handleRead");
        }

        // (non-Javadoc)
        // @see com.tmax.probus.dq.api.IDqReactor.IDqReactorHandler#handleWrite(java.nio.channels.SelectionKey)
        @Override
        public void handleWrite(final SelectionKey key) {
            if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "handleWrite");
            // XXX must do something
            listener_.messageSending(key);
            if (logger.isLoggable(FINER)) logger.exiting(getClass().getName(), "handleWrite");
        }

        // (non-Javadoc)
        // @see com.tmax.probus.dq.api.IDqReactor.IDqReactorHandler#handleConnect(java.nio.channels.SelectionKey)
        @Override
        public void handleConnect(SelectionKey key) {
            if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "handleConnect");
            // XXX must do something
            if (logger.isLoggable(FINER)) logger.exiting(getClass().getName(), "handleConnect");
        }
    }
}
