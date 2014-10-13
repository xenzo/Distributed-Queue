/*
 * IDqCollectionListener.java Version 1.0 Feb 4, 2012
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
package com.tmaxsoft.probus.dq.collection;

/**
 * The listener interface for receiving IDqCollection events. The class that is
 * interested in processing a IDqCollection event implements this interface, and
 * the object created with that class is registered with a component using the
 * component's <code>addIDqCollectionListener<code> method. When
 * the IDqCollection event occurs, that object's appropriate
 * method is invoked.
 * @param <E> the element type
 * @see IDqCollectionEvent
 */
public interface IDqItemEventListener<E> {
    /**
     * Process item added.
     * @param e the e
     */
    void processItemAdded(E e);

    /**
     * Process item removed.
     * @param e the e
     */
    void processItemRemoved(E e);
}
