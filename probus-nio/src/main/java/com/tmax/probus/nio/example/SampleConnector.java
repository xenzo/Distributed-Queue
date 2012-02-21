/*
 * SampleClient.java Version 1.0 Feb 13, 2012
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


import com.tmax.probus.nio.api.IReactor;
import com.tmax.probus.nio.reactor.Connector;


/**
 *
 */
public class SampleConnector extends Connector {
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
