/*
 * SessionSupportedReactor.java Version 1.0 May 1, 2013
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
package com.tmax.nio;


import static java.nio.channels.SelectionKey.*;
import static java.util.logging.Level.*;

import java.io.IOException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ExecutorService;

import com.tmax.nio.api.IChannelSessionManager;
import com.tmax.nio.api.ISelector;
import com.tmax.nio.api.ISelectorConfig;
import com.tmax.nio.api.ISelectorCreator;
import com.tmax.nio.api.ISession;


/**
 * The Class SessionSupportedReactor.
 */
abstract public class SessionSupportedReactor extends AbstractReactor
        implements IChannelSessionManager, ISelectorCreator {
    /** The _session map. */
    private final Map<SelectableChannel, ISession> _sessionMap = new ConcurrentSkipListMap<SelectableChannel, ISession>();

    /** @InheritDoc */
    @Override public ISession getSession(final SelectableChannel aChannel) {
        return getSessionMap().get(aChannel);
    }

    /** @InheritDoc */
    @Override public ISession putSession(final SelectableChannel channel, final ISession session) {
        return getSessionMap().put(channel, session);
    }

    /** @InheritDoc */
    @Override public ISession removeSession(final SelectableChannel channel) {
        return getSessionMap().remove(channel);
    }

    /**
     * Gets the session map.
     * @return the sessionMap
     */
    protected final Map<SelectableChannel, ISession> getSessionMap() {
        return _sessionMap;
    }

    /** @InheritDoc */
    @Override public final ISelector newSelector(final String id, final ExecutorService executor, final ISelectorConfig config) {
        return new SelectDispatcher(id, executor, config);
    }

    /**
     * On connection closed.
     * @param session the session
     */
    protected void onConnectionClosed(final ISession session) {
    }

    /**
     * On connection connected.
     * @param session the session
     */
    protected void onConnectionConnected(final ISession session) {
    }

    /** @InheritDoc */
    @Override protected final void onConnectionClosed(final SelectableChannel channel) {
        onConnectionClosed(getSession(channel));
    }

    /** @InheritDoc */
    @Override protected void handleAccept(final ISelector selector, final ServerSocketChannel server)
            throws IOException {
        if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "processAccept");
        final SocketChannel channel = server.accept();
        ISession session = getSession(channel);
        if (session == null) session = newSession(selector, channel, server);
        if (_isConnected(channel)) onConnectionConnected(session);
        session.doAccept();
        if (logger.isLoggable(FINER)) logger.exiting(getClass().getName(), "processAccept");
    }

    /** @InheritDoc */
    @Override protected void handleConnect(final ISelector selector, final SelectableChannel channel)
            throws IOException {
        if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "processConnect");
        ISession session = getSession(channel);
        if (session == null) session = newSession(selector, channel, null);
        if (_isConnected(channel)) onConnectionConnected(session);
        session.doConnect();
        if (logger.isLoggable(FINER)) logger.exiting(getClass().getName(), "processConnect");
    }

    /**
     * New session.
     * @param selector the selector
     * @param channel the channel
     * @param server the server
     * @return the i session
     */
    abstract protected ISession newSession(ISelector selector, SelectableChannel channel, ServerSocketChannel server);

    /** @InheritDoc */
    @Override protected void handleRead(final ISelector selector, final SelectableChannel channel) throws IOException {
        if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "handleRead");
        final ISession session = getSession(channel);
        if (session == null) throw new NullPointerException();
        session.doRead();
        if (logger.isLoggable(FINER)) logger.exiting(getClass().getName(), "handleRead");
    }

    /** @InheritDoc */
    @Override protected void handleWrite(final ISelector selector, final SelectableChannel channel) throws IOException {
        if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "handleWrite");
        final ISession session = getSession(channel);
        if (session == null) throw new NullPointerException();
        session.doWrite();
        if (logger.isLoggable(FINER)) logger.exiting(getClass().getName(), "handleWrite");
    }

    /**
     * Checks if is connected.
     * @param channel the channel
     * @return true, if is connected
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private final boolean _isConnected(final SelectableChannel channel) throws IOException {
        return channel != null && ((SocketChannel) channel).finishConnect();
    }

    /**
     * The Class SessionSelectorDispatcher.
     */
    protected class SelectDispatcher extends AbstractSelectDispatcher {
        private final String _id;
        private final ISelectorConfig _config;

        /**
         * Instantiates a new session selector dispatcher.
         * @param dispatchExecutor the dispatch executor
         */
        protected SelectDispatcher(final String id, final ExecutorService dispatchExecutor, final ISelectorConfig config) {
            super(dispatchExecutor);
            if (config == null) throw new NullPointerException();
            _id = id;
            _config = config;
        }

        /** @InheritDoc */
        @Override public final String getId() {
            return _id;
        }

        protected final ISelectorConfig getConfig() {
            return _config;
        }

        /** @InheritDoc */
        @Override public final void deregister(final SelectableChannel channel) {
            if (isRegistered(channel)) deregisterChannel(channel);
        }

        /** @InheritDoc */
        @Override public final boolean isAcceptable(final SelectableChannel channel) {
            return isRegisteredFor(channel, OP_ACCEPT);
        }

        /** @InheritDoc */
        @Override public final boolean isConnectable(final SelectableChannel channel) {
            return isRegisteredFor(channel, OP_CONNECT);
        }

        /** @InheritDoc */
        @Override public final boolean isReadable(final SelectableChannel channel) {
            return isRegisteredFor(channel, OP_READ);
        }

        /** @InheritDoc */
        @Override public final boolean isWritable(final SelectableChannel channel) {
            return isRegisteredFor(channel, OP_WRITE);
        }

        /** @InheritDoc */
        @Override public final void turnAcceptOn(final SelectableChannel channel) {
            _addOrRegister(channel, OP_ACCEPT);
        }

        /** @InheritDoc */
        @Override public final void turnConnectOn(final SelectableChannel channel) {
            _addOrRegister(channel, OP_CONNECT);
        }

        /** @InheritDoc */
        @Override public final void turnReadOn(final SelectableChannel channel) {
            _addOrRegister(channel, OP_READ);
        }

        /** @InheritDoc */
        @Override public final void turnWriteOn(final SelectableChannel channel) {
            _addOrRegister(channel, OP_WRITE);
        }

        /**
         * add or regist.
         * @param channel the channel
         * @param ops the ops
         */
        private final void _addOrRegister(final SelectableChannel channel, final int ops) {
            if (!isRegistered(channel)) registerChannel(channel, ops);
            else if (isRegisteredFor(channel, ops)) addOps(channel, ops);
        }

        /** @InheritDoc */
        @Override public final void turnAcceptOff(final SelectableChannel channel) {
            if (isAcceptable(channel)) removeOps(channel, OP_ACCEPT);
        }

        /** @InheritDoc */
        @Override public final void turnConnectOff(final SelectableChannel channel) {
            if (isConnectable(channel)) removeOps(channel, OP_CONNECT);
        }

        /** @InheritDoc */
        @Override public final void turnReadOff(final SelectableChannel channel) {
            if (isReadable(channel)) removeOps(channel, OP_READ);
        }

        /** @InheritDoc */
        @Override public final void turnWriteOff(final SelectableChannel channel) {
            if (isWritable(channel)) removeOps(channel, OP_WRITE);
        }

        /** @InheritDoc */
        @Override protected int getSelectorFailLimit() {
            return getConfig().getSelectorFailLimit();
        }

        /** @InheritDoc */
        @Override protected long getSelectorTimeOut() {
            return getConfig().getSelectorTimeOut();
        }
    }
}
