/*
 * IRingBuffer.java Version 1.0 May 13, 2014
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
package com.tmaxsoft.collection.common;

/**
 * The Interface IRingBuffer.
 * @param <T> the generic type
 */
public interface IRingBuffer<T> {
        /**
         * Commit.
         * @param seq the seq
         */
        void commit(long seq);

        /**
         * Gets the.
         * @param seq the seq
         * @return the t
         */
        T get(long seq);
}
//end IRingBuffer