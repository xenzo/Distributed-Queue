/*
 * BackupQueue.java Version 1.0 Jan 31, 2012
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


import java.lang.reflect.Proxy;
import java.util.concurrent.ConcurrentMap;


/**
 * A factory for creating DqCollection objects.
 * @author Kim, Dong iL
 * @see IDqCollectionOperator
 * @see DqMap
 * @see IDqStack
 * @see IDqElement
 * @since 1.6
 */
public class DqCollections<K, E extends IDqElement<K>> {
    private DqCollection<K, E> newCollection(String id) {
        return new DqCollection<>(id, null, null);
    }

    public ConcurrentMap<K, E> createConcurrentMap() {
        final DqCollection<K, E> collection = newCollection("");
        @SuppressWarnings("unchecked") final ConcurrentMap<K, E> instance =
                (ConcurrentMap<K, E>) Proxy.newProxyInstance(collection.getClass().getClassLoader(),
                    new Class[] { ConcurrentMap.class }, new MapProxyInvoker<K, E>(collection));
        return instance;
    }

    public enum Types {
        BlockingQueue(java.util.concurrent.BlockingQueue.class),
        BlockingDeque(java.util.concurrent.BlockingDeque.class),
        Stack(java.util.Stack.class),
        ConcurrentMap(java.util.concurrent.ConcurrentMap.class),
        ConcurrentNavigableMap(java.util.concurrent.ConcurrentNavigableMap.class),
        SolidOperator(IDqCollectionOperator.class);
        private Types(Class<?> claz) {
            claz_ = claz;
        }

        private Class<?> claz_;

        public Class<?> getClazz() {
            return claz_;
        }
    }
}
