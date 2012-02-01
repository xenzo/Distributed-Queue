/*
 * DqBufferPool.java Version 1.0 Jan 27, 2012
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
package com.tmax.probus.dq.util;


import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;


/**
 *
 */
public class DqBufferPool {
    /**
     * Logger for this class
     */
    private final transient Logger logger = Logger.getLogger("com.tmax.probus.dq.nio");
    private final Map<Integer, ByteBuffer> bufferPool_ = new ConcurrentHashMap<Integer, ByteBuffer>();

    /**
     * @param length
     */
    public ByteBuffer getBuffer(final long length) {
        return null;
    }
}
