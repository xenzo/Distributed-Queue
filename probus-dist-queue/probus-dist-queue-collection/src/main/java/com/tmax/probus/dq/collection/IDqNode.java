/*
 * Node.java Version 1.0 Feb 2, 2012
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
package com.tmax.probus.dq.collection;

/**
 * The Class Node.
 */
class IDqNode<K, E extends IDqElement<K>> {
    /** The next. */
    IDqNode prev;
    /** The next. */
    IDqNode next;
    /** The element. */
    final E element;

    /**
     * Instantiates a new node.
     * @param obj the obj
     */
    IDqNode(final E obj) {
        element = obj;
    }

    /**
     * Gets the id.
     * @return the id
     */
    K getId() {
        if (element != null) return element.getId();
        return null;
    }

    // (non-Javadoc)
    // @see java.lang.Object#hashCode()
    @Override
    public int hashCode() {
        return element.hashCode();
    }

    // (non-Javadoc)
    // @see java.lang.Object#equals(java.lang.Object)
    @Override
    public boolean equals(final Object obj) {
        if (obj == null || element == null) return false;
        if (obj instanceof IDqNode) return element.equals(((IDqNode) obj).element);
        return super.equals(obj);
    }
}
