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


import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;

import com.tmax.probus.nio.reactor.AbstractIoReactor;


public class SampleIOReactor extends AbstractIoReactor {
    /**
     * @param strategy
     * @param sampleServer TODO
     */
    public SampleIOReactor() {
    }

    // (non-Javadoc)
    // @see com.tmax.probus.nio.reactor.AbstractReactor#getSelector()
    @Override protected Selector getSelector() {
        return null;
    }

    // (non-Javadoc)
    // @see com.tmax.probus.nio.reactor.AbstractIoReactor#processAccept(java.nio.channels.SelectionKey)
    @Override protected SelectableChannel processAccept(SelectionKey key) {
        return null;
    }

    // (non-Javadoc)
    // @see com.tmax.probus.nio.reactor.AbstractIoReactor#processConnect(java.nio.channels.SelectionKey)
    @Override protected SelectableChannel processConnect(SelectionKey key) {
        return null;
    }
}
