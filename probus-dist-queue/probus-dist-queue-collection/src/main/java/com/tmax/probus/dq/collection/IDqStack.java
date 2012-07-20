/*
 * IDqStack.java Version 1.0 Feb 3, 2012
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
 * DqCollections에서 반환하는 Stack 구조 인터페이스
 * @param <E> the element type
 */
public interface IDqStack<K, E> extends IDqCollection<K, E> {
    /**
     * Pop.
     * @return the e
     */
    E pop();

    /**
     * Push.
     * @param e the e
     */
    void push(E e);
}
