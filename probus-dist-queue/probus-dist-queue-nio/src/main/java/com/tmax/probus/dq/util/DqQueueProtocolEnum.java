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
package com.tmax.probus.dq.util;

/**
 * The Enum DqProtocolEnum.
 */
public enum DqQueueProtocolEnum {
    /** The NONE. */
    NONE("NON"),
    /** The LOCK. */
    LOCK("LCK"),
    /** The UNLOCK. */
    UNLOCK("ULK"),
    /** The PUSH. */
    PUSH("PUS"),
    /** The TAKE. */
    TAKE("TKE"),
    /** The POP. */
    POP("POP"),
    /** The POLL. */
    POLL("POL"),
    /** The ACK. */
    ACK("ACK"),
    /** The NACK. */
    NACK("NCK");
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
