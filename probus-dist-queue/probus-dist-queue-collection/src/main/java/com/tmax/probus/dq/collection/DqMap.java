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
import java.util.Comparator;
import java.util.Map;
import java.util.Set;


/**
 * DqCollections에서 반환하는 Map 인터페이스이다. java.util.Map을 사용하려고 하였으나 메소드의 중복으로 사용빈도가
 * 떨어질것으로 보이는 Map은 자체 정의하였다.
 * @param <K> the key type
 * @param <V> the value type
 */
class DqMap<K, V extends IDqElement<K>> extends DqCollection<K, V> implements Map<K, V> {
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = -8912203139879732827L;

    /**
     * @param id
     * @param listener
     * @param maxSize
     * @param comparator
     */
    DqMap(String id, IDqItemEventListener<V> listener, int maxSize, Comparator<K> comparator) {
        super(id, listener, maxSize, comparator);
    }

    /** {@inheritDoc} */
    @Override
    public boolean isEmpty() {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public boolean containsValue(Object value) {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public V remove(Object key) {
        return remove(key);
    }

    /** {@inheritDoc} */
    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
    }

    /** {@inheritDoc} */
    @Override
    public Set<K> keySet() {
        return keySet();
    }

    /** {@inheritDoc} */
    @Override
    public Collection<V> values() {
        return values();
    }

    /** {@inheritDoc} */
    @Override
    public Set<java.util.Map.Entry<K, V>> entrySet() {
        return entrySet();
    }
}
