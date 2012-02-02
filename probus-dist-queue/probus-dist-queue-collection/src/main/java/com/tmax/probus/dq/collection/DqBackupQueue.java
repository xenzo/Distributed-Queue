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

import java.lang.ref.WeakReference;
import java.util.AbstractQueue;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;


/**
 * The Class DqBackupQueue.
 * @param <K> the key type
 * @param <E> the element type
 */
public class DqBackupQueue<K, E extends IDqElement<K>>
        extends AbstractQueue<E>
        implements BlockingDeque<E>, BlockingQueue<E>, IDqMap<K, E> {
    /** Logger for this class. */
    private final transient Logger logger = Logger.getLogger("com.tmax.probus.dq.collection");
    /** The head_. */
    private Node<K, E> head_;
    /** The tail_. */
    private Node<K, E> tail_;
    /** The capacity. */
    private int capacity;
    /** The count. */
    private final AtomicInteger count = new AtomicInteger(0);
    /** The full count. */
    private final AtomicInteger fullCount = new AtomicInteger(0);
    /** Lock held by take, poll, etc */
    private final ReentrantLock lock_ = new ReentrantLock();
    /** Wait queue for waiting takes */
    private final Condition notEmpty = lock_.newCondition();
    /** Wait queue for waiting puts */
    private final Condition notFull = lock_.newCondition();
    private final transient Node<K, E> NULL_NODE = new Node<K, E>(null);
    {
        NULL_NODE.prev = NULL_NODE.next = NULL_NODE;
        NULL_NODE.isReal = false;
    }

    /**
     * Instantiates a new dq backup queue.
     */
    private DqBackupQueue() {
        this(Integer.MAX_VALUE);
    }

    /**
     * Instantiates a new dq backup queue.
     * @param maxValue the max value
     */
    private DqBackupQueue(final int maxValue) {
        if (maxValue <= 0) throw new IllegalArgumentException();
        capacity = maxValue;
        head_ = tail_ = NULL_NODE;
    }

    // (non-Javadoc)
    // @see java.util.concurrent.BlockingDeque#putFirst(java.lang.Object)
    @Override public void putFirst(E e) throws InterruptedException {
        if (e == null) throw new NullPointerException();
        Node<K, E> node = new Node<K, E>(e);
        final ReentrantLock lock = lock_;
        lock.lock();
        try {
            while (!linkFirst(node))
                notFull.await();
        } finally {
            lock.unlock();
        }
    }

    // (non-Javadoc)
    // @see java.util.Deque#addFirst(java.lang.Object)
    @Override public void addFirst(final E e) {
        if (!offerFirst(e)) throw new IllegalStateException("FULL");
    }

    // (non-Javadoc)
    // @see java.util.Deque#offerFirst(java.lang.Object)
    @Override public boolean offerFirst(final E e) {
        if (e == null) throw new NullPointerException();
        Node<K, E> node = new Node<K, E>(e);
        final ReentrantLock lock = lock_;
        lock.lock();
        try {
            return linkFirst(node);
        } finally {
            lock.unlock();
        }
    }

    // (non-Javadoc)
    // @see java.util.concurrent.BlockingQueue#put(java.lang.Object)
    @Override public void put(final E e) throws InterruptedException {
        putLast(e);
    }

    // (non-Javadoc)
    // @see java.util.concurrent.BlockingDeque#putLast(java.lang.Object)
    @Override public void putLast(E e) throws InterruptedException {
        if (e == null) throw new NullPointerException();
        Node<K, E> node = new Node<K, E>(e);
        final ReentrantLock lock = lock_;
        lock.lock();
        try {
            while (!linkLast(node))
                notFull.await();
        } finally {
            lock.unlock();
        }
    }

    // (non-Javadoc)
    // @see java.util.Deque#addLast(java.lang.Object)
    @Override public void addLast(final E e) {
        if (!offerLast(e)) throw new IllegalStateException("FULL");
    }

    // (non-Javadoc)
    // @see java.util.Queue#offer(java.lang.Object)
    @Override public boolean offer(final E e) {
        return offerLast(e);
    }

    // (non-Javadoc)
    // @see java.util.Deque#offerLast(java.lang.Object)
    @Override public boolean offerLast(final E e) {
        if (e == null) throw new NullPointerException();
        Node<K, E> node = new Node<K, E>(e);
        final ReentrantLock lock = lock_;
        lock.lock();
        try {
            return linkLast(node);
        } finally {
            lock.unlock();
        }
    }

    /**
     *
     */
    private boolean linkFirst(Node<K, E> node) {
        if (isFull()) return false;
        Node<K, E> first = head_.next;
        node.next = first;
        node.prev = first.prev;
        first.prev.next = node;
        first.prev = node;
        head_.next = node;
        count.incrementAndGet();
        fullCount.incrementAndGet();
        return true;
    }

    private E unlinkFirst() {
        Node<K, E> h = head_.next;
        head_.next = h.next;
        h.isReal = false;
        return h.element;
    }

    /**
     * @param node
     * @return
     */
    private boolean linkLast(Node<K, E> node) {
        if (isFull()) return false;
        Node<K, E> last = tail_.prev;
        node.prev = last;
        node.next = last.next;
        last.next.prev = node;
        last.next = node;
        tail_.prev = node;
        count.incrementAndGet();
        fullCount.incrementAndGet();
        return true;
    }

    private E unlinkLast() {
        Node<K, E> p = tail_.prev;
        tail_.prev = p.prev;
        p.isReal = false;
        return p.element;
    }

    void unlinkSolidly(Node<K, E> node) {
        Node<K, E> p = node.prev;
        Node<K, E> n = node.next;
        p.next = n;
        n.prev = p;
        node.prev = node;
        node.next = node;
        fullCount.decrementAndGet();
        if (node.isReal) count.decrementAndGet();
    }

    /**
     * @return
     */
    private boolean isFull() {
        return count.intValue() >= capacity || fullCount.intValue() >= Integer.MAX_VALUE;
    }

    // (non-Javadoc)
    // @see java.util.Deque#descendingIterator()
    @Override public Iterator<E> descendingIterator() {
        if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "descendingIterator");
        // XXX must do something
        return null;
    }

    // (non-Javadoc)
    // @see java.util.concurrent.BlockingQueue#drainTo(java.util.Collection)
    @Override public int drainTo(final Collection<? super E> collection) {
        final int cnt = 0;
        return cnt;
    }

    // (non-Javadoc)
    // @see java.util.concurrent.BlockingQueue#drainTo(java.util.Collection, int)
    @Override public int drainTo(final Collection<? super E> collection, final int max) {
        final int cnt = 0;
        return cnt;
    }

    // (non-Javadoc)
    // @see java.util.Deque#getFirst()
    @Override public E getFirst() {
        if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "getFirst");
        // XXX must do something
        return null;
    }

    // (non-Javadoc)
    // @see java.util.Deque#getLast()
    @Override public E getLast() {
        if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "getLast");
        // XXX must do something
        return null;
    }

    // (non-Javadoc)
    // @see com.tmax.probus.dq.collection.IDqMap#get(java.lang.Object)
    @Override public E getValue(final K key) {
        if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "get");
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
    // @see java.util.concurrent.BlockingQueue#offer(java.lang.Object, long, java.util.concurrent.TimeUnit)
    @Override public boolean offer(final E paramE, final long paramLong, final TimeUnit paramTimeUnit)
            throws InterruptedException {
        if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "offer");
        // XXX must do something
        return false;
    }

    // (non-Javadoc)
    // @see java.util.Queue#peek()
    @Override public E peek() {
        return head_.element;
    }

    // (non-Javadoc)
    // @see java.util.Deque#peekFirst()
    @Override public E peekFirst() {
        if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "peekFirst");
        // XXX must do something
        return null;
    }

    // (non-Javadoc)
    // @see java.util.Deque#peekLast()
    @Override public E peekLast() {
        if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "peekLast");
        // XXX must do something
        return null;
    }

    // (non-Javadoc)
    // @see java.util.Queue#poll()
    @Override public E poll() {
        if (count.intValue() == 0) return null;
        return dequeue();
    }

    // (non-Javadoc)
    // @see java.util.concurrent.BlockingQueue#poll(long, java.util.concurrent.TimeUnit)
    @Override public E poll(final long paramLong, final TimeUnit paramTimeUnit)
            throws InterruptedException {
        if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "poll");
        // XXX must do something
        return null;
    }

    // (non-Javadoc)
    // @see java.util.Deque#pollFirst()
    @Override public E pollFirst() {
        if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "pollFirst");
        // XXX must do something
        return null;
    }

    // (non-Javadoc)
    // @see java.util.Deque#pollLast()
    @Override public E pollLast() {
        if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "pollLast");
        // XXX must do something
        return null;
    }

    // (non-Javadoc)
    // @see java.util.Deque#pop()
    @Override public E pop() {
        if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "pop");
        // XXX must do something
        return null;
    }

    // (non-Javadoc)
    // @see java.util.Deque#push(java.lang.Object)
    @Override public void push(final E paramE) {
        if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "push");
        // XXX must do something
        if (logger.isLoggable(FINER)) logger.exiting(getClass().getName(), "push");
    }

    // (non-Javadoc)
    // @see java.util.concurrent.BlockingQueue#remainingCapacity()
    @Override public int remainingCapacity() {
        if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "remainingCapacity");
        // XXX must do something
        return 0;
    }

    // (non-Javadoc)
    // @see java.util.Deque#removeFirst()
    @Override public E removeFirst() {
        if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "removeFirst");
        // XXX must do something
        return null;
    }

    // (non-Javadoc)
    // @see java.util.Deque#removeFirstOccurrence(java.lang.Object)
    @Override public boolean removeFirstOccurrence(final Object paramObject) {
        if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "removeFirstOccurrence");
        // XXX must do something
        return false;
    }

    // (non-Javadoc)
    // @see java.util.Deque#removeLast()
    @Override public E removeLast() {
        if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "removeLast");
        // XXX must do something
        return null;
    }

    // (non-Javadoc)
    // @see java.util.Deque#removeLastOccurrence(java.lang.Object)
    @Override public boolean removeLastOccurrence(final Object paramObject) {
        if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "removeLastOccurrence");
        // XXX must do something
        return false;
    }

    // (non-Javadoc)
    // @see com.tmax.probus.dq.collection.IDqMap#removeItem(java.lang.Object)
    @Override public E removeValue(final K key) {
        if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "removeItem");
        final Node<K, E> node = get(key);
        if (fullCount.intValue() == 0 || node == null) return null;
        final Node<K, E> p = node.prev;
        final Node<K, E> n = node.next;
        if (p != null) node.prev.next = n;
        if (n != null) node.next.prev = p;
        fullCount.decrementAndGet();
        return node.element;
    }

    // (non-Javadoc)
    // @see java.util.AbstractCollection#size()
    @Override public int size() {
        if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "size");
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

    /**
     * @return
     */
    private E dequeue() {
        if (head_.next == null) return null;
        head_ = head_.next;
        return head_.element;
    }

    /**
     * Enqueu.
     * @param e the e
     */
    private void enqueu(final E e) {
        final Node<K, E> node = new Node<K, E>(e);
        node.prev = tail_;
        tail_ = tail_.next = node;
        put(node);
    }

    /**
     * @param key
     * @return
     */
    private Node<K, E> get(final K key) {
        if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "get");
        // XXX must do something
        return null;
    }

    private void put(final Node<K, E> node) {
    }

    static class Node<K, E extends IDqElement<K>> {
        /** The next. */
        Node<K, E> prev;
        /** The next. */
        Node<K, E> next;
        /** The element. */
        final E element;
        boolean isReal = true;

        /**
         * Instantiates a new node.
         * @param obj the obj
         */
        Node(final E obj) {
            element = obj;
        }

        // (non-Javadoc)
        // @see java.lang.Object#equals(java.lang.Object)
        @Override public boolean equals(final Object obj) {
            if (obj == null || element == null) return false;
            if (obj instanceof Node) return element.equals(((Node<?, ?>) obj).element);
            return super.equals(obj);
        }

        // (non-Javadoc)
        // @see java.lang.Object#hashCode()
        @Override public int hashCode() {
            if (element == null) return 0;
            return element.hashCode();
        }

        /**
         * Gets the id.
         * @return the id
         */
        K getId() {
            if (element != null) return element.getId();
            return null;
        }
    }

    // (non-Javadoc)
    // @see java.util.concurrent.BlockingDeque#offerFirst(java.lang.Object, long, java.util.concurrent.TimeUnit)
    @Override public boolean offerFirst(E paramE, long paramLong, TimeUnit paramTimeUnit)
            throws InterruptedException {
        if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "offerFirst");
        // XXX must do something
        return false;
    }

    // (non-Javadoc)
    // @see java.util.concurrent.BlockingDeque#offerLast(java.lang.Object, long, java.util.concurrent.TimeUnit)
    @Override public boolean offerLast(E paramE, long paramLong, TimeUnit paramTimeUnit)
            throws InterruptedException {
        if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "offerLast");
        // XXX must do something
        return false;
    }

    // (non-Javadoc)
    // @see java.util.concurrent.BlockingDeque#takeFirst()
    @Override public E takeFirst() throws InterruptedException {
        if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "takeFirst");
        // XXX must do something
        return null;
    }

    // (non-Javadoc)
    // @see java.util.concurrent.BlockingDeque#takeLast()
    @Override public E takeLast() throws InterruptedException {
        if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "takeLast");
        // XXX must do something
        return null;
    }

    // (non-Javadoc)
    // @see java.util.concurrent.BlockingDeque#pollFirst(long, java.util.concurrent.TimeUnit)
    @Override public E pollFirst(long paramLong, TimeUnit paramTimeUnit)
            throws InterruptedException {
        return null;
    }

    // (non-Javadoc)
    // @see java.util.concurrent.BlockingDeque#pollLast(long, java.util.concurrent.TimeUnit)
    @Override public E pollLast(long paramLong, TimeUnit paramTimeUnit) throws InterruptedException {
        if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "pollLast");
        // XXX must do something
        return null;
    }
}
