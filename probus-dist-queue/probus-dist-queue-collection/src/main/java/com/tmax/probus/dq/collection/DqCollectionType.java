/*
 * DqCollectionType.java Version 1.0 Jul 23, 2012
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
 *
 */
public enum DqCollectionType {
    BlockingQueue(java.util.concurrent.BlockingQueue.class),
    BlockingDeque(java.util.concurrent.BlockingDeque.class),
    Stack(java.util.Stack.class),
    Map(java.util.Map.class),
    ConcurrentMap(java.util.concurrent.ConcurrentMap.class),
    ConcurrentNavigableMap(java.util.concurrent.ConcurrentNavigableMap.class),
    SolidOperator(IDqCollectionOperator.class);
    private DqCollectionType(Class<?> claz) {
        claz_ = claz;
    }

    private Class<?> claz_;

    public Class<?> getClazz() {
        return claz_;
    }
}
