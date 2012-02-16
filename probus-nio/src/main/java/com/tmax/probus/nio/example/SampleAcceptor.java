/*
 * SampleAcceptor.java Version 1.0 Feb 14, 2012
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


import com.tmax.probus.nio.api.IInterestOptsStrategy;
import com.tmax.probus.nio.api.IReactor;
import com.tmax.probus.nio.reactor.Acceptor;


public class SampleAcceptor extends Acceptor {
    /**  */
    IReactor ioReactor_;
    IInterestOptsStrategy strategy_;

    /**
     * @param strategy
     * @param sampleServer TODO
     */
    public SampleAcceptor(IReactor ioReactor, IInterestOptsStrategy strategy) {
        ioReactor_ = ioReactor;
        strategy_ = strategy;
    }

    // (non-Javadoc)
    // @see com.tmax.probus.nio.reactor.AbstractReactor#getIoReactor()
    @Override public IReactor getIoReactor() {
        return ioReactor_;
    }
}
