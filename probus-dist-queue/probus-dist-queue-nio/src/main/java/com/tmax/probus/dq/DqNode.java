/*
 * DqNode.java Version 1.0 Jan 26, 2012
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
package com.tmax.probus.dq;


import java.util.logging.Logger;

import com.tmax.probus.dq.util.DqBufferPool;


/**
 * The Class DqNode.
 */
public class DqNode {
    /** Logger for this class. */
    private final transient Logger logger = Logger.getLogger("com.tmax.probus.dq");

    public DqClientInfo clientInfo() {
        return new DqClientInfo(this);
    }

    public DqServerInfo serverInfo() {
        return new DqServerInfo(this);
    }

    /**
     * @return
     */
    public DqBufferPool getBufferPool() {
        return null;
    }
}
