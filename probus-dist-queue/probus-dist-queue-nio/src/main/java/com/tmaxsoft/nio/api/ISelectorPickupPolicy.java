/*
 * IPickSelectorPolicy.java Version 1.0 May 4, 2013
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
package com.tmaxsoft.nio.api;


import java.nio.channels.SelectableChannel;


/**
 * The Interface ISelectorPickingPolicy.
 */
public interface ISelectorPickupPolicy {
    /**
     * Obtain read selector.
     * @return the i selector
     */
    ISelector obtainReadSelector();

    /**
     * Obtain write selector.
     * @return the i selector
     */
    ISelector obtainWriteSelector();

    /**
     * Obtain accept selector.
     * @return the i selector
     */
    ISelector obtainAcceptSelector();

    /**
     * Obtain connect selector.
     * @return the i selector
     */
    ISelector obtainConnectSelector();

    /**
     * Adds the accept selector.
     * @param selector the selector
     */
    void addAcceptSelector(ISelector selector);

    /**
     * Adds the connect selector.
     * @param selector the selector
     */
    void addConnectSelector(ISelector selector);

    /**
     * Adds the read selector.
     * @param selector the selector
     */
    void addReadSelector(ISelector selector);

    /**
     * Adds the write selector.
     * @param selector the selector
     */
    void addWriteSelector(ISelector selector);

    /**
     * Terminate.
     */
    void terminate();

    /**
     * Gets the selector for.
     * @param channel the channel
     * @param op the op
     * @return the selector for
     */
    ISelector searchSelectorFor(SelectableChannel channel, int op);

    /**
     * Removes the selector.
     * @param selector the selector
     * @return true, if successful
     */
    boolean removeSelector(final ISelector selector);
}
