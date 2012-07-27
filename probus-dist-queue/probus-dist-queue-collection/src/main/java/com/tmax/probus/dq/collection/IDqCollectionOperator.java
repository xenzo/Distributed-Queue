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


import java.util.concurrent.TimeUnit;


/**
 * DqCollections에서 물리적으로 실제 데이터를 삭제하거나 사용할 수 있도록 하는 인터페이스이다.
 * @param <K> the key type
 * @param <V> the value type
 */
public interface IDqCollectionOperator<K, V> extends IDqSolidOperator<K, V> {
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
     * Size.
     * @return the int
     */
    int size();

    /**
     * Unlink first.
     * @return the v
     */
    V unlinkFirst();

    /**
     * Unlink first.
     * @param timeout the timeout
     * @param unit the unit
     * @return the v
     * @throws InterruptedException the interrupted exception
     */
    V unlinkFirst(final long timeout, final TimeUnit unit) throws InterruptedException;

    /**
     * Take first.
     * @return the v
     * @throws InterruptedException the interrupted exception
     */
    V takeFirst() throws InterruptedException;

    /**
     * Unlink last.
     * @return the v
     */
    V unlinkLast();

    /**
     * Unlink last.
     * @param timeout the timeout
     * @param unit the unit
     * @return the v
     * @throws InterruptedException the interrupted exception
     */
    V unlinkLast(final long timeout, final TimeUnit unit) throws InterruptedException;

    /**
     * Take last.
     * @return the v
     * @throws InterruptedException the interrupted exception
     */
    V takeLast() throws InterruptedException;
}
