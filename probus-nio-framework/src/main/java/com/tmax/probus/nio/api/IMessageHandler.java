/*
 * IMessageHandler.java Version 1.0 Mar 9, 2012
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


import java.nio.ByteBuffer;
import java.util.Queue;


/**
 *
 */
public interface IMessageHandler {
    /**
     * Read용 ByteBuffer를 획득한다.
     * @return the byte buffer
     */
    ByteBuffer acquireReadBuffer();

    /**
     * Write 메세지를 저장할 Queue를 획득한다.
     * @return the queue
     */
    Queue<ByteBuffer> acquireWriteQueue();

    /**
     * buffer로 부터 메세지를 읽어들인다. OP_READ 이벤트 발생시 호출된다. afterRead가 호출되려면 byte[]를
     * 리턴해야 한다.
     * @param isEof EOF 여부
     * @return 하나의 메세지를 다 읽은게 아니라면 null을 반환
     */
    byte[] onMessageRead(boolean isEof);

    /**
     * Read버퍼를 반환한다.
     */
    void releaseReadBuffer();

    /**
     * Write메세지 용 큐를 반환한다.
     */
    void releaseWriteQueue();

    /**
     * 전송할 byte[] 메세지를 등록한다.
     * @param msg the msg
     */
    void write(byte[] msg);

    /**
     * 전송할 byte[] 메세지 중 offset부터 length만큼을 등록한다.
     * @param msg the msg
     * @param offset the offset
     * @param length the length
     */
    void write(byte[] msg, int offset, int length);
}
