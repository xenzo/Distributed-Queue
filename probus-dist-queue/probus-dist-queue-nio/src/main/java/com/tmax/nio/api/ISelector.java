/*
 * ISelector.java Version 1.0 May 1, 2013
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
package com.tmax.nio.api;


import java.nio.channels.SelectableChannel;

import com.tmax.api.IIdentifiable;
import com.tmax.api.ILifeCycle;


/**
 * The Interface ISelector.
 */
public interface ISelector extends ILifeCycle, IIdentifiable {
    void deregister(SelectableChannel channel);

    void turnAcceptOn(SelectableChannel channel);

    void turnConnectOn(SelectableChannel channel);

    void turnReadOn(SelectableChannel channel);

    void turnWriteOn(SelectableChannel channel);

    void turnAcceptOff(SelectableChannel channel);

    void turnConnectOff(SelectableChannel channel);

    void turnReadOff(SelectableChannel channel);

    void turnWriteOff(SelectableChannel channel);

    boolean isRegistered(SelectableChannel channel);

    boolean isAcceptable(SelectableChannel channel);

    boolean isConnectable(SelectableChannel channel);

    boolean isReadable(SelectableChannel channel);

    boolean isWritable(SelectableChannel channel);

    boolean isRegisteredFor(final SelectableChannel channel, final int ops);

    int keyCount();
}
