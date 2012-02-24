/*
 * ISessionReactor.java Version 1.0 Feb 23, 2012
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


import java.nio.channels.SelectableChannel;


/**
 * The Interface ISessionReactor.
 */
public interface ISessionReactor extends IReactor {
    /**
     * Gets the session.
     * @param channel the channel
     * @return the session
     */
    ISession getSession(SelectableChannel channel);

    /**
     * Put session.
     * @param channel the channel
     * @param session the session
     */
    void putSession(SelectableChannel channel, ISession session);

    /**
     * Removes the session.
     * @param channel the channel
     * @return
     */
    ISession removeSession(SelectableChannel channel);
}
