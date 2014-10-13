/*
 * IDqElement.java Version 1.0 Jan 31, 2012
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

/** DqCollection에서 사용할 인자값을 나타내는 인터페이스이다. */
public interface IDqElement<K> {
    /**
     * Gets the id.
     * @return the id
     */
    K getIdentifier();
}
