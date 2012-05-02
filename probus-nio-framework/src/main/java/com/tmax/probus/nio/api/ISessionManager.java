/*
 * ISessionManager.java Version 1.0 Mar 6, 2012
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
 * 세션관리자
 */
public interface ISessionManager {
    /**
     * 인자로 넘어온 channel에 해당하는 ISession객체를 반환한다.
     * @param channel the channel
     * @return the session
     */
    ISession getSession(SelectableChannel channel);

    /**
     * channel에 해당하는 세션을 저장한다.
     * @param channel the channel
     * @param session the session
     */
    void putSession(SelectableChannel channel, ISession session);

    /**
     * channel에 해당하는 세션을 제거한다.
     * @param channel the channel
     * @return the i session
     */
    ISession removeSession(SelectableChannel channel);
}
