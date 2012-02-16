/*
 * AbstractMessageMaker.java Version 1.0 Feb 16, 2012
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
package com.tmax.probus.nio.reactor;


import java.nio.ByteBuffer;

import com.tmax.probus.nio.api.IMessageMaker;
import com.tmax.probus.nio.api.ISession;


/**
 * The Class AbstractMessageMaker.
 */
public abstract class AbstractMessageMaker implements IMessageMaker {
    /** The header_. */
    protected byte[] header_;
    /** The message_. */
    protected byte[] message_;
    /** The state_. */
    protected State state_ = State.HEADER;
    /** The offset_. */
    protected int offset_ = 0;
    /** The session_. */
    protected ISession session_;
    int messageLength_ = 0;

    // (non-Javadoc)
    // @see com.tmax.probus.nio.api.IMessageMaker#putData(java.nio.ByteBuffer)
    @Override public void putData(final ByteBuffer buffer) {
        switch (state_) {
        case HEADER:
            if (header_ == null) header_ = new byte[getHeaderLength()];
            offset_ += copyBuffer2ByteArray(buffer, header_, getHeaderLength(), offset_);
            if (getHeaderLength() == offset_) {
                messageLength_ = computeMessageLength(header_);
                message_ = new byte[messageLength_];
                System.arraycopy(header_, 0, message_, 0, getHeaderLength());
                state_ = State.BODY;
            }
            if (messageLength_ > offset_ && !buffer.hasRemaining()) break;
        case BODY:
            offset_ += copyBuffer2ByteArray(buffer, message_, messageLength_, offset_);
            if (offset_ == messageLength_) {
                final byte[] msg = message_;
                message_ = null;
                state_ = State.HEADER;
                offset_ = 0;
                session_.processMessageReceived(msg);
            }
            break;
        }
    }

    /**
     * Compute message length.
     * @param header the header
     * @return the int
     */
    abstract protected int computeMessageLength(byte[] header);

    /**
     * Gets the header length.
     * @return the header length
     */
    abstract protected int getHeaderLength();

    /**
     * Copy buffer2 byte array.
     * @param src the src
     * @param tar the tar
     * @param length the length
     * @param offset the offset
     * @return the int
     */
    private int copyBuffer2ByteArray(final ByteBuffer src, final byte[] tar, final int length, final int offset) {
        final int len = Math.min(length - offset, src.remaining());
        if (len <= 0) return 0;
        src.get(tar, offset, len);
        return len;
    }

    /**
     * The Enum State.
     */
    enum State {
        /** The HEADER. */
        HEADER,
        /** The BODY. */
        BODY;
    }
}
