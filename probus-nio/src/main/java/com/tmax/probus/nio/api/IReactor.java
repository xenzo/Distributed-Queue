/*
 * IReactor.java Version 1.0 Feb 9, 2012
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


import java.io.IOException;
import java.nio.channels.SelectableChannel;


/**
 * The Interface IReactor.
 */
public interface IReactor extends Runnable {
    /**
     * Adds the pending job.
     * @param runnable the runnable
     */
    void addPendingJob(Runnable runnable);

    /**
     * Register.
     * @param channel the channel
     * @param op the op
     * @throws IOException Signals that an I/O exception has occurred.
     */
    void register(SelectableChannel channel, int opts);

    /**
     * Register.
     * @param channel the channel
     * @param op the op
     * @param attachment the attachment
     * @throws IOException Signals that an I/O exception has occurred.
     */
    void register(SelectableChannel channel, int opts, Object attachment);

    /**
     * Deregister.
     * @param channel the channel
     * @throws IOException Signals that an I/O exception has occurred.
     */
    void deregister(SelectableChannel channel);

    /**
     * Stop.
     */
    void stop();

    /**
     * Wakeup.
     */
    void wakeup();

    void changeOpts(final SelectableChannel channel, final int opts);
}
