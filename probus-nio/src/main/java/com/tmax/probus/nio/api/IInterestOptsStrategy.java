/*
 * ISelectionStrategy.java Version 1.0 Feb 13, 2012
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
package com.tmax.probus.nio.api;


import java.nio.channels.SelectableChannel;


/**
 * The Interface ISelectionStrategy.
 */
public interface IInterestOptsStrategy {
    /**
     * After accept.
     * @param ioReactor the io reactor
     * @param channel the channel
     * @param attachment the attachment
     */
    void afterAccept(IReactor ioReactor, SelectableChannel channel, Object attachment);

    /**
     * After connect.
     * @param ioReactor the io reactor
     * @param channel the channel
     * @param attachment the attachment
     */
    void afterConnect(IReactor ioReactor, SelectableChannel channel, Object attachment);

    /**
     * After read.
     * @param ioReactor the io reactor
     * @param channel the channel
     * @param attachment the attachment
     */
    void afterRead(IReactor ioReactor, SelectableChannel channel, Object attachment);

    /**
     * After write.
     * @param ioReactor the io reactor
     * @param channel the channel
     * @param attachment the attachment
     */
    void afterWrite(IReactor ioReactor, SelectableChannel channel, Object attachment);
}
