/*
 * DqIoEventListener.java Version 1.0 Jan 27, 2012
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
package com.tmaxsoft.probus.dq.nio.server;


import java.nio.channels.SelectionKey;
import java.util.concurrent.Executor;
import java.util.logging.Logger;

import com.tmaxsoft.probus.dq.DqNode;
import com.tmaxsoft.probus.dq.nio.AbstractDqIoEventListener;


/**
 *
 */
public class DqIoEventListener extends AbstractDqIoEventListener {
    /**
     * Logger for this class
     */
    private final transient Logger logger = Logger.getLogger("com.tmaxsoft.probus.dq.nio");

    /**
     * @param node
     */
    public DqIoEventListener(final DqNode node) {
        super(node);
    }

    // (non-Javadoc)
    // @see com.tmaxsoft.probus.dq.nio.AbstractDqIoEventListener#getWorkerExecutor()
    @Override public Executor getWorkerExecutor() {
        return getNode().serverInfo().getWorkerExecutor();
    }

    // (non-Javadoc)
    // @see com.tmaxsoft.probus.dq.nio.AbstractDqIoEventListener#createReadWorker(java.nio.channels.SelectionKey, java.lang.String, int)
    @Override protected DqReadWorker createReadWorker(final SelectionKey key, String id, final String protocol, final int length) {
        return new DqReadWorker(getNode(), id, key, protocol, length);
    }
}
