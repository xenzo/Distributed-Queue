/*
 * SelectorConfig.java Version 1.0 May 6, 2013
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
package com.tmaxsoft.nio.config;


import com.tmaxsoft.ConfigSupport;
import com.tmaxsoft.nio.api.ISelectorConfig;


/**
 * The Class SelectorConfig.
 */
public class SelectorConfig extends ConfigSupport implements ISelectorConfig {
    /** @InheritDoc */
    @Override public int getSelectorFailLimit() {
        return getIntValue("", 10);
    }

    /** @InheritDoc */
    @Override public long getSelectorTimeOut() {
        return getLongValue("", 500L);
    }
}
