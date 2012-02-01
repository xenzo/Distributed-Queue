/*
 * DqBuffer.java Version 1.0 Jan 27, 2012
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
import java.nio.channels.SocketChannel;
import java.util.logging.Logger;


/**
 *
 */
public class DqBuffer {
    /**
     * Logger for this class
     */
    private final transient Logger logger = Logger.getLogger("com.tmax.probus.dq.nio");
    SocketChannel channel;
    ByteBuffer buffer;

    public int read() {
        return 9;
    }
}
