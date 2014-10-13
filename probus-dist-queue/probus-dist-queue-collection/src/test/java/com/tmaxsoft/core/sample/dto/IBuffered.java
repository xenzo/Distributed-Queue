/*
 * IBBBDto.java Version 1.0 Apr 23, 2014
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
package com.tmaxsoft.core.sample.dto;


import java.nio.ByteBuffer;


public interface IBuffered {
        long getOffset();

        void setOffset(long offset);

        int getIndex();

        ByteBuffer getData();

        void setIndex(int index);

        final static int INDEX = 4;
        final static int POSITION = 4;
        final static int BASE = INDEX + POSITION;
        final static int LONG = 8;
        final static int INT = 4;
        final static int DOUBLE = 8;
        final static int BOOLEAN = 1;
        final static int CHAR = 1;
        final static int OBJECT = 4;
        final static byte TRUE = 1;
        final static byte FALSE = 0;

        /**
         * @param byteBuffer
         */
        void setData(ByteBuffer byteBuffer);
}//end IBBBDto
