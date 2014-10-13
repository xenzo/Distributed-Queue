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
package com.tmaxsoft.probus.dq.nio;


import static java.util.logging.Level.*;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.concurrent.Executor;
import java.util.logging.Logger;

import com.tmaxsoft.probus.dq.DqNode;
import com.tmaxsoft.probus.dq.api.IDqIoEventListener;
import com.tmaxsoft.probus.dq.nio.server.DqReadWorker;


/**
 * The listener interface for receiving dqIoEvent events. The class that is
 * interested in processing a dqIoEvent event implements this interface, and the
 * object created with that class is registered with a component using the
 * component's <code>addDqIoEventListener<code> method. When
 * the dqIoEvent event occurs, that object's appropriate
 * method is invoked.
 * @see DqIoEventEvent
 */
public abstract class AbstractDqIoEventListener implements IDqIoEventListener {
    /** The Constant LENGTH_LEN. */
    private static final int LENGTH_LEN = Long.toString(Long.MAX_VALUE).length(); // 10
    /** The Constant PROTOCOL_LEN. */
    private static final int PROTOCOL_LEN = 3;
    private static final int ID_LEN = 20;
    /** The header buffers_. */
    private final ByteBuffer[] headerBuffers_;
    /** The length buffer_. */
    private final ByteBuffer lengthBuffer_;
    /** The protocol buffer_. */
    private final ByteBuffer protocolBuffer_;
    /** The id buffer_. */
    private final ByteBuffer idBuffer_;
    /** Logger for this class. */
    private final transient Logger logger = Logger.getLogger("com.tmaxsoft.probus.dq.nio");
    /** The node_. */
    private final DqNode node_;
    /** The worker executor_. */
    private final Executor workerExecutor_;

    /**
     * Instantiates a new dq io event listener.
     * @param node the node
     */
    public AbstractDqIoEventListener(final DqNode node) {
        node_ = node;
        protocolBuffer_ = ByteBuffer.allocate(PROTOCOL_LEN);
        lengthBuffer_ = ByteBuffer.allocate(LENGTH_LEN);
        idBuffer_ = ByteBuffer.allocate(ID_LEN);
        headerBuffers_ = new ByteBuffer[] { idBuffer_, protocolBuffer_, lengthBuffer_ };
        workerExecutor_ = getWorkerExecutor();
    }

    public abstract Executor getWorkerExecutor();

    // (non-Javadoc)
    // @see com.tmaxsoft.probus.dq.api.IDqEventListener#messageReceived(java.nio.channels.SelectionKey)
    @Override public void messageReceived(final SelectionKey key) {
        final SocketChannel channel = (SocketChannel) key.channel();
        protocolBuffer_.clear();
        lengthBuffer_.clear();
        long nRead = -1;
        try {
            while (nRead >= 0 && (idBuffer_.hasRemaining() || protocolBuffer_.hasRemaining() || lengthBuffer_.hasRemaining())) {
                nRead = channel.read(headerBuffers_);
            }
        } catch (final IOException ex) {
            logger.log(WARNING, "" + ex.getMessage(), ex);
            key.cancel();
            try {
                channel.close();
            } catch (final IOException ex1) {
                logger.log(WARNING, "" + ex1.getMessage(), ex1);
            }
            return;
        }
        if (nRead < 0) return;
        final String id = new String(idBuffer_.array()).trim();
        final String protocol = new String(protocolBuffer_.array()).trim();
        final int length = Integer.parseInt(new String(lengthBuffer_.array()).trim());
        workerExecutor_.execute(createReadWorker(key, id, protocol, length));
    }

    // (non-Javadoc)
    // @see com.tmaxsoft.probus.dq.api.IDqEventListener#messageSending(java.nio.channels.SelectionKey)
    @Override public void messageSending(final SelectionKey key) {
        if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "messageSending");
        // XXX must do something
        if (logger.isLoggable(FINER)) logger.exiting(getClass().getName(), "messageSending");
    }

    /**
     * @param key
     * @param protocol
     * @param length
     * @return
     */
    protected abstract DqReadWorker createReadWorker(final SelectionKey key, String id, final String protocol, final int length);

    /**
     * @return the node
     */
    protected DqNode getNode() {
        return node_;
    }
}
