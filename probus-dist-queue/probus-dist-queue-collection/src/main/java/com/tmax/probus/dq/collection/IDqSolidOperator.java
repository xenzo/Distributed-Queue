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


import java.util.List;
import java.util.concurrent.TimeUnit;


/**
 * DqCollections에서 물리적으로 실제 데이터를 삭제하거나 사용할 수 있도록 하는 인터페이스이다.
 * @param <K> the key type
 * @param <V> the value type
 */
public interface IDqSolidOperator<K, V> {
    /**
     * DqCollection은 orphan 리스트가 존재할 수도 있기 때문에 그 해결책 중 하나로 논리적으로 head와 tail의
     * 범위에서 벗어난 노드들에 대해 timeout을 적용할 수 있도록 하였다.<br/>
     * timeout된 노드들을 리스트에서 제거하고 그 노드들의 element 리스트를 반환한다.
     * @param timeout the timeout
     * @param unit the unit
     * @return the list
     */
    List<V> clearTimedOutSolidly(long timeout, TimeUnit unit);

    /**
     * Gets the.
     * @param key the key
     * @return the v
     */
    V get(K key);

    /**
     * Gets the first.
     * @return the first
     */
    V getFirst();

    /**
     * 아이디를 반환한다.
     * @return the id
     */
    String getId();

    /**
     * Gets the last.
     * @return the last
     */
    V getLast();

    /**
     * 전체 노드중에서 특정 키를 가진 노드를 찾는다.
     * @param key the key
     * @return the e
     */
    V getSolidly(K key);

    /**
     * DqCollection은 orphan 리스트가 존재할 수도 있기 때문에 그 해결책 중 하나로 논리적으로 head와 tail의
     * 범위에서 벗어난 노드들에 대해 timeout을 적용할 수 있도록 하였다.<br/>
     * timeout된 노드들의 element 리스트를 반환한다.
     * @param timeout the timeout
     * @param unit the unit
     * @return the timed out list
     */
    List<V> getTimedOutSolidly(long timeout, TimeUnit unit);

    /**
     * Link first.
     * @param value the value
     * @return true, if successful
     */
    boolean linkFirst(V value);

    /**
     * Link last.
     * @param value the value
     * @return true, if successful
     */
    boolean linkLast(V value);

    /**
     * Put solidly.
     * @param key the key
     * @param value the value
     * @param putIfAbsent the put if absent
     * @return the v
     */
    V putSolidly(K key, V value, boolean putIfAbsent);

    /**
     * Removes the.
     * @param key the key
     * @return the v
     */
    V remove(K key);

    /**
     * Removes the.
     * @param key the key
     * @param expectValue the expect value
     * @return the v
     */
    V remove(K key, V expectValue);

    /**
     * Removes the item.
     * @param key the key
     * @return the e
     */
    V removeSolidly(K key);

    /**
     * Removes the solidly.
     * @param key the key
     * @param expectValue the expect value
     * @return the v
     */
    V removeSolidly(K key, V expectValue);

    /**
     * Size.
     * @return the int
     */
    int size();

    /**
     * 전체 노드의 갯수.
     * @return the int
     */
    int sizeSolidly();

    /**
     * Unlink first.
     * @return the v
     */
    V unlinkFirst();

    V unlinkFirst(final long timeout, final TimeUnit unit) throws InterruptedException;

    V takeFirst() throws InterruptedException;

    /**
     * Unlink last.
     * @return the v
     */
    V unlinkLast();

    V unlinkLast(final long timeout, final TimeUnit unit) throws InterruptedException;

    V takeLast() throws InterruptedException;
}
