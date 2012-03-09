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

/**
 * Reactor 인터페이스.
 */
public interface IReactor extends ISelector {
    /**
     * Reactor 종료.
     */
    void destroy();

    /**
     * Accept 이벤트를 처리할 ISelectorProcessor객체를 반환한다.
     * @return the accept processor
     */
    ISelectorDispatcher getAcceptDispatcher();

    /**
     * Connect 이벤트를 처리할 ISelectorProcessor객체를 반환한다.
     * @return the connect processor
     */
    ISelectorDispatcher getConnectDispatcher();

    /**
     * Read/Write 이벤트를 처리할 ISelectorProcessor객체를 반환한다.
     * @return the read processor
     */
    ISelectorDispatcher getReadWriteDispatcher();

    /**
     * Reactor 시동.
     */
    void init();

    /**
     * Start.
     */
    void start();

    /**
     * Stop.
     */
    void stop();
}
