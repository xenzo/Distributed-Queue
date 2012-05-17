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


import java.io.IOException;


/**
 *
 */
public interface IMessageIoHandler {
    /**
     * buffer로 부터 메세지를 읽어들인다. OP_READ 이벤트 발생시 호출된다. handOff 처리되려면 byte[]를 리턴해야
     * 한다.
     * @param isEof EOF 여부
     * @return 하나의 메세지를 다 읽은게 아니라면 null을 반환
     */
    byte[] read() throws IOException;

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

    /**
     * 메세지를 전송한다. OP_WRITE 이벤트 발생시 호출된다. handOff 처리되려면 true를 리턴해야 한다.
     * @return true, if finish
     * @throws IOException Signals that an I/O exception has occurred.
     */
    boolean send() throws IOException;
}
