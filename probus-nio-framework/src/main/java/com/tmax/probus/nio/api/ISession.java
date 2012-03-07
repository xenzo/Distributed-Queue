/*
 * ISession.java Version 1.0 Feb 24, 2012
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
 * 연결을 나타내는 세션 인터페이스
 */
public interface ISession {
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
     * Reactor에서 Accept이벤트를 처리한 후 실행할 동작을 정의한다.(e.g channel에 OP_READ를 설정)
     * @param reactor the reactor
     */
    void afterAccept(IReactor reactor);

    /**
     * Reactor에서 Connect이벤트를 처리한 후 실행할 동작을 정의한다.
     * @param reactor the reactor
     */
    void afterConnect(IReactor reactor);

    /**
     * Reactor에서 Read이벤트를 처리한 후 실행할 동작을 정의한다.(e.g channel에 OP_WRITE를 설정)
     * @param reactor the reactor
     */
    void afterRead(IReactor reactor);

    /**
     * Reactor에서 Write이벤트를 처리한 후 실행할 동작을 정의한다.
     * @param reactor the reactor
     */
    void afterWrite(IReactor reactor);

    /**
     * 세션 폐기
     */
    void destroy();

    /**
     * 세션 초기화
     */
    void init();

    /**
     * channel로 부터 메세지를 읽어들인다. OP_READ 이벤트 발생시 호출된다. afterRead가 호출되려면 true를 리턴해야
     * 한다.
     * @param isEof EOF 여부
     * @return true, if on message read
     */
    boolean onMessageRead(boolean isEof);

    /**
     * 하나의 완결된 메세지가 읽어진 경우 호출된다.
     */
    void messageReceived(byte[] message);

    /**
     * Read버퍼를 반환한다.
     */
    void releaseReadBuffer();

    /**
     * Write메세지 용 큐를 반환한다.
     */
    void releaseWriteQueue();

    /**
     * 인바운드 메세지를 완성할 IMessageReader객체를 변경한다.
     * @param reader the new message reader
     */
    void setMessageReader(IMessageReader reader);

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
