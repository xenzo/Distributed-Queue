/*
 * MapProxyInvoker.java Version 1.0 Jul 24, 2012
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
import java.util.NavigableSet;
import java.util.Set;
import java.util.concurrent.ConcurrentNavigableMap;


class MapProxyInvoker<K, E extends IDqElement<K>>
        implements ConcurrentNavigableMap<K, E>, IDqCollection<K, E> {
    /**  */
    private IDqCollectionOperator<K, E> map_;

    /**
     * @param newCollection
     * @param dqCollections TODO
     */
    public MapProxyInvoker(DqCollection<K, E> newCollection) {
        map_ = newCollection;
    }

    /** {@inheritDoc} */
    @Override public void setOperator(IDqCollectionOperator<K, E> operator) {
        map_ = operator;
    }

    /** {@inheritDoc} */
    @Override public IDqCollectionOperator<K, E> getOperator() {
        return map_;
    }

    /** {@inheritDoc} */
    @Override public E putIfAbsent(K key, E value) {
        return map_.putSolidly(key, value, true);
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked") @Override public boolean remove(Object key, Object value) {
        return map_.remove((K) key, (E) value) != null ? true : false;
    }

    /** {@inheritDoc} */
    @Override public E replace(K key, E value) {
        return null;
    }

    /** {@inheritDoc} */
    @Override public boolean replace(K key, E oldValue, E newValue) {
        return false;
    }

    /** {@inheritDoc} */
    @Override public void clear() {
    }

    /** {@inheritDoc} */
    @Override public boolean containsKey(Object key) {
        return false;
    }

    /** {@inheritDoc} */
    @Override public boolean containsValue(Object value) {
        return false;
    }

    /** {@inheritDoc} */
    @Override public Set<java.util.Map.Entry<K, E>> entrySet() {
        return null;
    }

    /** {@inheritDoc} */
    @Override public E get(Object key) {
        return null;
    }

    /** {@inheritDoc} */
    @Override public boolean isEmpty() {
        return false;
    }

    /** {@inheritDoc} */
    @Override public E put(K key, E value) {
        return null;
    }

    /** {@inheritDoc} */
    @Override public void putAll(Map<? extends K, ? extends E> m) {
    }

    /** {@inheritDoc} */
    @Override public E remove(Object key) {
        return null;
    }

    /** {@inheritDoc} */
    @Override public int size() {
        return 0;
    }

    /** {@inheritDoc} */
    @Override public Collection<E> values() {
        return null;
    }

    /** {@inheritDoc} */
    @Override public java.util.Map.Entry<K, E> ceilingEntry(K arg0) {
        return null;
    }

    /** {@inheritDoc} */
    @Override public K ceilingKey(K arg0) {
        return null;
    }

    /** {@inheritDoc} */
    @Override public java.util.Map.Entry<K, E> firstEntry() {
        return null;
    }

    /** {@inheritDoc} */
    @Override public java.util.Map.Entry<K, E> floorEntry(K arg0) {
        return null;
    }

    /** {@inheritDoc} */
    @Override public K floorKey(K arg0) {
        return null;
    }

    /** {@inheritDoc} */
    @Override public java.util.Map.Entry<K, E> higherEntry(K arg0) {
        return null;
    }

    /** {@inheritDoc} */
    @Override public K higherKey(K arg0) {
        return null;
    }

    /** {@inheritDoc} */
    @Override public java.util.Map.Entry<K, E> lastEntry() {
        return null;
    }

    /** {@inheritDoc} */
    @Override public java.util.Map.Entry<K, E> lowerEntry(K arg0) {
        return null;
    }

    /** {@inheritDoc} */
    @Override public K lowerKey(K arg0) {
        return null;
    }

    /** {@inheritDoc} */
    @Override public java.util.Map.Entry<K, E> pollFirstEntry() {
        return null;
    }

    /** {@inheritDoc} */
    @Override public java.util.Map.Entry<K, E> pollLastEntry() {
        return null;
    }

    /** {@inheritDoc} */
    @Override public Comparator<? super K> comparator() {
        return null;
    }

    /** {@inheritDoc} */
    @Override public K firstKey() {
        return null;
    }

    /** {@inheritDoc} */
    @Override public K lastKey() {
        return null;
    }

    /** {@inheritDoc} */
    @Override public NavigableSet<K> descendingKeySet() {
        return null;
    }

    /** {@inheritDoc} */
    @Override public ConcurrentNavigableMap<K, E> descendingMap() {
        return null;
    }

    /** {@inheritDoc} */
    @Override public ConcurrentNavigableMap<K, E> headMap(K toKey) {
        return null;
    }

    /** {@inheritDoc} */
    @Override public ConcurrentNavigableMap<K, E> headMap(K toKey, boolean inclusive) {
        return null;
    }

    /** {@inheritDoc} */
    @Override public NavigableSet<K> keySet() {
        return null;
    }

    /** {@inheritDoc} */
    @Override public NavigableSet<K> navigableKeySet() {
        return null;
    }

    /** {@inheritDoc} */
    @Override public ConcurrentNavigableMap<K, E> subMap(K fromKey, K toKey) {
        return null;
    }

    /** {@inheritDoc} */
    @Override public ConcurrentNavigableMap<K, E> subMap(K fromKey, boolean fromInclusive, K toKey, boolean toInclusive) {
        return null;
    }

    /** {@inheritDoc} */
    @Override public ConcurrentNavigableMap<K, E> tailMap(K fromKey) {
        return null;
    }

    /** {@inheritDoc} */
    @Override public ConcurrentNavigableMap<K, E> tailMap(K fromKey, boolean inclusive) {
        return null;
    }
}
