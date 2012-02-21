/*
 * SampleClient.java Version 1.0 Feb 21, 2012
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
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import com.tmax.probus.nio.api.IConnector;
import com.tmax.probus.nio.api.IReactor;
import com.tmax.probus.nio.reactor.Connector;
import com.tmax.probus.nio.util.ByteBufferPool;


/**
 *
 */
public class SampleClient {
    /**
     * Logger for this class
     */
    private final transient Logger logger = Logger.getLogger("com.tmax.probus.nio.example");
    private IConnector connector_;
    private IReactor ioReactor_;
    private Executor executor_ = Executors.newFixedThreadPool(2);
    ByteBufferPool bufferPool_;

    /**
     *
     */
    public SampleClient() {
        ioReactor_ = new SampleIOReactor();
        executor_.execute(ioReactor_);
        connector_ = new SampleConnector(ioReactor_);
        executor_.execute(connector_);
        try {
            bufferPool_ = ByteBufferPool.newPool(10 * 1024 * 1024, 4 * 1024);
        } catch (IOException ex) {
        }
    }

    class SampleConnector extends Connector {
        /**  */
        IReactor ioReactor_;

        /**
         * @param strategy
         * @param sampleServer TODO
         */
        public SampleConnector(IReactor ioReactor) {
            ioReactor_ = ioReactor;
        }

        // (non-Javadoc)
        // @see com.tmax.probus.nio.reactor.AbstractReactor#getIoReactor()
        @Override public IReactor getIoReactor() {
            return ioReactor_;
        }
    }
}
