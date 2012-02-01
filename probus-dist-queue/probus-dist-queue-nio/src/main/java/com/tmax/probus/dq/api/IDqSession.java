/*
 * IDqSession.java Version 1.0 Jan 30, 2012
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
package com.tmax.probus.dq.api;


import java.net.Socket;


/**
 *
 */
public interface IDqSession {
    /**
     * @param socket
     */
    void setSocket(Socket socket);

    /**
     * @param reactor
     */
    void setIoReactor(IDqReactor reactor);
}
