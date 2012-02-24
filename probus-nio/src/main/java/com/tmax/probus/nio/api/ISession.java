/*
 * ISession.java Version 1.0 Feb 13, 2012
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
import java.nio.channels.SocketChannel;
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
     * Adds the write message.
     * @param carrier the carrier
     */
    void addWriteMessage(ByteBuffer carrier);

    /**
     * Destroy.
     */
    void destroy();

    /**
     * Gets the channel.
     * @return the channel
     */
    SocketChannel getChannel();

    /**
     * Gets the interest opts strategy.
     * @return the interest opts strategy
     */
    IInterestOptsStrategy getInterestOptsStrategy();

    /**
     * Inits the.
     */
    void init();

    /**
     * On error occured.
     */
    void onErrorOccured();

    /**
     * Process message read.
     * @return true, if successful
     */
    boolean onMessageRead();

    /**
     * Process session closed.
     */
    void onSessionClosed();

    /**
     * On session opened.
     */
    void onSessionOpened();

    /**
     * Process message received.
     * @param message the message
     */
    void processMessageReceived(byte[] message);

    /**
     * Process message sended.
     * @param message the message
     */
    void processMessageSended(byte[] message);

    /**
     * Release read buffer.
     */
    void releaseReadBuffer();

    /**
     * Release write queue.
     */
    void releaseWriteQueue();

    /**
     * Write message.
     * @param msg the msg
     */
    void writeMessage(byte[] msg);

    /**
     * Write message.
     * @param msg the msg
     * @param offset the offset
     * @param length the length
     */
    void writeMessage(byte[] msg, int offset, int length);
}
