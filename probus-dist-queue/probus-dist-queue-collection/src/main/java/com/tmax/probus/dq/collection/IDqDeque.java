/*
 * IDqDeque.java Version 1.0 Feb 6, 2012
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
import java.util.concurrent.BlockingDeque;


/**
 *
 */
public interface IDqDeque<K, E> extends BlockingDeque<E> {
    /**
     * Eliminate.
     * @param key the key
     * @return the e
     */
    E eliminate(K key);

    /**
     * Gets the expired list.
     * @return the expired list
     */
    List<E> getExpiredList();
}
