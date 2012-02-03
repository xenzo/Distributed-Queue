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


import java.util.Collection;


/**
 * DqCollections에서 반환하는 Map 인터페이스이다. java.util.Map을 사용하려고 하였으나 메소드의 중복으로 사용빈도가
 * 떨어질것으로 보이는 Map은 자체 정의하였다.
 * @param <K> the key type
 * @param <V> the value type
 */
public interface IDqMap<K, V> extends Collection<V> {
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
