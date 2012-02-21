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
import java.nio.channels.Selector;
import java.nio.channels.spi.SelectorProvider;

import com.tmax.probus.nio.reactor.AbstractIoReactor;


public class SampleIOReactor extends AbstractIoReactor {
    Selector ioSelector_;

    /**
     * @param strategy
     * @param sampleServer TODO
     */
    public SampleIOReactor() {
        try {
            ioSelector_ = SelectorProvider.provider().openSelector();
        } catch (IOException ex) {
        }
    }

    // (non-Javadoc)
    // @see com.tmax.probus.nio.reactor.AbstractReactor#getSelector()
    @Override protected Selector getSelector() {
        return ioSelector_;
    }
}
