/*
 * ISelectorProcessor.java Version 1.0 Feb 24, 2012
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


import java.io.IOException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;


/** selector 처리자 인터페이스. */
public interface ISelectorDispatcher extends ISelector, ILifeCycle, Runnable {
    /**
     * Handle accept.
     * @return
     */
    SelectableChannel handleAccept(SelectionKey key) throws IOException;

    /**
     * Handle connect.
     * @param key the key
     */
    void handleConnect(SelectionKey key) throws IOException;

    /** Handle read. */
    void handleRead(SelectionKey key) throws IOException;

    /** Handle write. */
    void handleWrite(SelectionKey key) throws IOException;

    /** Wakeup selector. */
    void wakeupSelector();
}
