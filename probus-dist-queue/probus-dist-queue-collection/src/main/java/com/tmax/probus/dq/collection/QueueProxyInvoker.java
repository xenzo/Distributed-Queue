/*
 * QueueProxyInvoker.java Version 1.0 Jul 24, 2012
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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;


/**
 *
 */
public class QueueProxyInvoker<K, E extends IDqElement<K>>
        implements InvocationHandler, BlockingQueue<E> {
    /**
     * Logger for this class
     */
    private final transient Logger logger = Logger.getLogger("com.tmax.probus.dq.collection");
    /**  */
    private DqCollection<K, E> queue_;

    /**
     * @param newCollection
     * @param dqCollections TODO
     */
    public QueueProxyInvoker(DqCollection<K, E> newCollection) {
        queue_ = newCollection;
    }

    /** {@inheritDoc} */
    @Override public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        return method.invoke(proxy, args);
    }

    /** {@inheritDoc} */
    @Override public E remove() {
        E e = queue_.unlinkFirst();
        if (e == null) throw new NoSuchElementException();
        return e;
    }

    /** {@inheritDoc} */
    @Override public E poll() {
        return queue_.unlinkFirst();
    }

    /** {@inheritDoc} */
    @Override public E element() {
        if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "element");
        // XXX must do something
        return null;
    }

    /** {@inheritDoc} */
    @Override public E peek() {
        return queue_.getFirst();
    }

    /** {@inheritDoc} */
    @Override public int size() {
        return queue_.size();
    }

    /** {@inheritDoc} */
    @Override public boolean isEmpty() {
        return queue_.size() == 0;
    }

    /** {@inheritDoc} */
    @Override public Iterator<E> iterator() {
        if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "iterator");
        // XXX must do something
        return null;
    }

    /** {@inheritDoc} */
    @Override public Object[] toArray() {
        if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "toArray");
        // XXX must do something
        return null;
    }

    /** {@inheritDoc} */
    @Override public <T> T[] toArray(T[] a) {
        if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "toArray");
        // XXX must do something
        return null;
    }

    /** {@inheritDoc} */
    @Override public boolean containsAll(Collection<?> c) {
        if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "containsAll");
        // XXX must do something
        return false;
    }

    /** {@inheritDoc} */
    @Override public boolean addAll(Collection<? extends E> c) {
        if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "addAll");
        // XXX must do something
        return false;
    }

    /** {@inheritDoc} */
    @Override public boolean removeAll(Collection<?> c) {
        if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "removeAll");
        // XXX must do something
        return false;
    }

    /** {@inheritDoc} */
    @Override public boolean retainAll(Collection<?> c) {
        if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "retainAll");
        // XXX must do something
        return false;
    }

    /** {@inheritDoc} */
    @Override public void clear() {
        if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "clear");
        // XXX must do something
        if (logger.isLoggable(FINER)) logger.exiting(getClass().getName(), "clear");
    }

    /** {@inheritDoc} */
    @Override public boolean add(E e) {
        if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "add");
        // XXX must do something
        return false;
    }

    /** {@inheritDoc} */
    @Override public boolean offer(E e) {
        if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "offer");
        // XXX must do something
        return false;
    }

    /** {@inheritDoc} */
    @Override public void put(E e) throws InterruptedException {
        if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "put");
        // XXX must do something
        if (logger.isLoggable(FINER)) logger.exiting(getClass().getName(), "put");
    }

    /** {@inheritDoc} */
    @Override public boolean offer(E e, long timeout, TimeUnit unit) throws InterruptedException {
        if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "offer");
        // XXX must do something
        return false;
    }

    /** {@inheritDoc} */
    @Override public E take() throws InterruptedException {
        if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "take");
        // XXX must do something
        return null;
    }

    /** {@inheritDoc} */
    @Override public E poll(long timeout, TimeUnit unit) throws InterruptedException {
        if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "poll");
        // XXX must do something
        return null;
    }

    /** {@inheritDoc} */
    @Override public int remainingCapacity() {
        if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "remainingCapacity");
        // XXX must do something
        return 0;
    }

    /** {@inheritDoc} */
    @Override public boolean remove(Object o) {
        if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "remove");
        // XXX must do something
        return false;
    }

    /** {@inheritDoc} */
    @Override public boolean contains(Object o) {
        if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "contains");
        // XXX must do something
        return false;
    }

    /** {@inheritDoc} */
    @Override public int drainTo(Collection<? super E> c) {
        if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "drainTo");
        // XXX must do something
        return 0;
    }

    /** {@inheritDoc} */
    @Override public int drainTo(Collection<? super E> c, int maxElements) {
        if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "drainTo");
        // XXX must do something
        return 0;
    }
}
