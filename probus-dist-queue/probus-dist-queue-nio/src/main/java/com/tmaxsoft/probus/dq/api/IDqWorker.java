/*
 * IDqWorker.java Version 1.0 Jan 27, 2012
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
package com.tmaxsoft.probus.dq.api;


import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;


/**
 * The Interface IDqWorker.
 */
public interface IDqWorker extends Runnable {
    /**
     * Gets the data buffer.
     * @return the data buffer
     */
    ByteBuffer getDataBuffer();

    /**
     * Gets the key.
     * @return the key
     */
    SelectionKey getKey();

    /**
     * Gets the length.
     * @return the length
     */
    int getLength();

    /**
     * Gets the protocol.
     * @return the protocol
     */
    String getProtocol();

    /**
     * Process.
     */
    void process();

    /**
     * Gets the id.
     * @return the id
     */
    String getId();
}
