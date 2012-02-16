/*
 * SampleSession.java Version 1.0 Feb 14, 2012
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


import java.nio.channels.SocketChannel;

import com.tmax.probus.nio.api.IInterestOptsStrategy;
import com.tmax.probus.nio.api.IMessageWrapper;
import com.tmax.probus.nio.reactor.AbstractSession;


class SampleSession extends AbstractSession {
    /**
     * @param channel
     * @param strategy
     * @param readBufferWrapper
     */
    public SampleSession(SocketChannel channel, IInterestOptsStrategy strategy, IMessageWrapper readBufferWrapper) {
        super(channel, strategy, readBufferWrapper);
    }

    // (non-Javadoc)
    // @see com.tmax.probus.nio.api.ISession#onErrorOccured()
    @Override public void onErrorOccured() {
    }

    // (non-Javadoc)
    // @see com.tmax.probus.nio.reactor.AbstractSession#createWriteMessageWrapper(byte[], int, int)
    @Override protected IMessageWrapper createWriteMessageWrapper(byte[] msg, int offset, int length) {
        return null;
    }
}
