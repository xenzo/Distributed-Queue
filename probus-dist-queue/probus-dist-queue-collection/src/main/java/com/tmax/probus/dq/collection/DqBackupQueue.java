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
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;


/**
 * The Class DqBackupQueue.
 * @param <K> the key type
 * @param <E> the element type
 */
public class DqBackupQueue<K, E extends IDqElement<K>> extends AbstractQueue<E>
        implements BlockingQueue<E>, IDqMap<K, IDqNode<K, E>> {
    /** Logger for this class. */
    private final transient Logger logger = Logger.getLogger("com.tmax.probus.dq.collection");
    /** The tail_. */
    private IDqNode<K, E> first_;
    /** The head_. */
    private IDqNode<K, E> head_;
    /** The tail_. */
    private IDqNode<K, E> tail_;
    /** The NUL l_ node. */
    private static final transient IDqNode NULL_NODE = new IDqNode(null);
    private int capacity;

    /**
     * Instantiates a new dq backup queue.
     */
    private DqBackupQueue() {
        this(Integer.MAX_VALUE);
    }

    /**
     * @param maxValue
     */
    private DqBackupQueue(int maxValue) {
        capacity = maxValue;
        first_ = head_ = tail_ = NULL_NODE;
    }

    // (non-Javadoc)
    // @see java.util.Queue#offer(java.lang.Object)
    @Override
    public boolean offer(final E e) {
        if (e == null) return false;
        final IDqNode node = new IDqNode(e);
        if (first_ == NULL_NODE) first_ = node;
        if (head_ == NULL_NODE) head_ = node;
        if (tail_ != NULL_NODE) {
            node.prev = tail_;
            node.next = NULL_NODE;
            tail_.next = node;
        }
        tail_ = node;
        return false;
    }

    // (non-Javadoc)
    // @see java.util.Queue#poll()
    @Override
    public E poll() {
        if (head_ == NULL_NODE) return null;
        final E ret = head_.element;
        head_ = head_.next;
        if (head_ == NULL_NODE) tail_ = NULL_NODE;
        return ret;
    }

    // (non-Javadoc)
    // @see java.util.Queue#peek()
    @Override
    public E peek() {
        return head_.element;
    }

    // (non-Javadoc)
    // @see java.util.AbstractCollection#iterator()
    @Override
    public Iterator<E> iterator() {
        if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "iterator");
        // XXX must do something
        return null;
    }

    // (non-Javadoc)
    // @see java.util.AbstractCollection#size()
    @Override
    public int size() {
        if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "size");
        // XXX must do something
        return 0;
    }

    // (non-Javadoc)
    // @see java.util.concurrent.BlockingQueue#drainTo(java.util.Collection)
    @Override
    public int drainTo(final Collection<? super E> collection) {
        int cnt = 0;
        for (; head_ != NULL_NODE; head_ = head_.next) {
            cnt++;
            collection.add(head_.element);
        }
        if (head_ == NULL_NODE) tail_ = NULL_NODE;
        return cnt;
    }

    // (non-Javadoc)
    // @see java.util.concurrent.BlockingQueue#drainTo(java.util.Collection, int)
    @Override
    public int drainTo(final Collection<? super E> collection, final int max) {
        int cnt = 0;
        for (; head_ != NULL_NODE && cnt <= max; head_ = head_.next) {
            cnt++;
            collection.add(head_.element);
        }
        if (head_ == NULL_NODE) tail_ = NULL_NODE;
        return cnt;
    }

    // (non-Javadoc)
    // @see java.util.concurrent.BlockingQueue#offer(java.lang.Object, long, java.util.concurrent.TimeUnit)
    @Override
    public boolean offer(final E paramE, final long paramLong, final TimeUnit paramTimeUnit)
            throws InterruptedException {
        if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "offer");
        // XXX must do something
        return false;
    }

    // (non-Javadoc)
    // @see java.util.concurrent.BlockingQueue#poll(long, java.util.concurrent.TimeUnit)
    @Override
    public E poll(final long paramLong, final TimeUnit paramTimeUnit) throws InterruptedException {
        if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "poll");
        // XXX must do something
        return null;
    }

    // (non-Javadoc)
    // @see java.util.concurrent.BlockingQueue#put(java.lang.Object)
    @Override
    public void put(final E paramE) throws InterruptedException {
        if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "put");
        // XXX must do something
        if (logger.isLoggable(FINER)) logger.exiting(getClass().getName(), "put");
    }

    // (non-Javadoc)
    // @see java.util.concurrent.BlockingQueue#remainingCapacity()
    @Override
    public int remainingCapacity() {
        if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "remainingCapacity");
        // XXX must do something
        return 0;
    }

    // (non-Javadoc)
    // @see java.util.concurrent.BlockingQueue#take()
    @Override
    public E take() throws InterruptedException {
        if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "take");
        // XXX must do something
        return null;
    }
}
