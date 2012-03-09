/*
 * IConnectionEventListener.java Version 1.0 Mar 9, 2012
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


import java.nio.channels.SocketChannel;


/**
 * The listener interface for receiving IConnectionEvent events. The class that
 * is interested in processing a IConnectionEvent event implements this
 * interface, and the object created with that class is registered with a
 * component using the component's
 * <code>addIConnectionEventListener<code> method. When
 * the IConnectionEvent event occurs, that object's appropriate
 * method is invoked.
 * @see IConnectionEventEvent
 */
public interface IConnectionEventListener {
    /**
     * Event connection closed.
     * @param reactor the reactor
     * @param channel the channel
     */
    void eventConnectionClosed(IReactor reactor, SocketChannel channel);

    /**
     * @param abstractReactor
     * @param channel
     */
    void eventConnectionConnected(IReactor reactor, SocketChannel channel);
}
