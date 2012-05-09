/*
 * AbstractSession.java Version 1.0 Feb 28, 2012
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


import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import com.tmax.probus.nio.api.IReactor;
import com.tmax.probus.nio.api.ISession;


/**
 * The Class AbstractSession.
 */
public abstract class AbstractSession extends AbstractMessageHandler implements ISession {
    /**
     * Instantiates a new abstract session.
     * @param reactor the reactor
     * @param channel the channel
     */
    public AbstractSession(final IReactor reactor, final SocketChannel channel) {
        super(reactor, channel);
        initSocket();
    }

    /** {@inheritDoc} */
    @Override public void afterAccept(final IReactor reactor) {
        reactor.changeOps(getChannel(), SelectionKey.OP_READ);
    }

    /** {@inheritDoc} */
    @Override public void afterConnect(final IReactor reactor) {
        reactor.changeOps(getChannel(), SelectionKey.OP_WRITE);
    }

    /** {@inheritDoc} */
    @Override public void afterRead(final IReactor reactor) {
        reactor.changeOps(getChannel(), SelectionKey.OP_WRITE);
    }

    /** {@inheritDoc} */
    @Override public void afterWrite(final IReactor reactor) {
        reactor.changeOps(getChannel(), SelectionKey.OP_READ);
    }

    /**
     * Inits the socket.
     */
    abstract protected void initSocket();
}
