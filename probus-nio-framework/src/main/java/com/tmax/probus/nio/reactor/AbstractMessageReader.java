/*
 * AbstractMessageHandler.java Version 1.0 May 7, 2012
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


import static java.util.logging.Level.*;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

import com.tmax.probus.nio.api.IMessageReader;
import com.tmax.probus.nio.api.IReactor;


/**
 * The Class AbstractMessageHandler.
 */
public abstract class AbstractMessageReader implements IMessageReader {
    /** Logger for this class */
    private final transient Logger logger = Logger.getLogger("com.tmax.probus.nio.reactor");
    /** The write queue */
    private final Queue<ByteBuffer> writeQueue_ = new LinkedBlockingQueue<ByteBuffer>();
    /** The read buffer */
    private ByteBuffer readBuffer_;
    /** The read buffer lock */
    private final Lock readBufferLock_ = new ReentrantLock();
    /** The write queue lock */
    private final Lock writeQueueLock_ = new ReentrantLock();
    /** The channel */
    private final SocketChannel channel_;
    /** 헤더 메세지. */
    protected byte[] header_;
    /** 전체 메세지. */
    protected byte[] message_;
    /** 처리 상태. */
    protected State state_ = State.HEADER;
    /** The offset */
    protected int offset_ = 0;
    /** The message length */
    protected int messageLength_ = 0;
    /** The reactor */
    protected final IReactor reactor_;

    /**
     * Instantiates a new abstract message handler.
     * @param channel the channel
     */
    public AbstractMessageReader(final IReactor reactor, final SocketChannel channel) {
        reactor_ = reactor;
        channel_ = channel;
    }

    /** {@inheritDoc} */
    @Override public byte[] read() throws IOException {
        int nRead = 0, nLastRead = 0;
        byte[] msg = null;
        final ByteBuffer readBuffer = acquireReadBuffer();
        try {
            while (readBuffer.hasRemaining() && (nLastRead = channel_.read(readBuffer)) > 0)
                nRead += nLastRead;
            if (logger.isLoggable(FINEST))
                logger.logp(FINEST, getClass().getName(), "read()", "nRead=" + nRead + ", nLastRead=" + nLastRead);
            readBuffer.flip();
            final boolean isEof = nLastRead < 0;
            msg = copyBufferToMessage(readBuffer, isEof);
            readBuffer.compact();
            if (isEof) reactor_.closeChannel(channel_);
        } finally {
            releaseReadBuffer();
        }
        if (logger.isLoggable(FINER))
            logger.exiting(getClass().getName(), "readMessage(SocketChannel)", "end - return value=" + msg);
        return msg;
    }

    /** {@inheritDoc} */
    @Override public boolean send() throws IOException {
        final Queue<ByteBuffer> queue = acquireWriteQueue();
        try {
            if (queue == null || queue.isEmpty()) return true;
            ByteBuffer msg = null;
            while ((msg = queue.peek()) != null) {
                int cnt = 0;
                while (cnt++ < getWriteRetryCount() && msg.hasRemaining())
                    channel_.write(msg);
                if (msg.hasRemaining()) return false;
                queue.remove();
            }
        } finally {
            releaseWriteQueue();
        }
        return true;
    }

    /** {@inheritDoc} */
    @Override public void write(final byte[] msg) {
        write(msg, 0, msg.length);
    }

    /** {@inheritDoc} */
    @Override public void write(final byte[] msg, final int offset, final int length) {
        final ByteBuffer buffer = createWriteByteBuffer(msg, offset, length);
        final Queue<ByteBuffer> writeQueue = acquireWriteQueue();
        try {
            writeQueue.add(buffer);
            reactor_.changeOps(channel_, SelectionKey.OP_WRITE);
        } finally {
            releaseWriteQueue();
        }
    }

    /**
     * Read용 ByteBuffer를 획득한다.
     * @return the byte buffer
     */
    protected ByteBuffer acquireReadBuffer() {
        final Lock lock = readBufferLock_;
        lock.lock();
        if (readBuffer_ == null) readBuffer_ = createReadBuffer();
        return readBuffer_;
    }

    /**
     * Write 메세지를 저장할 Queue를 획득한다.
     * @return the queue
     */
    protected Queue<ByteBuffer> acquireWriteQueue() {
        final Lock lock = writeQueueLock_;
        lock.lock();
        return writeQueue_;
    }

    /**
     * header로 부터 Header와 Body를 포함한 메세지 전체 길이를 반환해야 한다.
     * @param header the header
     * @return 메세지 전체 길이
     */
    abstract protected int computeMessageLength(byte[] header);

