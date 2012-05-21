/*
 * IReactor.java Version 1.0 Feb 24, 2012
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


/** Reactor 인터페이스. */
public interface IReactor extends ISelectorOperation, ILifeCycle {
    /**
     * Accept 이벤트를 처리할 ISelectorProcessor객체를 반환한다.
     * @param channel the channel
     * @return the accept processor
     */
    ISelectorDispatcher getAcceptDispatcher(SelectableChannel channel);

    /**
     * Connect 이벤트를 처리할 ISelectorProcessor객체를 반환한다.
     * @param channel the channel
     * @return the connect processor
     */
    ISelectorDispatcher getConnectDispatcher(SelectableChannel channel);

    /**
     * Read이벤트를 처리할 ISelectorProcessor객체를 반환한다.
     * @param channel the channel
     * @return the read processor
     */
    ISelectorDispatcher getReadDispatcher(SelectableChannel channel);

    /**
     * Write이벤트를 처리할 ISelectorProcessor객체를 반환한다.
     * @param channel the channel
     * @return the read processor
     */
    ISelectorDispatcher getWriteDispatcher(SelectableChannel channel);
}
