/*
 * ISelector.java Version 1.0 Mar 6, 2012
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


/** selector 관련 작업 인터페이스 */
public interface ISelectorOperation {
    /**
     * channel의 interestOpts에 추가한다.
     * @param channel the channel
     * @param opts the opts
     */
    void addOps(SelectableChannel channel, int opts);

    /**
     * channel의 interestOpts을 변경한다.
     * @param channel the channel
     * @param opts the opts
     */
    void changeOps(SelectableChannel channel, int opts);

    /**
     * Close channel.
     * @param channel the channel
     */
    void closeChannel(SelectableChannel channel);

    /**
     * Deregister.
     * @param channel the channel
     */
    void deregister(SelectableChannel channel);

    /**
     * channel을 selector에 인자로 넘어온 interestOpts로 등록한다.
     * @param channel the channel
     * @param opts the opts
     */
    void register(SelectableChannel channel, int opts);

    /**
     * channel의 interestOpts를 해제한다.
     * @param channel the channel
     * @param opts the opts
     */
    void removeOps(SelectableChannel channel, int opts);
}
