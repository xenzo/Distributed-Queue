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


import java.net.Socket;
import java.nio.channels.SocketChannel;


/**
 * 연결을 나타내는 세션 인터페이스.
 */
public interface ISession {
    /**
     * Gets the channel.
     * @return the channel
     */
    SocketChannel getChannel();

    /**
     * Gets the socket.
     * @return the socket
     */
    Socket getSocket();

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
     * 세션 폐기.
     */
    void destroy();

    /**
     * 세션 초기화.
     */
    void init();

    /**
     * 인바운드 메세지를 완성할 IMessageReader객체를 변경한다.
     * @param reader the new message reader
     */
    void setMessageReader(IMessageReader reader);

    /**
     * Gets the message handler.
     * @return the message handler
     */
    IMessageHandler getMessageHandler();

    /**
     * Gets the connection event listener.
     * @return the connection event listener
     */
    IConnectionEventListener getConnectionEventListener();

    /**
     *
     * @return
     */
    IMessageEventListener getMessageEventListener();
}
