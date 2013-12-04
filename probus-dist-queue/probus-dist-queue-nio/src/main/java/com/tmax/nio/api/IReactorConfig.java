/*
 * ISelectorManagerConfig.java Version 1.0 May 8, 2013
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


import com.tmax.api.IConfig;


/**
 * The Interface IReactorConfig.
 */
public interface IReactorConfig extends IConfig {
    /**
     * Gets the write selector count.
     * @return the write selector count
     */
    int getWriteSelectorCount();

    /**
     * Gets the read selector count.
     * @return the read selector count
     */
    int getReadSelectorCount();

    /**
     * Gets the connect selector count.
     * @return the connect selector count
     */
    int getConnectSelectorCount();

    /**
     * Gets the accept selector count.
     * @return the accept selector count
     */
    int getAcceptSelectorCount();

    /**
     * Gets the accept timeout.
     * @return the accept timeout
     */
    long getAcceptTimeout();

    /**
     * Gets the connect timeout.
     * @return the connect timeout
     */
    long getConnectTimeout();

    /**
     * Gets the read timeout.
     * @return the read timeout
     */
    long getReadTimeout();

    /**
     * Gets the write timeout.
     * @return the write timeout
     */
    long getWriteTimeout();

    /**
     * Gets the accept fail limit.
     * @return the accept fail limit
     */
    int getAcceptFailLimit();

    /**
     * Gets the connect fail limit.
     * @return the connect fail limit
     */
    int getConnectFailLimit();

    /**
     * Gets the read fail limit.
     * @return the read fail limit
     */
    int getReadFailLimit();

    /**
     * Gets the write fail limit.
     * @return the write fail limit
     */
    int getWriteFailLimit();

    /**
     * Sets the write fail limit.
     * @param writeFailLimit the new write fail limit
     */
    void setWriteFailLimit(int writeFailLimit);

    /**
     * Sets the read fail limit.
     * @param readFailLimit the new read fail limit
     */
    void setReadFailLimit(int readFailLimit);

    /**
     * Sets the connect fail limit.
     * @param connectFailLimit the new connect fail limit
     */
    void setConnectFailLimit(int connectFailLimit);

    /**
     * Sets the accept fail limit.
     * @param acceptFailLimit the new accept fail limit
     */
    void setAcceptFailLimit(int acceptFailLimit);

    /**
     * Sets the write timeout.
     * @param writeTimeout the new write timeout
     */
    void setWriteTimeout(long writeTimeout);

    /**
     * Sets the read timeout.
     * @param readTimeout the new read timeout
     */
    void setReadTimeout(long readTimeout);

    /**
     * Sets the connect timeout.
     * @param connectTimeout the new connect timeout
     */
    void setConnectTimeout(long connectTimeout);

    /**
     * Sets the accept timeout.
     * @param acceptTimeout the new accept timeout
     */
    void setAcceptTimeout(long acceptTimeout);

    /**
     * Sets the write selector count.
     * @param writeSelectorCount the new write selector count
     */
    void setWriteSelectorCount(int writeSelectorCount);

    /**
     * Sets the read selector count.
     * @param readSelectorCount the new read selector count
     */
    void setReadSelectorCount(int readSelectorCount);

    /**
     * Sets the connect selector count.
     * @param connectSelectorCount the new connect selector count
     */
    void setConnectSelectorCount(int connectSelectorCount);

    /**
     * Sets the accept selector count.
     * @param acceptSelectorCount the new accept selector count
     */
    void setAcceptSelectorCount(int acceptSelectorCount);
}
