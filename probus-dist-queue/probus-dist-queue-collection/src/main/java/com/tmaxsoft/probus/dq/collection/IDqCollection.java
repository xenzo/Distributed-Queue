/*
 * IDqCollection.java Version 1.0 Jul 26, 2012
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
package com.tmaxsoft.probus.dq.collection;

/**
 *
 */
public interface IDqCollection<K, V> {
    void setOperator(IDqCollectionOperator<K, V> operator);

    IDqCollectionOperator<K, V> getOperator();
}
