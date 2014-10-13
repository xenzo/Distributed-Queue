/*
 * IConnectHandler.java Version 1.0 Apr 28, 2013
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
package com.tmaxsoft.nio.api;


import java.nio.channels.SelectionKey;


/**
 *
 */
public interface IConnectHandler {
    /**
     * Handle connect.
     * @param key the key
     */
    void handleConnect(SelectionKey key);
}
