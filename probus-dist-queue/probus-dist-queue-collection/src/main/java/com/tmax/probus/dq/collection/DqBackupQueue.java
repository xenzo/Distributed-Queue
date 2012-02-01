/*
 * BackupQueue.java Version 1.0 Jan 31, 2012
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


import static java.util.logging.Level.*;

import java.util.AbstractQueue;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;


/**
 *
 */
public class DqBackupQueue<E extends IDqElement> extends AbstractQueue<E>
        implements BlockingQueue<E> {
    /**
     * Logger for this class
     */
    private final transient Logger logger = Logger.getLogger("com.tmax.probus.dq.collection");
    private Map<String, E> repo_ = new ConcurrentHashMap<String, E>();
    private Queue<E> queue_ = new LinkedBlockingQueue<E>();

    // (non-Javadoc)
    // @see java.util.Queue#offer(java.lang.Object)
    @Override public boolean offer(E e) {
        if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "offer");
        String id = e.getId();
        if (repo_.containsKey(id)) return false;
        if (queue_.offer(e)) {
            repo_.put(id, e);
            return true;
        }
        return false;
    }

    // (non-Javadoc)
    // @see java.util.Queue#poll()
    @Override public E poll() {
        if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "poll");
        // XXX must do something
        return null;
    }

    // (non-Javadoc)
    // @see java.util.Queue#peek()
    @Override public E peek() {
        if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "peek");
        // XXX must do something
        return null;
    }

    // (non-Javadoc)
    // @see java.util.AbstractCollection#iterator()
    @Override public Iterator<E> iterator() {
        if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "iterator");
        // XXX must do something
        return null;
    }

    // (non-Javadoc)
    // @see java.util.AbstractCollection#size()
    @Override public int size() {
        if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "size");
        // XXX must do something
        return 0;
    }

    // (non-Javadoc)
    // @see java.util.concurrent.BlockingQueue#drainTo(java.util.Collection)
    @Override public int drainTo(Collection<? super E> paramCollection) {
        if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "drainTo");
        // XXX must do something
        return 0;
    }

    // (non-Javadoc)
    // @see java.util.concurrent.BlockingQueue#drainTo(java.util.Collection, int)
    @Override public int drainTo(Collection<? super E> paramCollection, int paramInt) {
        if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "drainTo");
        // XXX must do something
        return 0;
    }

    // (non-Javadoc)
    // @see java.util.concurrent.BlockingQueue#offer(java.lang.Object, long, java.util.concurrent.TimeUnit)
    @Override public boolean offer(E paramE, long paramLong, TimeUnit paramTimeUnit)
            throws InterruptedException {
        if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "offer");
        // XXX must do something
        return false;
    }

    // (non-Javadoc)
    // @see java.util.concurrent.BlockingQueue#poll(long, java.util.concurrent.TimeUnit)
    @Override public E poll(long paramLong, TimeUnit paramTimeUnit) throws InterruptedException {
        if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "poll");
        // XXX must do something
        return null;
    }

    // (non-Javadoc)
    // @see java.util.concurrent.BlockingQueue#put(java.lang.Object)
    @Override public void put(E paramE) throws InterruptedException {
        if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "put");
        // XXX must do something
        if (logger.isLoggable(FINER)) logger.exiting(getClass().getName(), "put");
    }

    // (non-Javadoc)
    // @see java.util.concurrent.BlockingQueue#remainingCapacity()
    @Override public int remainingCapacity() {
        if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "remainingCapacity");
        // XXX must do something
        return 0;
    }

    // (non-Javadoc)
    // @see java.util.concurrent.BlockingQueue#take()
    @Override public E take() throws InterruptedException {
        if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "take");
        // XXX must do something
        return null;
    }
}
