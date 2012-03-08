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

import com.tmax.probus.nio.api.IMessageReader;


/**
 * The Class AbstractMessageMaker.
 */
public abstract class AbstractMessageReader implements IMessageReader {
    /** 헤더 메세지. */
    protected byte[] header_;
    /** 전체 메세지. */
    protected byte[] message_;
    /** 처리 상태. */
    protected State state_ = State.HEADER;
    /** The offset_. */
    protected int offset_ = 0;
    /** The message length_. */
    protected int messageLength_ = 0;

    /** {@inheritDoc} */
    @Override public boolean readBuffer(final ByteBuffer buffer, boolean isEof) {
        if (getHeaderLength() > 0) return readByLength(buffer, isEof);
        else return readUntilEof(buffer, isEof);
    }

    /**
     * Read until eof.
     * @param buffer the buffer
     * @param isEof the is eof
     * @return true, if successful
     */
    private boolean readUntilEof(ByteBuffer buffer, boolean isEof) {
        if (message_ == null) {
            messageLength_ = computeMessageLength(null);
            if (messageLength_ > 0) message_ = new byte[messageLength_];
        }
        offset_ += copyBuffer2ByteArray(buffer, message_, offset_, messageLength_);
        if (isEof || offset_ == messageLength_) {
            byte[] msg = new byte[offset_];
            System.arraycopy(message_, 0, msg, 0, offset_);
            message_ = null;
            offset_ = 0;
            return true;
        }
        return false;
    }

    /**
     * Read by length.
     * @param buffer the buffer
     * @param isEof the is eof
     * @return true, if successful
     */
    private boolean readByLength(final ByteBuffer buffer, boolean isEof) {
        switch (state_) {
        case HEADER:
            if (header_ == null) header_ = new byte[getHeaderLength()];
            offset_ += copyBuffer2ByteArray(buffer, header_, offset_, getHeaderLength());
            if (getHeaderLength() == offset_) {
                messageLength_ = computeMessageLength(header_);
                message_ = new byte[messageLength_];
                System.arraycopy(header_, 0, message_, 0, getHeaderLength());
                state_ = State.BODY;
            }
            if (messageLength_ > offset_ && !buffer.hasRemaining()) break;
        case BODY:
            offset_ += copyBuffer2ByteArray(buffer, message_, offset_, messageLength_);
            if (offset_ == messageLength_ || isEof) {
                byte[] msg = null;
                if (isEof) {
                    msg = new byte[offset_];
                    System.arraycopy(message_, 0, msg, 0, offset_);
                } else msg = message_;
                message_ = null;
                state_ = State.HEADER;
                offset_ = 0;
                return true;
            }
            break;
        }
        return false;
    }

    /**
     * header로 부터 Header와 Body를 포함한 메세지 전체 길이를 반환해야 한다.
     * @param header the header
     * @return 메세지 전체 길이
     */
    abstract protected int computeMessageLength(byte[] header);

    /**
     * 헤더의 길이를 반환해야 한다.
     * @return 헤더의 길이
     */
    abstract protected int getHeaderLength();

    /**
     * ByteBuffer의 데이터를 읽어들여 byte 배열에 복사한다.
     * @param src 읽어들일 버퍼
     * @param tar 기입할 byte 배열
     * @param offset the offset
     * @param length the length
     * @return 복사한 길이를 반환한다.
     */
    protected final int copyBuffer2ByteArray(final ByteBuffer src, final byte[] tar, final int offset, final int length) {
        final int len = Math.min(length - offset, src.remaining());
        if (len <= 0) return 0;
        src.get(tar, offset, len);
        return len;
    }

    /**
     * 메세지 처리 단계를 나타내는 enum.
     */
    private enum State {
        /** The HEADER. */
        HEADER,
        /** The BODY. */
        BODY;
    }
}
