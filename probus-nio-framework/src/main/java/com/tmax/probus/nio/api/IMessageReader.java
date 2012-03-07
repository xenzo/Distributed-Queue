/*
 * IMessageMaker.java Version 1.0 Feb 15, 2012
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
 * The Interface IMessageReader.
 */
public interface IMessageReader {
    /**
     * Read buffer.
     * @param readBuffer the read buffer
     * @param isEof
     * @return 메세지가 끝까지 읽어들여졌는지의 여부를 반환한다.
     */
    boolean readBuffer(ByteBuffer buffer, boolean isEof);
}
