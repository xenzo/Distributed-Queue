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

import java.lang.reflect.Array;
import java.util.AbstractQueue;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;


/**
 * The Class DqBackupQueue.
 * @param <K> the key type
 * @param <E> the element type
 */
public class AbstractDqQueue<K, E extends IDqElement<K>>
        extends AbstractQueue<E> implements IDqQueue<K, E> {
    /** Logger for this class. */
    private final transient Logger logger = Logger.getLogger("com.tmax.probus.dq.collection");
    /** The repo_. */
    private final ConcurrentMap<K, Node<K, E>> repo_ = new ConcurrentHashMap<K, Node<K, E>>();
    /** The head_. */
    private final Node<K, E> head_;
    /** The tail_. */
    private final Node<K, E> tail_;
    /** The capacity. */
    private int capacity;
    /** The count. */
    private final AtomicInteger count = new AtomicInteger(0);
    /** The full count. */
    private final AtomicInteger fullCount = new AtomicInteger(0);
    /** The lock_. */
    private final Lock lock_ = new ReentrantLock();
    /** Wait queue for waiting takes. */
    private final Condition notEmpty = lock_.newCondition();
    /** Wait queue for waiting puts. */
    private final Condition notFull = lock_.newCondition();
    /** The NULL node. */
    private final transient Node<K, E> NULL_NODE = new Node<K, E>(null);
    {
        NULL_NODE.prev = NULL_NODE.next = NULL_NODE;
        NULL_NODE.isReal = false;
    }

    /**
     * Instantiates a new dq backup queue.
     */
    protected AbstractDqQueue() {
        this(Integer.MAX_VALUE);
    }

    /**
     * Instantiates a new dq backup queue.
     * @param maxValue the max value
     */
    protected AbstractDqQueue(final int maxValue) {
        if (maxValue <= 0) throw new IllegalArgumentException();
        capacity = maxValue;
        head_ = tail_ = NULL_NODE;
    }

    // (non-Javadoc)
    // @see java.util.AbstractQueue#addAll(java.util.Collection)
    @Override public boolean addAll(final Collection<? extends E> collection) {
        if (collection == null) throw new NullPointerException();
        if (collection == this) throw new IllegalArgumentException();
        final Lock lock = lock_;
        lock.lock();
        try {
            boolean modified = false;
            for (final E e : collection) {
                if (linkLast(new Node<K, E>(e))) modified = true;
            }
            return modified;
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
    // @see java.util.Deque#addLast(java.lang.Object)
    @Override public void addLast(final E e) {
        if (!offerLast(e)) throw new IllegalStateException("FULL");
    }

    // (non-Javadoc)
    // @see java.util.AbstractQueue#clear()
    @Override public void clear() {
        final Lock lock = lock_;
        lock.lock();
        try {
            head_.next = tail_.prev = NULL_NODE;
            count.set(0);
            notFull.signalAll();
        } finally {
            lock.unlock();
        }
    }

    // (non-Javadoc)
    // @see java.util.AbstractCollection#contains(java.lang.Object)
    @Override public boolean contains(final Object o) {
        if (o == null) return false;
        final Node<K, E> node = repo_.get(((E) o).getId());
        return node != null && node.isReal;
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
        return drainTo(collection, Integer.MAX_VALUE);
    }

    // (non-Javadoc)
    // @see java.util.concurrent.BlockingQueue#drainTo(java.util.Collection, int)
    @Override public int drainTo(final Collection<? super E> collection, final int max) {
        if (collection == null) throw new NullPointerException();
        if (collection == this) throw new IllegalArgumentException();
        final Lock lock = lock_;
        lock.lock();
        try {
            final int n = Math.min(max, count.intValue());
            for (int i = 0; i < n; i++) {
                collection.add(head_.next.element);
                unlinkFirst();
            }
            return n;
        } finally {
            lock.unlock();
        }
    }

    // (non-Javadoc)
    // @see com.tmax.probus.dq.collection.IDqMap#get(java.lang.Object)
    @Override public E findReal(final K key) {
        final Node<K, E> node = repo_.get(key);
        if (node == null) return null;
        return node.element;
    }

    // (non-Javadoc)
    // @see com.tmax.probus.dq.collection.IDqQueue#FullSize()
    @Override public int fullSize() {
        return fullCount.intValue();
    }

    // (non-Javadoc)
    // @see java.util.Deque#getFirst()
    @Override public E getFirst() {
        final E e = peekFirst();
        if (e == null) throw new NoSuchElementException();
        return e;
    }

    // (non-Javadoc)
    // @see java.util.Deque#getLast()
    @Override public E getLast() {
        final E e = peekLast();
        if (e == null) throw new NoSuchElementException();
        return e;
    }

    // (non-Javadoc)
    // @see java.util.AbstractCollection#iterator()
    @Override public Iterator<E> iterator() {
        if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "iterator");
        // XXX must do something
        return null;
    }

    // (non-Javadoc)
    // @see java.util.Queue#offer(java.lang.Object)
    @Override public boolean offer(final E e) {
        return offerLast(e);
    }

    // (non-Javadoc)
    // @see java.util.concurrent.BlockingQueue#offer(java.lang.Object, long, java.util.concurrent.TimeUnit)
    @Override public boolean offer(final E e, final long timeout, final TimeUnit unit)
            throws InterruptedException {
        return offerLast(e, timeout, unit);
    }

    // (non-Javadoc)
    // @see java.util.Deque#offerFirst(java.lang.Object)
    @Override public boolean offerFirst(final E e) {
        if (e == null) throw new NullPointerException();
        final Node<K, E> node = new Node<K, E>(e);
        final Lock lock = lock_;
        lock.lock();
        try {
            return linkFirst(node);
        } finally {
            lock.unlock();
        }
    }

    // (non-Javadoc)
    // @see java.util.concurrent.BlockingDeque#offerFirst(java.lang.Object, long, java.util.concurrent.TimeUnit)
    @Override public boolean offerFirst(final E e, final long timeout, final TimeUnit unit)
            throws InterruptedException {
        if (e == null) throw new NullPointerException();
        final Node<K, E> node = new Node<K, E>(e);
        long nanos = unit.toNanos(timeout);
        final Lock lock = lock_;
        lock.lockInterruptibly();
        try {
            while (!linkFirst(node)) {
                if (nanos <= 0) return false;
                nanos = notFull.awaitNanos(nanos);
            }
            return true;
        } finally {
            lock.unlock();
        }
    }

    // (non-Javadoc)
    // @see java.util.Deque#offerLast(java.lang.Object)
    @Override public boolean offerLast(final E e) {
        if (e == null) throw new NullPointerException();
        final Node<K, E> node = new Node<K, E>(e);
        final Lock lock = lock_;
        lock.lock();
        try {
            return linkLast(node);
        } finally {
            lock.unlock();
        }
    }

    // (non-Javadoc)
    // @see java.util.concurrent.BlockingDeque#offerLast(java.lang.Object, long, java.util.concurrent.TimeUnit)
    @Override public boolean offerLast(final E e, final long timeout, final TimeUnit unit)
            throws InterruptedException {
        if (e == null) throw new NullPointerException();
        final Node<K, E> node = new Node<K, E>(e);
        long nanos = unit.toNanos(timeout);
        final Lock lock = lock_;
        lock.lockInterruptibly();
        try {
            while (!linkLast(node)) {
                if (nanos <= 0) return false;
                nanos = notFull.awaitNanos(nanos);
            }
            return true;
        } finally {
            lock.unlock();
        }
    }

    // (non-Javadoc)
    // @see java.util.Queue#peek()
    @Override public E peek() {
        return peekFirst();
    }

    // (non-Javadoc)
    // @see java.util.Deque#peekFirst()
    @Override public E peekFirst() {
        final Lock lock = lock_;
        lock.lock();
        try {
            return head_.next.element;
        } finally {
            lock.unlock();
        }
    }

    // (non-Javadoc)
    // @see java.util.Deque#peekLast()
    @Override public E peekLast() {
        final Lock lock = lock_;
        lock.lock();
        try {
            return tail_.prev.element;
        } finally {
            lock.unlock();
        }
    }

    // (non-Javadoc)
    // @see java.util.Queue#poll()
    @Override public E poll() {
        return pollFirst();
    }

    // (non-Javadoc)
    // @see java.util.concurrent.BlockingQueue#poll(long, java.util.concurrent.TimeUnit)
    @Override public E poll(final long timeout, final TimeUnit unit)
            throws InterruptedException {
        return pollFirst(timeout, unit);
    }

    // (non-Javadoc)
    // @see java.util.Deque#pollFirst()
    @Override public E pollFirst() {
        final Lock lock = lock_;
        lock.lock();
        try {
            return unlinkFirst();
        } finally {
            lock.unlock();
        }
    }

    // (non-Javadoc)
    // @see java.util.concurrent.BlockingDeque#pollFirst(long, java.util.concurrent.TimeUnit)
    @Override public E pollFirst(final long timeout, final TimeUnit unit)
            throws InterruptedException {
        long nanos = unit.toNanos(timeout);
        final Lock lock = lock_;
        lock.lockInterruptibly();
        try {
            E e;
            while ((e = unlinkFirst()) == null) {
                if (nanos <= 0) return null;
                nanos = notEmpty.awaitNanos(nanos);
            }
            return e;
        } finally {
            lock.unlock();
        }
    }

    // (non-Javadoc)
    // @see java.util.Deque#pollLast()
    @Override public E pollLast() {
        final Lock lock = lock_;
        lock.lock();
        try {
            return unlinkLast();
        } finally {
            lock.unlock();
        }
    }

    // (non-Javadoc)
    // @see java.util.concurrent.BlockingDeque#pollLast(long, java.util.concurrent.TimeUnit)
    @Override public E pollLast(final long timeout, final TimeUnit unit)
            throws InterruptedException {
        long nanos = unit.toNanos(timeout);
        final Lock lock = lock_;
        lock.lockInterruptibly();
        try {
            E e;
            while ((e = unlinkLast()) == null) {
                if (nanos <= 0) return null;
                nanos = notEmpty.awaitNanos(nanos);
            }
            return e;
        } finally {
            lock.unlock();
        }
    }

    // (non-Javadoc)
    // @see java.util.Deque#pop()
    @Override public E pop() {
        return removeFirst();
    }

    // (non-Javadoc)
    // @see java.util.Deque#push(java.lang.Object)
    @Override public void push(final E e) {
        addFirst(e);
    }

    // (non-Javadoc)
    // @see java.util.concurrent.BlockingQueue#put(java.lang.Object)
    @Override public void put(final E e) throws InterruptedException {
        putLast(e);
    }

    // (non-Javadoc)
    // @see java.util.concurrent.BlockingDeque#putFirst(java.lang.Object)
    @Override public void putFirst(final E e) throws InterruptedException {
        if (e == null) throw new NullPointerException();
        final Node<K, E> node = new Node<K, E>(e);
        final Lock lock = lock_;
        lock.lock();
        try {
            while (!linkFirst(node))
                notFull.await();
        } finally {
            lock.unlock();
        }
    }

    // (non-Javadoc)
    // @see java.util.concurrent.BlockingDeque#putLast(java.lang.Object)
    @Override public void putLast(final E e) throws InterruptedException {
        if (e == null) throw new NullPointerException();
        final Node<K, E> node = new Node<K, E>(e);
        final Lock lock = lock_;
        lock.lock();
        try {
            while (!linkLast(node))
                notFull.await();
        } finally {
            lock.unlock();
        }
    }

    // (non-Javadoc)
    // @see java.util.concurrent.BlockingQueue#remainingCapacity()
    @Override public int remainingCapacity() {
        return capacity - count.intValue();
    }

    // (non-Javadoc)
    // @see java.util.AbstractCollection#remove(java.lang.Object)
    @Override public boolean remove(final Object o) {
        return removeFirstOccurrence(o);
    }

    // (non-Javadoc)
    // @see java.util.Deque#removeFirst()
    @Override public E removeFirst() {
        final E e = pollFirst();
        if (e == null) throw new NoSuchElementException();
        return e;
    }

    // (non-Javadoc)
    // @see java.util.concurrent.BlockingDeque#removeFirstOccurrence(java.lang.Object)
    @Override public boolean removeFirstOccurrence(final Object o) {
        if (o == null) return false;
        final Lock lock = lock_;
        lock.lock();
        try {
            final Node<K, E> node = repo_.get(((E) o).getId());
            if (node == null || !node.isReal) return false;
            unlinkSolidly(node);
            return linkFirst(node) && unlinkFirst() != null;
        } finally {
            lock.unlock();
        }
    }

    // (non-Javadoc)
    // @see java.util.Deque#removeLast()
    @Override public E removeLast() {
        final E e = pollLast();
        if (e == null) throw new NoSuchElementException();
        return e;
    }

    // (non-Javadoc)
    // @see java.util.concurrent.BlockingDeque#removeLastOccurrence(java.lang.Object)
    @Override public boolean removeLastOccurrence(final Object o) {
        if (o == null) return false;
        final Lock lock = lock_;
        lock.lock();
        try {
            final Node<K, E> node = repo_.get(((E) o).getId());
            if (node == null || !node.isReal) return false;
            unlinkSolidly(node);
            return linkLast(node) && unlinkLast() != null;
        } finally {
            lock.unlock();
        }
    }

    // (non-Javadoc)
    // @see com.tmax.probus.dq.collection.IDqMap#removeItem(java.lang.Object)
    @Override public E removeReal(final K key) {
        final Node<K, E> node = repo_.get(key);
        if (node == null) throw new NoSuchElementException();
        unlinkSolidly(node);
        return node.element;
    }

    // (non-Javadoc)
    // @see java.util.AbstractCollection#size()
    @Override public int size() {
        return count.intValue();
    }

    // (non-Javadoc)
    // @see java.util.concurrent.BlockingQueue#take()
    @Override public E take() throws InterruptedException {
        return takeFirst();
    }

    // (non-Javadoc)
    // @see java.util.concurrent.BlockingDeque#takeFirst()
    @Override public E takeFirst() throws InterruptedException {
        final Lock lock = lock_;
        lock.lock();
        try {
            E e;
            while ((e = unlinkFirst()) == null)
                notEmpty.await();
            return e;
        } finally {
            lock.unlock();
        }
    }

    // (non-Javadoc)
    // @see java.util.concurrent.BlockingDeque#takeLast()
    @Override public E takeLast() throws InterruptedException {
        final Lock lock = lock_;
        lock.lock();
        try {
            E e;
            while ((e = unlinkLast()) == null)
                notEmpty.await();
            return e;
        } finally {
            lock.unlock();
        }
    }

    // (non-Javadoc)
    // @see java.util.AbstractCollection#toArray()
    @Override public Object[] toArray() {
        final Lock lock = lock_;
        lock.lock();
        try {
            final Object[] a = new Object[count.intValue()];
            int k = 0;
            for (Node<K, E> n = head_.next; n != NULL_NODE; n = n.next)
                a[k++] = n.element;
            return a;
        } finally {
            lock.unlock();
        }
    }

    // (non-Javadoc)
    // @see java.util.AbstractCollection#toArray(T[])
    @Override public <T extends Object> T[] toArray(T[] a) {
        final Lock lock = lock_;
        lock.lock();
        try {
            if (a.length < count.intValue()) a = (T[]) Array.newInstance(a.getClass().getComponentType(), count.intValue());
            int k = 0;
            for (Node<K, E> n = head_.next; n != NULL_NODE; n = n.next)
                a[k++] = (T) n.element;
            if (a.length > k) a[k] = null;
            return a;
        } finally {
            lock.unlock();
        }
    }

    // (non-Javadoc)
    // @see java.util.AbstractCollection#toString()
    @Override public String toString() {
        final Lock lock = lock_;
        lock.lock();
        try {
            Node<K, E> first = head_.next;
            if (first == NULL_NODE) return "[]";
            final StringBuilder sb = new StringBuilder("[");
            while (true) {
                final E e = first.element;
                sb.append(e == this ? "(this Collection)" : e);
                first = first.next;
                if (first == NULL_NODE) return sb.append("]").toString();
                sb.append(',').append(' ');
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * Unlink solidly.
     * @param node the node
     */
    void unlinkSolidly(final Node<K, E> node) {
        if (node == NULL_NODE) throw new IllegalArgumentException();
        final Lock lock = lock_;
        lock.lock();
        try {
            final Node<K, E> existNode = repo_.get(node.getId());
            if (existNode == null) throw new NoSuchElementException();
            final Node<K, E> p = node.prev;
            final Node<K, E> n = node.next;
            p.next = n;
            n.prev = p;
            node.prev = node;
            node.next = node;
            fullCount.decrementAndGet();
            if (node.isReal) count.decrementAndGet();
            node.isReal = false;
            notFull.signal();
        } finally {
            lock.unlock();
        }
    };

    /**
     * Checks if is full.
     * @return true, if is full
     */
    private boolean isFull() {
        return count.intValue() >= capacity || fullCount.intValue() >= Integer.MAX_VALUE;
    }

    /**
     * Link first.
     * @param node the node
     * @return true, if successful
     */
    private boolean linkFirst(final Node<K, E> node) {
        if (isFull()) return false;
        if (node == NULL_NODE) throw new IllegalArgumentException();
        final Node<K, E> existNode = repo_.putIfAbsent(node.getId(), node);
        if (existNode != null) throw new IllegalArgumentException();
        final Node<K, E> first = head_.next;
        node.next = first;
        node.prev = first.prev;
        first.prev.next = node;
        first.prev = node;
        head_.next = node;
        count.incrementAndGet();
        fullCount.incrementAndGet();
        notEmpty.signal();
        return true;
    }

    /**
     * Link last.
     * @param node the node
     * @return true, if successful
     */
    private boolean linkLast(final Node<K, E> node) {
        if (isFull()) return false;
        if (node == NULL_NODE) throw new IllegalArgumentException();
        final Node<K, E> existNode = repo_.putIfAbsent(node.getId(), node);
        if (existNode != null) throw new IllegalArgumentException("EXISTS");
        final Node<K, E> last = tail_.prev;
        node.prev = last;
        node.next = last.next;
        last.next.prev = node;
        last.next = node;
        tail_.prev = node;
        count.incrementAndGet();
        fullCount.incrementAndGet();
        notEmpty.signal();
        return true;
    }

    /**
     * Unlink first.
     * @return the e
     */
    private E unlinkFirst() {
        final Node<K, E> h = head_.next;
        head_.next = h.next;
        h.isReal = false;
        notFull.signal();
        return h.element;
    }

    /**
     * Unlink last.
     * @return the e
     */
    private E unlinkLast() {
        final Node<K, E> p = tail_.prev;
        tail_.prev = p.prev;
        p.isReal = false;
        notFull.signal();
        return p.element;
    }

    /**
     * The Class Node.
     * @param <K> the key type
     * @param <E> the element type
     */
    static class Node<K, E extends IDqElement<K>> {
        /** The next. */
        Node<K, E> prev;
        /** The next. */
        Node<K, E> next;
        /** The element. */
        final E element;
        /** The is real. */
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
}
