/*
 * SampleAcceptor.java Version 1.0 Feb 13, 2012
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

import com.tmax.probus.nio.api.IAcceptor;
import com.tmax.probus.nio.api.IReactor;
import com.tmax.probus.nio.util.ByteBufferPool;


/**
 *
 */
public class SampleServer {
    /**
     * Logger for this class
     */
    final transient Logger logger = Logger.getLogger("com.tmax.probus.nio.example");
    private IAcceptor acceptor_;
    private IReactor ioReactor_;
    private Executor executor_ = Executors.newFixedThreadPool(2);
    ByteBufferPool bufferPool_;

    /**
     *
     */
    public SampleServer() {
        ioReactor_ = new SampleIOReactor();
        executor_.execute(ioReactor_);
        acceptor_ = new SampleAcceptor(ioReactor_);
        executor_.execute(acceptor_);
        try {
            bufferPool_ = ByteBufferPool.newPool(10 * 1024 * 1024, 4 * 1024);
        } catch (IOException ex) {
        }
    }
}
