/*
 * DqReadWorker.java Version 1.0 Jan 27, 2012
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
package com.tmax.probus.dq.nio.server;


import static java.util.logging.Level.*;

import java.nio.channels.SelectionKey;
import java.util.logging.Logger;

import com.tmax.probus.dq.DqNode;
import com.tmax.probus.dq.nio.AbstractDqReadWorker;


/**
 *
 */
public class DqReadWorker extends AbstractDqReadWorker {
    /**
     * Logger for this class
     */
    private final transient Logger logger = Logger.getLogger("com.tmax.probus.dq.nio");

    /**
     * @param id
     * @param key
     * @param protocol
     * @param length
     */
    public DqReadWorker(final DqNode node, String id, final SelectionKey key, final String protocol, final int length) {
        super(node, id, key, protocol, length);
    }

    // (non-Javadoc)
    // @see com.tmax.probus.dq.nio.IDqWorker#process()
    @Override public void process() {
        if (logger.isLoggable(INFO)) logger.logp(INFO, getClass().getName(), "process", "", new Object[] {});
    }
}
