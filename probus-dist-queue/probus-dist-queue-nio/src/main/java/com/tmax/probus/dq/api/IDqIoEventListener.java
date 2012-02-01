/*
 * IDqEventListener.java Version 1.0 Jan 27, 2012
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


import java.nio.channels.SelectionKey;


/**
 * The listener interface for receiving IDqEvent events. The class that is
 * interested in processing a IDqEvent event implements this interface, and the
 * object created with that class is registered with a component using the
 * component's <code>addIDqEventListener<code> method. When
 * the IDqEvent event occurs, that object's appropriate
 * method is invoked.
 * @see IDqEventEvent
 */
public interface IDqIoEventListener {
    /**
     * Message received.
     * @param key the key
     */
    void messageReceived(final SelectionKey key);

    /**
     * Message sending.
     * @param key the key
     */
    void messageSending(final SelectionKey key);
}
