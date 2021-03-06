/*
 * ISelectorConfig.java Version 1.0 May 6, 2013
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


import com.tmaxsoft.api.IConfig;


/**
 * The Interface ISelectorConfig.
 */
public interface ISelectorConfig extends IConfig {
    /**
     * Gets the selector fail limit.
     * @return the selector fail limit
     */
    int getSelectorFailLimit();

    /**
     * Gets the selector time out.
     * @return the selector time out
     */
    long getSelectorTimeOut();
}
