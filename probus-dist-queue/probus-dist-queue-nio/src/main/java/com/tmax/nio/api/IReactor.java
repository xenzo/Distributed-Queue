/*
 * ISelectorPool.java Version 1.0 May 1, 2013
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
package com.tmax.nio.api;


import com.tmax.api.ILifeCycle;


/**
 * The Interface IReactor.
 */
public interface IReactor extends ILifeCycle {
    /**
     * Gets the accept selector.
     * @return the accept selector
     */
    public abstract ISelector getAcceptSelector();

    /**
     * Gets the connect selector.
     * @return the connect selector
     */
    public abstract ISelector getConnectSelector();

    /**
     * Gets the read selector.
     * @return the read selector
     */
    public abstract ISelector getReadSelector();

    /**
     * Gets the write selector.
     * @return the write selector
     */
    public abstract ISelector getWriteSelector();
}
