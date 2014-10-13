/*
 * DqWorker.java Version 1.0 Jan 27, 2012
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
import java.util.logging.Logger;

import com.tmaxsoft.probus.dq.DqNode;
import com.tmaxsoft.probus.dq.api.IDqWorker;
import com.tmaxsoft.probus.nio.util.IDqByteBuffer;


/**
 * The Class DqWorker.
 */
public abstract class AbstractDqReadWorker implements IDqWorker {
    /** The channel_. */
    private final SocketChannel channel_;
    /** The key_. */
    private final SelectionKey key_;
    /** The length_. */
    private final int length_;
    /** Logger for this class. */
    private final transient Logger logger = Logger.getLogger("com.tmaxsoft.probus.dq.nio");
    /** The protocol_. */
    private final String protocol_;
    /** The node_. */
    private final DqNode node_;
    /** The id_. */
    private final String id_;

    /**
     * Instantiates a new dq worker.
     * @param node the node
     * @param id the id
     * @param key the key
     * @param protocol the protocol
     * @param length the length
     */
    public AbstractDqReadWorker(final DqNode node, final String id, final SelectionKey key, final String protocol, final int length) {
        node_ = node;
        id_ = id;
        key_ = key;
        channel_ = (SocketChannel) key.channel();
        protocol_ = protocol;
        length_ = length;
        if (length <= 0) throw new IllegalArgumentException();
    }

    /**
     * Gets the data buffer.
     * @return the dataBuffer
     */
    @Override public ByteBuffer getDataBuffer() {
        return null;
    }

    // (non-Javadoc)
    // @see com.tmaxsoft.probus.dq.api.IDqWorker#getId()
    @Override public String getId() {
        return id_;
    }

    /**
     * Gets the key.
     * @return the key
     */
    @Override public SelectionKey getKey() {
        return key_;
    }

    /**
     * Gets the length.
     * @return the length
     */
    @Override public int getLength() {
        return length_;
    }

    /**
     * Gets the protocol.
     * @return the protocol
     */
    @Override public String getProtocol() {
        return protocol_;
    }

    // (non-Javadoc)
    // @see java.lang.Runnable#run()
    @Override public void run() {
        if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "run");
        IDqByteBuffer buffer = null;
        try {
            buffer = node_.getBufferPool().getBuffer();
            buffer.init(length_);
            ByteBuffer dataBuffer_ = buffer.getByteBuffer();
            if (length_ > 0) {
                int nRead = -1;
                try {
                    dataBuffer_.clear();
                    while (nRead >= 0 && dataBuffer_.hasRemaining())
                        nRead = channel_.read(dataBuffer_);
                } catch (final IOException ex) {
                    logger.log(WARNING, "" + ex.getMessage(), ex);
                }
                if (nRead < 0) {
                    try {
                        channel_.close();
                    } catch (final IOException ex) {
                        logger.log(WARNING, "" + ex.getMessage(), ex);
                    }
                    key_.cancel();
                    return;
                }
                process();
            }
        } finally {
            if (buffer != null) buffer.release();
        }
        if (logger.isLoggable(FINER)) logger.exiting(getClass().getName(), "run");
    }
}
