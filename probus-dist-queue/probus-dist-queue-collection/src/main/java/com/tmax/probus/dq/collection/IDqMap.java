/*
 * IDqMap.java Version 1.0 Feb 2, 2012
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

/**
 * The Interface IDqMap.
 * @param <K> the key type
 * @param <V> the value type
 */
public interface IDqMap<K, V> {
    /**
     * Gets the.
     * @param key the key
     * @return the v
     */
    V get(K key);

    /**
     * Put.
     * @param key the key
     * @param value the value
     * @return the v
     */
    void put(K key, V value);
}
