/*
 * ISession.java Version 1.0 Feb 24, 2012
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


import java.nio.ByteBuffer;
import java.util.Queue;


/**
 * The Interface ISession.
 */
public interface ISession {
    /**
     * Acquire read buffer.
     * @return the byte buffer
     */
    ByteBuffer acquireReadBuffer();

    /**
     * On message read.
     * @return true, if on message read
     */
    boolean onMessageRead();

    /**
     * Release read buffer.
     */
    void releaseReadBuffer();

    /**
     *
     */
    void releaseWriteQueue();

    /**
     *
     * @return
     */
    Queue<ByteBuffer> acquireWriteQueue();
}
