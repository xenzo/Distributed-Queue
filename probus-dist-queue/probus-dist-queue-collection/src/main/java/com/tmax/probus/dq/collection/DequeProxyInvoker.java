/*
 * DequeProxyInvoker.java Version 1.0 Jul 24, 2012
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
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;


/**
 *
 */
class DequeProxyInvoker<K, E extends IDqElement<K>>
        implements InvocationHandler, BlockingDeque<E> {
    /**
     * Logger for this class
     */
    private final transient Logger logger = Logger.getLogger("com.tmax.probus.dq.collection");
    /**  */
    private DqCollection<K, E> deque_;

    /**
     * @param newCollection
     * @param dqCollections TODO
     */
    public DequeProxyInvoker(DqCollection<K, E> newCollection) {
        deque_ = newCollection;
    }

    /** {@inheritDoc} */
    @Override public int remainingCapacity() {
        return 0;
    }

    /** {@inheritDoc} */
    @Override public int drainTo(Collection<? super E> c) {
        return 0;
    }

    /** {@inheritDoc} */
    @Override public int drainTo(Collection<? super E> c, int maxElements) {
        return 0;
    }

    /** {@inheritDoc} */
    @Override public boolean isEmpty() {
        return false;
    }

    /** {@inheritDoc} */
    @Override public Object[] toArray() {
        return null;
    }

    /** {@inheritDoc} */
    @Override public <T> T[] toArray(T[] a) {
        return null;
    }

    /** {@inheritDoc} */
    @Override public boolean containsAll(Collection<?> c) {
        return false;
    }

    /** {@inheritDoc} */
    @Override public boolean addAll(Collection<? extends E> c) {
        return false;
    }

    /** {@inheritDoc} */
    @Override public boolean removeAll(Collection<?> c) {
        return false;
    }

    /** {@inheritDoc} */
    @Override public boolean retainAll(Collection<?> c) {
        return false;
    }

    /** {@inheritDoc} */
    @Override public void clear() {
    }

    /** {@inheritDoc} */
    @Override public E removeFirst() {
        return null;
    }

    /** {@inheritDoc} */
    @Override public E removeLast() {
        return null;
    }

    /** {@inheritDoc} */
    @Override public E pollFirst() {
        return null;
    }

    /** {@inheritDoc} */
    @Override public E pollLast() {
        return null;
    }

    /** {@inheritDoc} */
    @Override public E getFirst() {
        return null;
    }

    /** {@inheritDoc} */
    @Override public E getLast() {
        return null;
    }

    /** {@inheritDoc} */
    @Override public E peekFirst() {
        return null;
    }

    /** {@inheritDoc} */
    @Override public E peekLast() {
        return null;
    }

    /** {@inheritDoc} */
    @Override public E pop() {
        return null;
    }

    /** {@inheritDoc} */
    @Override public Iterator<E> descendingIterator() {
        return null;
    }

    /** {@inheritDoc} */
    @Override public void addFirst(E e) {
        if (!offerFirst(e)) throw new IllegalStateException("Deque full");
    }

    /** {@inheritDoc} */
    @Override public void addLast(E e) {
        if (!offerLast(e)) throw new IllegalStateException("Deque full");
    }

    /** {@inheritDoc} */
    @Override public boolean offerFirst(E e) {
        return deque_.linkFirst(e);
    }

    /** {@inheritDoc} */
    @Override public boolean offerLast(E e) {
        return deque_.linkLast(e);
    }

    /** {@inheritDoc} */
    @Override public void putFirst(E e) throws InterruptedException {
    }

    /** {@inheritDoc} */
    @Override public void putLast(E e) throws InterruptedException {
    }

    /** {@inheritDoc} */
    @Override public boolean offerFirst(E e, long timeout, TimeUnit unit)
            throws InterruptedException {
        return false;
    }

    /** {@inheritDoc} */
    @Override public boolean offerLast(E e, long timeout, TimeUnit unit)
            throws InterruptedException {
        return false;
    }

    /** {@inheritDoc} */
    @Override public E takeFirst() throws InterruptedException {
        return null;
    }

    /** {@inheritDoc} */
    @Override public E takeLast() throws InterruptedException {
        return null;
    }

    /** {@inheritDoc} */
    @Override public E pollFirst(long timeout, TimeUnit unit) throws InterruptedException {
        return null;
    }

    /** {@inheritDoc} */
    @Override public E pollLast(long timeout, TimeUnit unit) throws InterruptedException {
        return null;
    }

    /** {@inheritDoc} */
    @Override public boolean removeFirstOccurrence(Object o) {
        return false;
    }

    /** {@inheritDoc} */
    @Override public boolean removeLastOccurrence(Object o) {
        return false;
    }

    /** {@inheritDoc} */
    @Override public boolean add(E e) {
        return false;
    }

    /** {@inheritDoc} */
    @Override public boolean offer(E e) {
        return false;
    }

    /** {@inheritDoc} */
    @Override public void put(E e) throws InterruptedException {
    }

    /** {@inheritDoc} */
    @Override public boolean offer(E e, long timeout, TimeUnit unit) throws InterruptedException {
        return false;
    }

    /** {@inheritDoc} */
    @Override public E remove() {
        return null;
    }

    /** {@inheritDoc} */
    @Override public E poll() {
        return null;
    }

    /** {@inheritDoc} */
    @Override public E take() throws InterruptedException {
        return null;
    }

    /** {@inheritDoc} */
    @Override public E poll(long timeout, TimeUnit unit) throws InterruptedException {
        return null;
    }

    /** {@inheritDoc} */
    @Override public E element() {
        return null;
    }

    /** {@inheritDoc} */
    @Override public E peek() {
        return null;
    }

    /** {@inheritDoc} */
    @Override public boolean remove(Object o) {
        return false;
    }

    /** {@inheritDoc} */
    @Override public boolean contains(Object o) {
        return false;
    }

    /** {@inheritDoc} */
    @Override public int size() {
        return 0;
    }

    /** {@inheritDoc} */
    @Override public Iterator<E> iterator() {
        return null;
    }

    /** {@inheritDoc} */
    @Override public void push(E e) {
    }

    /** {@inheritDoc} */
    @Override public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        return null;
    }
}
