/*
 * IEventListener.java Version 1.0 Feb 21, 2014
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
package com.tmaxsoft.collection;

/**
 *
 */
public interface IEventListener<K, V> {
        enum EventType {
                NEW, INVALID, DISCARD;
        }

        void eventOccurred(EventType type, K key, V value);
}
//end IEventListener