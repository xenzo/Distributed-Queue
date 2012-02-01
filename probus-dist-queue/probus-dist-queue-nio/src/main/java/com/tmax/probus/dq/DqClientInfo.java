/*
 * ClientInfo.java Version 1.0 Jan 27, 2012
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
package com.tmax.probus.dq;


import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import com.tmax.probus.dq.api.IDqReactor;
import com.tmax.probus.dq.nio.server.DqIoReactor;


public class DqClientInfo {
    /**  */
    private final DqNode dqNode_;
    /** The host address_. */
    private InetAddress hostAddress_;
    /** The i next_. */
    private int iNext_ = 0;
    /** The io reactor_. */
    private IDqReactor ioReactor_;
    /** The io reactor executor. */
    private Executor ioReactorExecutor;
    /** The io reactors_. */
    private List<IDqReactor> ioReactors_;
    /** The n io reactor_. */
    private int nIoReactor_;
    /** The n worker_. */
    private int nWorker_;
    /** The port_. */
    private int port_;
    /** The timeout_. */
    private long timeout_;
    /** The worker executor_. */
    private final Executor workerExecutor_ = Executors.newFixedThreadPool(nWorker_);

    /**
     * @param dqNode
     */
    DqClientInfo(final DqNode dqNode) {
        dqNode_ = dqNode;
    }

    /**
     * Gets the host address.
     * @return the host address
     */
    public InetAddress getHostAddress() {
        return hostAddress_;
    }

    /**
     * Gets the io reactor.
     * @return the io reactor
     */
    public IDqReactor getIoReactor() {
        final int next = iNext_++;
        if (next < ioReactors_.size()) return ioReactors_.get(next);
        else return getIoReactor();
    }

    /**
     * Gets the io reactor count.
     * @return the io reactor count
     */
    public int getIoReactorCount() {
        return nIoReactor_;
    }

    /**
     * Gets the port.
     * @return the port
     */
    public int getPort() {
        return port_;
    }

    /**
     * Gets the select timeout.
     * @return the select timeout
     */
    public long getSelectTimeout() {
        return timeout_;
    }

    /**
     * Gets the worker count.
     * @return the worker count
     */
    public int getWorkerCount() {
        return nWorker_;
    }

    /**
     * Gets the worker executor.
     * @return the worker executor
     */
    public Executor getWorkerExecutor() {
        return null;
    }

    /**
     * Inits the io selectors.
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private void initIoSelectors() throws IOException {
        final int nReactor = getIoReactorCount();
        ioReactorExecutor = Executors.newFixedThreadPool(nReactor);
        ioReactors_ = new ArrayList<IDqReactor>(nReactor);
        for (int i = 0; i < nReactor; i++) {
            final IDqReactor reactor = new DqIoReactor(dqNode_);
            ioReactors_.add(reactor);
            ioReactorExecutor.execute(reactor);
        }
    }
}