    /**
     * ByteBuffer의 데이터를 읽어들여 byte 배열에 복사한다.<br/>
     * 복사할 길이가 0이하일 경우 복사 없이 0을 리턴한다.
     * @param src 읽어들일 버퍼
     * @param tar 기입할 byte 배열
     * @param offset 복사할 위치
     * @param length 복사할 길이
     * @return 복사한 길이를 반환한다. 복사할 것이 없는 경우 0 반환
     */
    protected final int copyBuffer2ByteArray(final ByteBuffer src, final byte[] tar, final int offset, final int length) {
        final int len = Math.min(length, src.remaining());
        if (len <= 0) return 0;
        src.get(tar, offset, len);
        return len;
    }

    /**
     * Creates the read buffer.
     * @return the byte buffer
     */
    protected ByteBuffer createReadBuffer() {
        return ByteBuffer.allocate(8192);
    }

    /**
     * Creates the write byte buffer.
     * @param msg the msg
     * @param offset the offset
     * @param length the length
     * @return the byte buffer
     */
    protected ByteBuffer createWriteByteBuffer(final byte[] msg, final int offset, final int length) {
        return ByteBuffer.wrap(msg, offset, length);
    }

    /**
     * Gets the channel.
     * @return the channel
     */
    public final SocketChannel getChannel() {
        return channel_;
    }

    /**
     * 헤더의 길이를 반환해야 한다.
     * @return 헤더의 길이
     */
    abstract protected int getHeaderLength();

    /**
     * Gets the write retry count.
     * @return the write retry count
     */
    protected int getWriteRetryCount() {
        return 2;
    }

    /**
     * Read버퍼를 반환한다.
     */
    protected void releaseReadBuffer() {
        final Lock lock = readBufferLock_;
        lock.unlock();
    }

    /**
     * Write메세지 용 큐를 반환한다.
     */
    protected void releaseWriteQueue() {
        final Lock lock = writeQueueLock_;
        lock.unlock();
    }

    private byte[] copyBufferToMessage(final ByteBuffer buffer, final boolean isEof) {
        if (getHeaderLength() >= 0) return readByLength(buffer, isEof);
        else return readUntilEof(buffer, isEof);
    }

    /**
     * Read by length.
     * @param buffer the buffer
     * @param isEof the is eof
     * @return true, if successful
     */
    private byte[] readByLength(final ByteBuffer buffer, final boolean isEof) {
        switch (state_) {
        case HEADER:
            if (header_ == null) header_ = new byte[getHeaderLength()];
            offset_ += copyBuffer2ByteArray(buffer, header_, offset_, getHeaderLength() - offset_);
            if (getHeaderLength() == offset_) {
                messageLength_ = computeMessageLength(header_);
                message_ = new byte[messageLength_];
                System.arraycopy(header_, 0, message_, 0, getHeaderLength());
                state_ = State.BODY;
            }
            if (messageLength_ > offset_ && !buffer.hasRemaining()) break;
        case BODY:
            offset_ += copyBuffer2ByteArray(buffer, message_, offset_, messageLength_ - offset_);
            if (offset_ == messageLength_ || isEof) {
                byte[] msg = null;
                if (isEof) {
                    msg = new byte[offset_];
                    System.arraycopy(message_, 0, msg, 0, offset_);
                } else msg = message_;
                message_ = null;
                state_ = State.HEADER;
                offset_ = 0;
                return msg;
            }
            break;
        }
        return null;
    }

    /**
     * Read until eof.
     * @param buffer the buffer
     * @param isEof the is eof
     * @return true, if successful
     */
    private byte[] readUntilEof(final ByteBuffer buffer, final boolean isEof) {
        final int len = buffer.remaining();
        if (len < 1) {
            if (isEof) return getMessageAndCleanUp();
            return null;
        }
        final byte[] data = new byte[offset_ + len];
        System.arraycopy(message_, 0, data, 0, offset_);
        buffer.get(data, offset_, len);
        offset_ += len;
        message_ = data;
        if (isEof) return getMessageAndCleanUp();
        return null;
    }

    /**
     * Gets the message and clean up.
     * @return the message and clean up
     */
    private byte[] getMessageAndCleanUp() {
        final byte[] msg = message_;
        message_ = null;
        offset_ = 0;
        return msg;
    }

    /**
     * 메세지 처리 단계를 나타내는 enum.
     */
    protected enum State {
        /** The HEADER. */
        HEADER,
        /** The BODY. */
        BODY;
    }
}
