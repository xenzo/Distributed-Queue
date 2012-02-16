/*
 * DqSelectorBase.java Version 1.0 Jan 26, 2012
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
package com.tmax.probus.dq.nio;


import static java.util.logging.Level.*;

import java.io.IOException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.spi.SelectorProvider;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Logger;

import com.tmax.probus.dq.api.IDqReactor;
import com.tmax.probus.dq.api.IDqSession;


/**
 * The Class DqSelectorBase.
 */
public abstract class AbstractDqReactor implements IDqReactor {
    /** Logger for this class. */
    private final transient Logger logger = Logger.getLogger("com.tmax.probus.dq.nio");
    /** The selector_. */
    private final Selector selector_;
    /** The pending queue_. */
    private final Queue<Runnable> pendingQueue_ = new ConcurrentLinkedQueue<Runnable>();
    /** The handler_. */
    private final IDqReactorHandler handler_;
    /** The live_. */
    private volatile boolean live_ = true;

    /**
     * Instantiates a new dq selector base.
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public AbstractDqReactor() throws IOException {
        selector_ = SelectorProvider.provider().openSelector();
        handler_ = createSelectionHandler();
    }

    // (non-Javadoc)
    // @see com.tmax.probus.dq.api.IDqReactor#addPendingJob(java.lang.Runnable)
    @Override public void addPendingJob(final Runnable job) {
        pendingQueue_.add(job);
    }

    /**
     * Checks if is live.
     * @return the live
     */
    @Override public boolean isLive() {
        return live_;
    }

    // (non-Javadoc)
    // @see com.tmax.probus.dq.api.IDqReactor#processPendingJobs()
    @Override public void processPendingJobs() {
        //        while (isLive()) {
        final Runnable runnable = pendingQueue_.poll();
        if (runnable != null) runnable.run();
        //        }
    }

    /**
     * Register.
     * @param aChannel the a channel
     * @param aOp the a op
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Override public void register(final SelectableChannel aChannel, final int aOp)
            throws IOException {
        register(aChannel, aOp, null);
    }

    /**
     * Register.
     * @param aChannel the a channel
     * @param aOp the a op
     * @param attach the handler
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Override public void register(final SelectableChannel aChannel, final int aOp, final Object attach)
            throws IOException {
        aChannel.configureBlocking(false);
        aChannel.register(selector_, aOp, attach);
    }

    // (non-Javadoc)
    // @see java.lang.Runnable#run()
    @Override public void run() {
        while (isLive()) {
            processPendingJobs();
            try {
                final int nKey = getSelector().select(getSelectTimeout());
                if (nKey <= 0) continue;
                final Iterator<SelectionKey> selectedKeys = selector_.selectedKeys().iterator();
                while (selectedKeys.hasNext()) {
                    final SelectionKey key = selectedKeys.next();
                    selectedKeys.remove();
                    if (!key.isValid()) continue;
                    key.interestOps(0);
                    if (key.isAcceptable()) handler_.handleAccept(key);
                    else if (key.isConnectable()) handler_.handleConnect(key);
                    else if (key.isReadable()) handler_.handleRead(key);
                    else if (key.isWritable()) handler_.handleWrite(key);
                }
            } catch (final IOException ex) {
                logger.log(WARNING, "" + ex.getMessage(), ex);
            }
        }
    }

    // (non-Javadoc)
    // @see com.tmax.probus.dq.api.IDqReactor#setLive(boolean)
    @Override public void setLive(final boolean live) {
        live_ = live;
    }

    // (non-Javadoc)
    // @see com.tmax.probus.dq.api.IDqReactor#shutdown()
    @Override public void shutdown() {
        if (pendingQueue_ != null) {
            pendingQueue_.clear();
        }
        setLive(false);
    }

    // (non-Javadoc)
    // @see com.tmax.probus.dq.api.IDqReactor#wakeupSelector()
    @Override public void wakeupSelector() {
        selector_.wakeup();
    }

    /**
     * Gets the selector.
     * @return the selector
     */
    protected Selector getSelector() {
        return selector_;
    }

    /**
     * Gets the select timeout.
     * @return the select timeout
     */
    protected abstract long getSelectTimeout();

    /**
     * @return
     */
    protected abstract IDqSession createSession();

    /**
     * @return
     */
    protected abstract IDqReactor getIoReactor();
}
