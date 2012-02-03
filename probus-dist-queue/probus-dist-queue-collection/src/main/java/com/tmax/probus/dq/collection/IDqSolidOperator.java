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

/**
 * DqCollections에서 물리적으로 실제 데이터를 삭제하거나 사용할 수 있도록 하는 인터페이스이다.
 * @param <K> the key type
 * @param <V> the value type
 */
public interface IDqSolidOperator<K, V> {
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
    V findSolidly(K key);

    /**
     * Removes the item.
     * @param key the key
     * @return the e
     */
    V removeSolidly(K key);

    /**
     * Put solidly.
     * @param key the key
     * @param value the value
     * @return the v
     */
    V putSolidly(K key, V value);
}
