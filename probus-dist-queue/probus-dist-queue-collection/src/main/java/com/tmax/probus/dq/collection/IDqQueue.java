/*
 * IDqQueue.java Version 1.0 Feb 2, 2012
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
package com.tmax.probus.dq.collection;


import java.util.concurrent.BlockingDeque;
import java.util.concurrent.BlockingQueue;


/**
 *
 */
public interface IDqQueue<K, V>
        extends BlockingDeque<V>, BlockingQueue<V>, IDqMap<K, V>, IDqStack<V> {
    /**
     * Full size.
     * @return the int
     */
    int fullSize();

    /**
     * Gets the.
     * @param key the key
     * @return the e
     */
    V findReal(K key);

    /**
     * Removes the item.
     * @param key the key
     * @return the e
     */
    V removeReal(K key);
}
