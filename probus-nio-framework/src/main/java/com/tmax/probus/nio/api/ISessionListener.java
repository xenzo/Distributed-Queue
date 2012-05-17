/*
 * IEndPoint.java Version 1.0 May 14, 2012
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
 * The listener interface for receiving ISession events. The class that is
 * interested in processing a ISession event implements this interface, and the
 * object created with that class is registered with a component using the
 * component's <code>addISessionListener<code> method. When
 * the ISession event occurs, that object's appropriate
 * method is invoked.
 * @see ISessionEvent
 */
public interface ISessionListener {
    /**
     * Invoked when session is created.
     * @param session the session
     * @throws Exception the exception
     */
    void sessionCreated(ISession session) throws Exception;

    /**
     * Session destroyed.
     * @param session the session
     * @throws Exception the exception
     */
    void sessionDestroyed(ISession session) throws Exception;

    /**
     * Session opened.
     * @param session the session
     * @throws Exception the exception
     */
    void sessionOpened(ISession session) throws Exception;

    /**
     * Session closed.
     * @param session the session
     * @throws Exception the exception
     */
    void sessionClosed(ISession session) throws Exception;
}
