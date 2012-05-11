/*
 * IEndPointInfo.java Version 1.0 May 10, 2012
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


import java.net.SocketAddress;


/**
 *
 */
public interface IEndPointInfo {
    /**
     * Gets the target address.
     * @return the target address
     */
    SocketAddress getTargetAddress();

    /**
     * Gets the source address.
     * @return the source address
     */
    SocketAddress getSourceAddress();
}
