/*
 * IByteBufferCarrier.java Version 1.0 Feb 13, 2012
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


/**
 * The Interface IMessageWrapper.
 */
public interface IMessageWrapper {
    /**
     * Clear.
     */
    void clear();

    /**
     * Compact.
     */
    void compact();

    /**
     * Flip.
     */
    void flip();

    /**
     * Gets the byte buffer.
     * @return the byte buffer
     */
    ByteBuffer getByteBuffer();

    /**
     * Gets the bytes.
     * @return the bytes
     */
    byte[] getBytes();

    /**
     * Checks for remaining.
     * @return true, if successful
     */
    boolean hasRemaining();

    /**
     * Inits the.
     * @param size the size
     */
    void init(int size);

    /**
     * Checks if is valid.
     * @return true, if is valid
     */
    boolean isValid();

    /**
     * Release.
     */
    void release();

    /**
     * Remaining.
     * @return the int
     */
    int remaining();

    /**
     * Rewind.
     */
    void rewind();
}
