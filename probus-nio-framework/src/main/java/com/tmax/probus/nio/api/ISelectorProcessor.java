/*
 * ISelectorProcessor.java Version 1.0 Feb 24, 2012
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
 * The Interface ISelectorProcessor.
 */
public interface ISelectorProcessor {
    /**
     * Change opts.
     * @param channel the channel
     * @param opts the opts
     */
    void changeOpts(SelectableChannel channel, int opts);

    /**
     * Deregister.
     * @param channel the channel
     */
    void deregister(SelectableChannel channel);

    /**
     * Checks if is running.
     * @return true, if checks if is running
     */
    boolean isRunning();

    /**
     * Register.
     * @param channel the channel
     * @param opts the opts
     * @param attachment the attachment
     */
    void register(final SelectableChannel channel, final int opts, final Object attachment);

    /**
     * Wakeup.
     */
    void wakeup();

    /**
     *
     */
    void start();
}
