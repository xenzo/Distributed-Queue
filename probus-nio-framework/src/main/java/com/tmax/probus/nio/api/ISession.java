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
     * Acquire write queue.
     * @return the queue
     */
    Queue<ByteBuffer> acquireWriteQueue();

    /**
     * After accept.
     * @param reactor the reactor
     */
    void afterAccept(IReactor reactor);

    /**
     * After connect.
     * @param reactor the reactor
     */
    void afterConnect(IReactor reactor);

    /**
     * After read.
     * @param reactor the reactor
     */
    void afterRead(IReactor reactor);

    /**
     * After write.
     * @param reactor the reactor
     */
    void afterWrite(IReactor reactor);

    /**
     * Destroy.
     */
    void destroy();

    /**
     * Inits the.
     */
    void init();

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
     * Release write queue.
     */
    void releaseWriteQueue();

    /**
     * Sets the message reader.
     * @param reader the new message reader
     */
    void setMessageReader(IMessageReader reader);

    /**
     * Write.
     * @param msg the msg
     */
    void write(byte[] msg);

    /**
     * Write.
     * @param msg the msg
     * @param offset the offset
     * @param length the length
     */
    void write(byte[] msg, int offset, int length);
}
