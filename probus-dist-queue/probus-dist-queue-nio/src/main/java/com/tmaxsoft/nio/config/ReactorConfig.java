/*
 * SelectorManagerConfig.java Version 1.0 May 8, 2013
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
import com.tmaxsoft.nio.api.IReactorConfig;


/**
 * The Class ReactorConfig.
 */
public class ReactorConfig extends ConfigSupport implements IReactorConfig {
    private int acceptSelectorCount, connectSelectorCount, readSelectorCount, writeSelectorCount;
    private long acceptTimeout, connectTimeout, readTimeout, writeTimeout;
    private int acceptFailLimit, connectFailLimit, readFailLimit, writeFailLimit;

    /**
     * Instantiates a new reactor config.
     */
    public ReactorConfig() {
    }

    /** @InheritDoc */
    @Override public void setAcceptSelectorCount(int acceptSelectorCount) {
        this.acceptSelectorCount = acceptSelectorCount;
    }

    /** @InheritDoc */
    @Override public void setConnectSelectorCount(int connectSelectorCount) {
        this.connectSelectorCount = connectSelectorCount;
    }

    /** @InheritDoc */
    @Override public void setReadSelectorCount(int readSelectorCount) {
        this.readSelectorCount = readSelectorCount;
    }

    /** @InheritDoc */
    @Override public void setWriteSelectorCount(int writeSelectorCount) {
        this.writeSelectorCount = writeSelectorCount;
    }

    /** @InheritDoc */
    @Override public void setAcceptTimeout(long acceptTimeout) {
        this.acceptTimeout = acceptTimeout;
    }

    /** @InheritDoc */
    @Override public void setConnectTimeout(long connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    /** @InheritDoc */
    @Override public void setReadTimeout(long readTimeout) {
        this.readTimeout = readTimeout;
    }

    /** @InheritDoc */
    @Override public void setWriteTimeout(long writeTimeout) {
        this.writeTimeout = writeTimeout;
    }

    /** @InheritDoc */
    @Override public void setAcceptFailLimit(int acceptFailLimit) {
        this.acceptFailLimit = acceptFailLimit;
    }

    /** @InheritDoc */
    @Override public void setConnectFailLimit(int connectFailLimit) {
        this.connectFailLimit = connectFailLimit;
    }

    /** @InheritDoc */
    @Override public void setReadFailLimit(int readFailLimit) {
        this.readFailLimit = readFailLimit;
    }

    /** @InheritDoc */
    @Override public void setWriteFailLimit(int writeFailLimit) {
        this.writeFailLimit = writeFailLimit;
    }

    /** @InheritDoc */
    @Override public int getAcceptSelectorCount() {
        return acceptSelectorCount;
    }

    /** @InheritDoc */
    @Override public int getConnectSelectorCount() {
        return connectSelectorCount;
    }

    /** @InheritDoc */
    @Override public int getReadSelectorCount() {
        return readSelectorCount;
    }

    /** @InheritDoc */
    @Override public int getWriteSelectorCount() {
        return writeSelectorCount;
    }

    /** @InheritDoc */
    @Override public long getAcceptTimeout() {
        return acceptTimeout;
    }

    /** @InheritDoc */
    @Override public long getConnectTimeout() {
        return connectTimeout;
    }

    /** @InheritDoc */
    @Override public long getReadTimeout() {
        return readTimeout;
    }

    /** @InheritDoc */
    @Override public long getWriteTimeout() {
        return writeTimeout;
    }

    /** @InheritDoc */
    @Override public int getAcceptFailLimit() {
        return acceptFailLimit;
    }

    /** @InheritDoc */
    @Override public int getConnectFailLimit() {
        return connectFailLimit;
    }

    /** @InheritDoc */
    @Override public int getReadFailLimit() {
        return readFailLimit;
    }

    /** @InheritDoc */
    @Override public int getWriteFailLimit() {
        return writeFailLimit;
    }
}
