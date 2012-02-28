/*
 * SampleIOReactor.java Version 1.0 Feb 14, 2012
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
import java.nio.channels.SelectableChannel;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.tmax.probus.nio.api.ISession;
import com.tmax.probus.nio.api.ISessionReactor;
import com.tmax.probus.nio.reactor.AbstractIoReactor;


/**
 * The Class SampleIOReactor.
 */
public class SampleIOReactor extends AbstractIoReactor {
    /** The io selector_. */
    Selector ioSelector_;
    /** The session map_. */
    Map<SelectableChannel, ISession> sessionMap_ = new ConcurrentHashMap<SelectableChannel, ISession>();

    /**
     * Instantiates a new sample io reactor.
     */
    public SampleIOReactor() {
        try {
            ioSelector_ = SelectorProvider.provider().openSelector();
        } catch (IOException ex) {
        }
    }

    /** {@inheritDoc} */
    @Override protected Selector getSelector() {
        return ioSelector_;
    }

    /** {@inheritDoc} */
    public ISession getSession(SelectableChannel channel) {
        return sessionMap_.get(channel);
    }

    /** {@inheritDoc} */
    @Override protected ISession createSession(ISessionReactor iReactor, SelectableChannel serverChannel, SocketChannel channel) {
        ISession session = new SampleSession(iReactor, channel, null);
        putSession(channel, session);
        return session;
    }

    /** {@inheritDoc} */
    @Override public void putSession(SelectableChannel channel, ISession session) {
        sessionMap_.put(channel, session);
    }

    /** {@inheritDoc} */
    @Override public ISession removeSession(SelectableChannel channel) {
        return sessionMap_.remove(channel);
    }
}
