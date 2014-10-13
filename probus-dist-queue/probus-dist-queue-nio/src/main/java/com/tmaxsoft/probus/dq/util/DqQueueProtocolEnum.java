/*
 * DqProtocolEnum.java Version 1.0 Jan 27, 2012
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
package com.tmaxsoft.probus.dq.util;

/**
 * The Enum DqProtocolEnum.
 */
public enum DqQueueProtocolEnum {
    /** The ACK. */
    ACK("ACK_"),
    /** The GE t_ first. */
    GET_FIRST("GETF"),
    /** The GE t_ last. */
    GET_LAST("GETL"),
    /** The LOCK. */
    LOCK("LOCK"),
    /** The NACK. */
    NACK("NACK"),
    /** The NONE. */
    NONE("NONE"),
    /** The OFFER. */
    OFFER("OFER"),
    /** The PU t_ first. */
    PUT_FIRST("PUTF"),
    /** The PU t_ last. */
    PUT_LAST("PUTL"),
    /** The TAKE. */
    TAKE("TAKE"),
    /** The UNLOCK. */
    UNLOCK("UNLK");
    /** The protocol_. */
    private final String protocol_;

    /**
     * Instantiates a new dq protocol enum.
     * @param protocol the protocol
     */
    private DqQueueProtocolEnum(final String protocol) {
        protocol_ = protocol;
    }

    /**
     * Value of protocol.
     * @param protocol the protocol
     * @return the dq protocol enum
     */
    public static DqQueueProtocolEnum valueOfProtocol(final String protocol) {
        for (final DqQueueProtocolEnum item : DqQueueProtocolEnum.values())
            if (protocol.equals(item.getProtocol())) return item;
        return NONE;
    }

    /**
     * Gets the protocol.
     * @return the protocol
     */
    public String getProtocol() {
        return protocol_;
    }
}
