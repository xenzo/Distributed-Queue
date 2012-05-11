/*
 * EndPointInfo.java Version 1.0 May 10, 2012
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


import com.tmax.probus.nio.api.IEndPointInfo;


/**
 *
 */
public abstract class EndPointInfo implements IEndPointInfo {
    /** {@inheritDoc} */
    @Override public boolean equals(final Object that) {
        if (that instanceof IEndPointInfo && getTargetAddress() != null) {
            final IEndPointInfo info = (IEndPointInfo) that;
            final boolean isSameTarget = getTargetAddress().equals(info.getTargetAddress());
            return isSameTarget
                    && (getSourceAddress() != null
                            ? info.getSourceAddress() == null
                            : getSourceAddress().equals(info.getSourceAddress()));
        }
        return super.equals(that);
    }

    /** {@inheritDoc} */
    @Override public int hashCode() {
        return getTargetAddress().hashCode();
    }
}
