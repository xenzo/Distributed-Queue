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
public interface ISelectorDispatcher extends ISelectorOperation, ILifeCycle, Runnable {
    /**
     * Handle accept.
     * @param key the key
     * @return the selectable channel
     * @throws IOException Signals that an I/O exception has occurred.
     */
    SelectableChannel handleAccept(SelectionKey key) throws IOException;

    /**
     * Handle connect.
     * @param key the key
     * @return the selectable channel
     * @throws IOException Signals that an I/O exception has occurred.
     */
    SelectableChannel handleConnect(SelectionKey key) throws IOException;

    /**
     * Handle read.
     * @param key the key
     * @return the selectable channel
     * @throws IOException Signals that an I/O exception has occurred.
     */
    SelectableChannel handleRead(SelectionKey key) throws IOException;

    /**
     * Handle write.
     * @param key the key
     * @return the selectable channel
     * @throws IOException Signals that an I/O exception has occurred.
     */
    SelectableChannel handleWrite(SelectionKey key) throws IOException;

    /**
     * Checks if is registed.
     * @param channel the channel
     * @return true, if is registed
     */
    boolean isRegisted(SelectableChannel channel);

    /**
     * Checks if is registed.
     * @param channel the channel
     * @param ops the ops
     * @return true, if is registed
     */
    boolean isRegisted(SelectableChannel channel, int ops);

    /**
     * Checks if is running.
     * @return true, if is running
     */
    boolean isRunning();

    /** Wakeup selector. */
    void wakeupSelector();
}
