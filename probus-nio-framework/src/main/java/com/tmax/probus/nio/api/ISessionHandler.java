/*
 * ISessionHandler.java Version 1.0 May 16, 2012
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
 * The Interface ISessionHandler.
 */
public interface ISessionHandler extends ILifeCycle, ISessionListener {
    /**
     * Adds the session.
     * @param session the session
     */
    void addSession(ISession session);

    /**
     * Removes the session.
     * @param session the session
     * @return the i session
     */
    ISession removeSession(ISession session);
}
