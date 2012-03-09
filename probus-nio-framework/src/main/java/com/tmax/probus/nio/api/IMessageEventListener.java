/*

 *
 * IMessageEventListener.java Version 1.0 Mar 9, 2012
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
package com.tmax.probus.nio.api;

/**
 *
 */
public interface IMessageEventListener {
    /**
     * 하나의 완결된 메세지가 읽어진 경우 호출된다.
     */
    void eventMessageReceived(byte[] msg);
}
