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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.AbstractQueue;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;


/**
 * A factory for creating DqCollection objects.
 * @since 1.5
 * @author Kim, Dong iL
 * @see IDqSolidOperator
 * @see IDqMap
 * @see IDqStack
 * @see IDqElement
 */
public class DqCollections {
    /**
     * cast to specified collection.
     * @param <T> the generic type
     * @param <E> the element type
     * @param original the original
     * @param claz the claz
     * @return the t
     */
    public static <T, E> T convert2Collection(final Collection<E> original, final Class<T> claz) {
        return claz.cast(original);
    }

    /**
     * cast to IDqSolidOperator.
     * @param <K> the key type
     * @param <E> the element type
     * @param collection the collection
     * @return the i dq solid operator
     */
    @SuppressWarnings("unchecked") public static <K, E extends IDqElement<K>> IDqSolidOperator<K, E> convert2SolidOperator(final Collection<E> collection) {
        return convert2Collection(collection, IDqSolidOperator.class);
    }

    /**
     * new Blocking Deque.
     * @param <E> the element type
     * @param id the id
     * @return the blocking deque
     */
    public static <E extends IDqElement<String>> BlockingDeque<E> newBlockingDeque(final String id) {
        return new DqCollection<String, E>(id);
    }

    /**
     * new Blocking Deque.
     * @param <E> the element type
     * @param id the id
     * @param maxSize the max size
     * @param maxCapacity the max capacity
     * @return the blocking deque
     */
    public static <E extends IDqElement<String>> BlockingDeque<E> newBlockingDeque(final String id, final int maxSize
            , final int maxCapacity) {
        return new DqCollection<String, E>(id, maxSize, maxCapacity);
    }

    /**
     * new Blocking Deque.
     * @param <E> the element type
     * @param id the id
     * @param listener the listener
     * @param maxSize the max size
     * @param maxCapacity the max capacity
     * @return the blocking deque
     */
    public static <E extends IDqElement<String>> BlockingDeque<E> newBlockingDeque(final String id, final int maxSize
            , final int maxCapacity, final IDqItemEventListener<E> listener) {
        return new DqCollection<String, E>(id, listener, maxSize, maxCapacity);
    }

    /**
     * new Blocking Queue.
     * @param <E> the element type
     * @param id the id
     * @return the blocking queue
     */
    public static <E extends IDqElement<String>> BlockingQueue<E> newBlockingQueue(final String id) {
        return new DqCollection<String, E>(id);
    }

    /**
     * new Blocking Queue.
     * @param <E> the element type
     * @param id the id
     * @param maxSize the max size
     * @param maxCapacity the max capacity
     * @return the blocking queue
     */
    public static <E extends IDqElement<String>> BlockingQueue<E> newBlockingQueue(final String id, final int maxSize
            , final int maxCapacity) {
        return new DqCollection<String, E>(id, maxSize, maxCapacity);
    }

    /**
     * new Blocking Queue.
     * @param <E> the element type
     * @param id the id
     * @param listener the listener
     * @param maxSize the max size
     * @param maxCapacity the max capacity
     * @return the blocking queue
     */
    public static <E extends IDqElement<String>> BlockingQueue<E> newBlockingQueue(final String id, final int maxSize
            , final int maxCapacity, final IDqItemEventListener<E> listener) {
        return new DqCollection<String, E>(id, listener, maxSize, maxCapacity);
    }

    /**
     * new Map.
     * @param <K> the key type
     * @param <E> the element type
     * @param id the id
     * @return the i dq map
     */
    public static <K, E extends IDqElement<K>> IDqMap<K, E> newDqMap(final String id) {
        return new DqCollection<K, E>(id);
    }

    /**
     * new Map.
     * @param <K> the key type
     * @param <E> the element type
     * @param id the id
     * @param maxSize the max size
     * @param maxCapacity the max capacity
     * @return the i dq map
     */
    public static <K, E extends IDqElement<K>> IDqMap<K, E> newDqMap(final String id, final int maxSize, final int maxCapacity) {
        return new DqCollection<K, E>(id, maxSize, maxCapacity);
    }

    /**
     * new Map.
     * @param <K> the key type
     * @param <E> the element type
     * @param id the id
     * @param listener the listener
     * @param maxSize the max size
     * @param maxCapacity the max capacity
     * @return the i dq map
     */
    public static <K, E extends IDqElement<K>> IDqMap<K, E> newDqMap(final String id, final int maxSize, final int maxCapacity
            , final IDqItemEventListener<E> listener) {
        return new DqCollection<K, E>(id, listener, maxSize, maxCapacity);
    }

    /**
     * new Stack.
     * @param <E> the element type
     * @param id the id
     * @return the i dq stack
     */
    public static <E extends IDqElement<String>> IDqStack<E> newDqStack(final String id) {
        return new DqCollection<String, E>(id);
    }

    /**
     * new Stack.
     * @param <E> the element type
     * @param id the id
     * @param maxSize the max size
     * @param maxCapacity the max capacity
     * @return the i dq stack
     */
    public static <E extends IDqElement<String>> IDqStack<E> newDqStack(final String id, final int maxSize, final int maxCapacity) {
        return new DqCollection<String, E>(id, maxSize, maxCapacity);
    }

    /**
     * new Stack.
     * @param <E> the element type
     * @param id the id
     * @param listener the listener
     * @param maxSize the max size
     * @param maxCapacity the max capacity
     * @return the i dq stack
     */
    public static <E extends IDqElement<String>> IDqStack<E> newDqStack(final String id, final int maxSize, final int maxCapacity
            , final IDqItemEventListener<E> listener) {
        return new DqCollection<String, E>(id, listener, maxSize, maxCapacity);
    }

    /**
     * Distributed SQM 구현을 위한 Queue 컬렉션이다. LinkedBlockingDeque를 참고하여 만들었으므로 참고하기
     * 바란다.
     * <p/>
     * 다른 점은 이 클래스에 있는 LinkedBlockingDeque에 존재하는 메소드들은 데이터의 추가는 하지만 삭제는 하지 않고
     * 삭제하는 척만 한다.</br> 데이터의 삭제는 내부에 정의한 Map을 통하여 일어난다. 이렇게 한 이유는 큐에 들어온 데이터를
     * 원격지로 전송하고 응답을 받은 후에 삭제해야 하기 때문이다. <br/>
     * 삭제 작업이 Queue처럼 head, tail에서만 이루어지는 것이 아니라 응답에 따라 무작위로 일어나기 때문에 Map을 사용했다.
     * <br/>
     * 데이터 추가시에는 Map과 Queue에 저장되고 데이터 제거시에는 head와 tail의 참조값만 변경하여 데이터가 제거된 것처럼
     * 보이게 하고 실제로 응답이 도착하면 데이터를 제거한다.
     * <p/>
     * 또 한가지는 원형큐 형태로 정의되어 있다는 것이다.<br/>
     * head와 tail은 하나의 같은 빈 노드이고 이 값은 바뀌지 않고 head의 next값과 tail의 prev 값만 변경하여
     * position을 바꾼다.
     * <p/>
     * 그리고 주의 할 점은 큐에 데이터가 추가되고 완전히 비워지면 새로운 Node의 리스트가 생성될 수 있다는 것이다. 왜냐하면 데이터는
     * 응답을 전송 받은 후에 삭제하기 때문에 Node 리스트가 계속 존재하는데 논리적 삭제로 인해서 head와 tail이 초기화 상태에서
     * 다시 데이터를 받게 되면 새로운 Node 리스트가 생성되고 기존에 남겨진 Node들은 내부의 Map을 통해서만 접근 가능하게 된다.
     * <br/>
     * 또 중복 데이터의 추가가 불가능하다. 응답이 온 경우 중복데이터의 삭제가 애매하여 그렇게 하였다.
     * @param <K> the key type
     * @param <E> the element type
     * @author Kim, Dong iL
     * @see java.util.concurrent.LinkedBlockingQueue
     * @see java.util.concurrent.LinkedBlockingDeque
     */
    private static final class DqCollection<K, E extends IDqElement<K>> extends AbstractQueue<E>
            implements IDqSolidOperator<K, E>, IDqMap<K, E>, IDqStack<E>, IDqDeque<K, E>,
            Serializable {
        private static final long serialVersionUID = 771471529351045470L;
        /** The Constant NANO_BASE. */
        private static final long NANO_BASE = System.nanoTime();
        private final transient Logger logger = Logger.getLogger("com.tmax.probus.dq.collection");
        /** The id. */
        final String id_;
        /** 아이템 추가/제거시 호출되는 리스너. */
        private IDqItemEventListener<E> listener_;
        /** The repo */
        private final transient ConcurrentMap<K, Node<K, E>> repo_;
        /** 큐의 head이다. */
        private final transient Node<K, E> head_;
        /** The tail. */
        private final transient Node<K, E> tail_;
        /** The maximum size. */
        private int maxSize_;
        /** head와 tail 사이의 노드 갯수. */
        private final transient AtomicInteger count_ = new AtomicInteger(0);
        /** repo에 존재하는 전체 노드 갯수 */
        private final transient AtomicInteger fullCount_ = new AtomicInteger(0);
        private final transient Lock lock_ = new ReentrantLock();
        /** Wait queue for waiting takes. */
        private final transient Condition notEmpty_ = lock_.newCondition();
        /** Wait queue for waiting puts. */
        private final transient Condition notFull_ = lock_.newCondition();
        /** The NULL node. head/tail is this. */
        private final transient Node<K, E> NULL_NODE = new Node<K, E>(null);
        {
            NULL_NODE.prev = NULL_NODE.next = NULL_NODE;
            NULL_NODE.setReal(false);
        }

        private DqCollection(final String id) {
            this(id, null, Integer.MAX_VALUE, 128);
        }

        private DqCollection(final String id, final IDqItemEventListener<E> listener, final int maxSize, final int mapCapacity) {
            if (id == null || maxSize <= 0 || mapCapacity <= 0) throw new IllegalArgumentException();
            id_ = id;
            listener_ = (listener == null ? new JustDoNotEventListener() : listener);
            repo_ = new ConcurrentHashMap<K, Node<K, E>>(mapCapacity);
            maxSize_ = maxSize;
            head_ = tail_ = NULL_NODE;
        }

        private DqCollection(final String id, final int maxSize, final int mapCapacity) {
            this(id, null, maxSize, mapCapacity);
        }

        // (non-Javadoc)
        // @see java.util.AbstractQueue#addAll(java.util.Collection)
        @Override public final boolean addAll(final Collection<? extends E> collection) {
            if (collection == null) throw new NullPointerException();
            if (collection == this) throw new IllegalArgumentException();
            final Lock lock = lock_;
            lock.lock();
            try {
                boolean modified = false;
                for (final E e : collection)
                    if (linkLast(newNode(e))) modified = true;
                return modified;
            } finally {
                lock.unlock();
            }
        }

        // (non-Javadoc)
        // @see java.util.concurrent.BlockingDeque#addFirst(java.lang.Object)
        @Override public final void addFirst(final E e) {
            if (!offerFirst(e)) throw new IllegalStateException("FULL-or-EXISTS");
        }

        // (non-Javadoc)
        // @see java.util.concurrent.BlockingDeque#addLast(java.lang.Object)
        @Override public final void addLast(final E e) {
            if (!offerLast(e)) throw new IllegalStateException("FULL-or-EXISTS");
        }

        // (non-Javadoc)
        // @see java.util.AbstractQueue#clear()
        @Override public final void clear() {
            final Lock lock = lock_;
            lock.lock();
            try {
                head_.next = tail_.prev = NULL_NODE;
                count_.set(0);
                notFull_.signalAll();
            } finally {
                lock.unlock();
            }
        }

        // (non-Javadoc)
        // @see com.tmax.probus.dq.collection.IDqSolidOperator#removeTimedOutSolidly(long, java.util.concurrent.TimeUnit)
        @Override public List<E> clearTimedOutSolidly(final long timeout, final TimeUnit unit) {
            if (logger.isLoggable(FINER)) logger.entering("DqCollection", "clearTimedOutSolidly(long=" + timeout + ", TimeUnit=" + unit + ")", "start");
            if (timeout < 0) throw new IllegalArgumentException();
            final List<E> olds = new ArrayList<E>();
            final long limit = unit.toNanos(timeout);
            final Lock lock = lock_;
            lock.lock();
            try {
                for (final Node<K, E> node : repo_.values())
                    if (node.getElapsedTime() > limit && !node.isReal()) olds.add(unlinkSolidly(node));
            } finally {
                lock.unlock();
            }
            if (logger.isLoggable(FINER)) logger.exiting("DqCollection", "clearTimedOutSolidly(long, TimeUnit)", "end - return value=" + olds);
            return olds;
        }

        // (non-Javadoc)
        // @see java.util.AbstractCollection#contains(java.lang.Object)
        @Override public final boolean contains(final Object o) {
            if (o == null) return false;
            @SuppressWarnings("unchecked") final Node<K, E> node = repo_.get(((E) o).getId());
            return node != null && node.isReal();
        }

        // (non-Javadoc)
        // @see java.util.Deque#descendingIterator()
        @Override public final Iterator<E> descendingIterator() {
            return new DescendingItr();
        }

        // (non-Javadoc)
        // @see java.util.concurrent.BlockingQueue#drainTo(java.util.Collection)
        @Override public final int drainTo(final Collection<? super E> collection) {
            return drainTo(collection, Integer.MAX_VALUE);
        }

        // (non-Javadoc)
        // @see java.util.concurrent.BlockingQueue#drainTo(java.util.Collection, int)
        @Override public final int drainTo(final Collection<? super E> collection, final int max) {
            if (collection == null) throw new NullPointerException();
            if (collection == this) throw new IllegalArgumentException();
            final Lock lock = lock_;
            lock.lock();
            try {
                final int n = Math.min(max, count_.intValue());
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
        // @see com.tmax.probus.dq.collection.IDqQueue#eliminate(java.lang.Object)
        @Override public E eliminate(final K key) {
            return removeSolidly(key);
        }

        // (non-Javadoc)
        // @see com.tmax.probus.dq.collection.IDqSolidOperator#findSolidly(java.lang.Object)
        @Override public final E findSolidly(final K key) {
            final Node<K, E> node = repo_.get(key);
            if (node == null) return null;
            return node.element;
        }

        // (non-Javadoc)
        // @see com.tmax.probus.dq.collection.IDqSolidOperator#fullSize()
        @Override public final int fullSize() {
            return fullCount_.intValue();
        }

        // (non-Javadoc)
        // @see com.tmax.probus.dq.collection.IDqMap#get(java.lang.Object)
        @Override public final E get(final K key) {
            final Node<K, E> node = repo_.get(key);
            if (node == null || !node.isReal()) return null;
            return node.element;
        }

        // (non-Javadoc)
        // @see com.tmax.probus.dq.collection.IDqQueue#getExpiredList()
        @Override public List<E> getExpiredList() {
            return getTimedOutSolidly(0, TimeUnit.NANOSECONDS);
        }

        // (non-Javadoc)
        // @see java.util.Deque#getFirst()
        @Override public final E getFirst() {
            final E e = peekFirst();
            if (e == null) throw new NoSuchElementException();
            return e;
        }

        /**
         * Gets the id.
         * @return the id
         */
        public final String getId() {
            return id_;
        }

        // (non-Javadoc)
        // @see java.util.Deque#getLast()
        @Override public final E getLast() {
            final E e = peekLast();
            if (e == null) throw new NoSuchElementException();
            return e;
        }

        // (non-Javadoc)
        // @see com.tmax.probus.dq.collection.IDqSolidOperator#getTimedOutSolidly(long, java.util.concurrent.TimeUnit)
        @Override public List<E> getTimedOutSolidly(final long timeout, final TimeUnit unit) {
            if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "getTimedOutSolidly");
            if (timeout < 0) throw new IllegalArgumentException();
            final List<E> olds = new ArrayList<E>();
            final long limit = unit.toNanos(timeout);
            final Lock lock = lock_;
            lock.lock();
            try {
                for (final Node<K, E> node : repo_.values())
                    if (node.getElapsedTime() > limit && !node.isReal()) olds.add(node.element);
            } finally {
                lock.unlock();
            }
            if (logger.isLoggable(FINER)) logger.exiting(getClass().getName(), "getTimedOutSolidly");
            return olds;
        }

        // (non-Javadoc)
        // @see java.util.AbstractCollection#iterator()
        @Override public final Iterator<E> iterator() {
            return new Itr();
        }

        // (non-Javadoc)
        // @see java.util.Queue#offer(java.lang.Object)
        @Override public final boolean offer(final E e) {
            return offerLast(e);
        }

        // (non-Javadoc)
        // @see java.util.concurrent.BlockingDeque#offer(java.lang.Object, long, java.util.concurrent.TimeUnit)
        @Override public final boolean offer(final E e, final long timeout, final TimeUnit unit)
                throws InterruptedException {
            return offerLast(e, timeout, unit);
        }

        // (non-Javadoc)
        // @see java.util.concurrent.BlockingDeque#offerFirst(java.lang.Object)
        @Override public final boolean offerFirst(final E e) {
            final Node<K, E> node = newNode(e);
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
        @Override public final boolean offerFirst(final E e, final long timeout, final TimeUnit unit)
                throws InterruptedException {
            final Node<K, E> node = newNode(e);
            long nanos = unit.toNanos(timeout);
            final Lock lock = lock_;
            lock.lockInterruptibly();
            try {
                while (!linkFirst(node)) {
                    if (nanos <= 0) return false;
                    nanos = notFull_.awaitNanos(nanos);
                }
                return true;
            } finally {
                lock.unlock();
            }
        }

        // (non-Javadoc)
        // @see java.util.concurrent.BlockingDeque#offerLast(java.lang.Object)
        @Override public final boolean offerLast(final E e) {
            final Node<K, E> node = newNode(e);
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
        @Override public final boolean offerLast(final E e, final long timeout, final TimeUnit unit)
                throws InterruptedException {
            final Node<K, E> node = newNode(e);
            long nanos = unit.toNanos(timeout);
            final Lock lock = lock_;
            lock.lockInterruptibly();
            try {
                while (!linkLast(node)) {
                    if (nanos <= 0) return false;
                    nanos = notFull_.awaitNanos(nanos);
                }
                return true;
            } finally {
                lock.unlock();
            }
        }

        // (non-Javadoc)
        // @see java.util.Queue#peek()
        @Override public final E peek() {
            return peekFirst();
        }

        // (non-Javadoc)
        // @see java.util.Deque#peekFirst()
        @Override public final E peekFirst() {
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
        @Override public final E peekLast() {
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
        @Override public final E poll() {
            return pollFirst();
        }

        // (non-Javadoc)
        // @see java.util.concurrent.BlockingDeque#poll(long, java.util.concurrent.TimeUnit)
        @Override public final E poll(final long timeout, final TimeUnit unit)
                throws InterruptedException {
            return pollFirst(timeout, unit);
        }

        // (non-Javadoc)
        // @see java.util.Deque#pollFirst()
        @Override public final E pollFirst() {
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
        @Override public final E pollFirst(final long timeout, final TimeUnit unit)
                throws InterruptedException {
            long nanos = unit.toNanos(timeout);
            final Lock lock = lock_;
            lock.lockInterruptibly();
            try {
                E e;
                while ((e = unlinkFirst()) == null) {
                    if (nanos <= 0) return null;
                    nanos = notEmpty_.awaitNanos(nanos);
                }
                return e;
            } finally {
                lock.unlock();
            }
        }

        // (non-Javadoc)
        // @see java.util.Deque#pollLast()
        @Override public final E pollLast() {
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
        @Override public final E pollLast(final long timeout, final TimeUnit unit)
                throws InterruptedException {
            long nanos = unit.toNanos(timeout);
            final Lock lock = lock_;
            lock.lockInterruptibly();
            try {
                E e;
                while ((e = unlinkLast()) == null) {
                    if (nanos <= 0) return null;
                    nanos = notEmpty_.awaitNanos(nanos);
                }
                return e;
            } finally {
                lock.unlock();
            }
        }

        // (non-Javadoc)
        // @see com.tmax.probus.dq.collection.IDqStack#pop()
        @Override public final E pop() {
            return removeFirst();
        }

        // (non-Javadoc)
        // @see com.tmax.probus.dq.collection.IDqStack#push(java.lang.Object)
        @Override public final void push(final E e) {
            addFirst(e);
        }

        // (non-Javadoc)
        // @see java.util.concurrent.BlockingDeque#put(java.lang.Object)
        @Override public final void put(final E e) throws InterruptedException {
            putLast(e);
        }

        // (non-Javadoc)
        // @see com.tmax.probus.dq.collection.IDqMap#put(java.lang.Object, java.lang.Object)
        @Override public final void put(final K key, final E value) {
            offerLast(value);
        }

        // (non-Javadoc)
        // @see java.util.concurrent.BlockingDeque#putFirst(java.lang.Object)
        @Override public final void putFirst(final E e) throws InterruptedException {
            final Node<K, E> node = newNode(e);
            final Lock lock = lock_;
            lock.lock();
            try {
                while (!linkFirst(node))
                    notFull_.await();
            } finally {
                lock.unlock();
            }
        }

        // (non-Javadoc)
        // @see java.util.concurrent.BlockingDeque#putLast(java.lang.Object)
        @Override public final void putLast(final E e) throws InterruptedException {
            final Node<K, E> node = newNode(e);
            final Lock lock = lock_;
            lock.lock();
            try {
                while (!linkLast(node))
                    notFull_.await();
            } finally {
                lock.unlock();
            }
        }

        // (non-Javadoc)
        // @see com.tmax.probus.dq.collection.IDqSolidOperator#putSolidly(java.lang.Object, java.lang.Object)
        @Override public E putSolidly(final K key, final E value) {
            final Node<K, E> node = newNode(value);
            final Lock lock = lock_;
            lock.lock();
            try {
                final E exists = findSolidly(key);
                if (exists == null) {
                    linkLast(node);
                    return null;
                } else {
                    replaceSolidly(node);
                    return exists;
                }
            } finally {
                lock.unlock();
            }
        }

        // (non-Javadoc)
        // @see java.util.concurrent.BlockingQueue#remainingCapacity()
        @Override public final int remainingCapacity() {
            return maxSize_ - count_.intValue();
        }

        // (non-Javadoc)
        // @see java.util.AbstractCollection#remove(java.lang.Object)
        @Override public final boolean remove(final Object o) {
            return removeFirstOccurrence(o);
        }

        // (non-Javadoc)
        // @see java.util.Deque#removeFirst()
        @Override public final E removeFirst() {
            final E e = pollFirst();
            if (e == null) throw new NoSuchElementException();
            return e;
        }

        // (non-Javadoc)
        // @see java.util.concurrent.BlockingDeque#removeFirstOccurrence(java.lang.Object)
        @Override public final boolean removeFirstOccurrence(final Object o) {
            if (o == null) return false;
            final Lock lock = lock_;
            lock.lock();
            try {
                @SuppressWarnings("unchecked") final Node<K, E> node = repo_.get(((E) o).getId());
                if (node == null || !node.isReal()) return false;
                unlinkSolidly(node);
                return linkFirst(node) && unlinkFirst() != null;
            } finally {
                lock.unlock();
            }
        }

        // (non-Javadoc)
        // @see java.util.Deque#removeLast()
        @Override public final E removeLast() {
            final E e = pollLast();
            if (e == null) throw new NoSuchElementException();
            return e;
        }

        // (non-Javadoc)
        // @see java.util.concurrent.BlockingDeque#removeLastOccurrence(java.lang.Object)
        @Override public final boolean removeLastOccurrence(final Object o) {
            if (o == null) return false;
            final Lock lock = lock_;
            lock.lock();
            try {
                @SuppressWarnings("unchecked") final Node<K, E> node = repo_.get(((E) o).getId());
                if (node == null || !node.isReal()) return false;
                unlinkSolidly(node);
                return linkLast(node) && unlinkLast() != null;
            } finally {
                lock.unlock();
            }
        }

        // (non-Javadoc)
        // @see com.tmax.probus.dq.collection.IDqSolidOperator#removeSolidly(java.lang.Object)
        @Override public final E removeSolidly(final K key) {
            if (key == null) throw new IllegalArgumentException();
            final Lock lock = lock_;
            lock.lock();
            try {
                final Node<K, E> node = repo_.get(key);
                if (node == null) return null;
                return unlinkSolidly(node);
            } finally {
                lock.unlock();
            }
        }

        // (non-Javadoc)
        // @see java.util.AbstractCollection#size()
        @Override public final int size() {
            return count_.intValue();
        }

        // (non-Javadoc)
        // @see java.util.concurrent.BlockingDeque#take()
        @Override public final E take() throws InterruptedException {
            return takeFirst();
        }

        // (non-Javadoc)
        // @see java.util.concurrent.BlockingDeque#takeFirst()
        @Override public final E takeFirst() throws InterruptedException {
            final Lock lock = lock_;
            lock.lock();
            try {
                E e;
                while ((e = unlinkFirst()) == null)
                    notEmpty_.await();
                return e;
            } finally {
                lock.unlock();
            }
        }

        // (non-Javadoc)
        // @see java.util.concurrent.BlockingDeque#takeLast()
        @Override public final E takeLast() throws InterruptedException {
            final Lock lock = lock_;
            lock.lock();
            try {
                E e;
                while ((e = unlinkLast()) == null)
                    notEmpty_.await();
                return e;
            } finally {
                lock.unlock();
            }
        }

        // (non-Javadoc)
        // @see java.util.AbstractCollection#toArray()
        @Override public final Object[] toArray() {
            final Lock lock = lock_;
            lock.lock();
            try {
                final Object[] a = new Object[count_.intValue()];
                int k = 0;
                for (Node<K, E> n = head_.next; n.isReal(); n = n.next)
                    a[k++] = n.element;
                return a;
            } finally {
                lock.unlock();
            }
        }

        // (non-Javadoc)
        // @see java.util.AbstractCollection#toArray(T[])
        @SuppressWarnings("unchecked") @Override public final <T extends Object> T[] toArray(T[] a) {
            final Lock lock = lock_;
            lock.lock();
            try {
                if (a.length < count_.intValue()) a = (T[]) Array.newInstance(a.getClass().getComponentType(), count_.intValue());
                int k = 0;
                for (Node<K, E> n = head_.next; n.isReal(); n = n.next)
                    a[k++] = (T) n.element;
                if (a.length > k) a[k] = null;
                return a;
            } finally {
                lock.unlock();
            }
        }

        // (non-Javadoc)
        // @see java.util.AbstractCollection#toString()
        @Override public final String toString() {
            final Lock lock = lock_;
            lock.lock();
            try {
                Node<K, E> first = head_.next;
                if (!first.isReal()) return "[]";
                final StringBuilder sb = new StringBuilder("[");
                for (;;) {
                    sb.append(first.element);
                    first = first.next;
                    if (!first.isReal()) return sb.append("]").toString();
                    sb.append(',').append(' ');
                }
            } finally {
                lock.unlock();
            }
        }

        /**
         * help GC.
         * @param node the exist node
         */
        private final void gc(final Node<K, E> node) {
            node.prev = node;
            node.next = node;
        }

        /**
         * Checks if is full.
         * @return true, if is full
         */
        private final boolean isFull() {
            return count_.intValue() >= maxSize_ || fullCount_.intValue() >= Integer.MAX_VALUE;
        }

        /**
         * BASE UNIT JOB
         * @param node the node
         * @return true, if successful
         */
        private final boolean linkFirst(final Node<K, E> node) {
            if (isFull()) return false;
            if (node == NULL_NODE) throw new IllegalArgumentException("NULL NODE");
            final Node<K, E> existNode = repo_.putIfAbsent(node.getId(), node);
            if (existNode != null) return false;
            final Node<K, E> first = head_.next;
            node.next = first;
            node.prev = first.prev;
            first.prev.next = node;
            first.prev = node;
            head_.next = node;
            node.setReal(true);
            count_.incrementAndGet();
            fullCount_.incrementAndGet();
            listener_.processItemAdded(node.element);
            notEmpty_.signal();
            return true;
        }

        /**
         * BASE UNIT JOB
         * @param node the node
         * @return true, if successful
         */
        private final boolean linkLast(final Node<K, E> node) {
            if (isFull()) return false;
            if (node == NULL_NODE) throw new IllegalArgumentException("NULL NODE");
            final Node<K, E> existNode = repo_.putIfAbsent(node.getId(), node);
            if (existNode != null) return false;
            final Node<K, E> last = tail_.prev;
            node.prev = last;
            node.next = last.next;
            last.next.prev = node;
            last.next = node;
            tail_.prev = node;
            node.setReal(true);
            count_.incrementAndGet();
            fullCount_.incrementAndGet();
            listener_.processItemAdded(node.element);
            notEmpty_.signal();
            return true;
        }

        /**
         * New node.
         * @param e the e
         * @return the node
         */
        private Node<K, E> newNode(final E e) {
            if (e == null) throw new NullPointerException(); // cannot make null node
            final Node<K, E> node = new Node<K, E>(e);
            return node;
        }

        /**
         * Read object. 일단 LinkedBlockingDeque에 있어서 옮겨 놓긴 했지만 고려되지 않았음
         * @param is the is
         * @throws IOException Signals that an I/O exception has occurred.
         * @throws ClassNotFoundException the class not found exception
         */
        private void readObject(final ObjectInputStream is) throws IOException,
                ClassNotFoundException {
            is.defaultReadObject();
            count_.set(0);
            for (;;) {
                @SuppressWarnings("unchecked") final E e = (E) is.readObject();
                if (e == null) break;
                add(e);
            }
        }

        /**
         * Replace real.
         * @param node the node
         * @return the node
         */
        private final Node<K, E> replaceSolidly(final Node<K, E> node) {
            if (node == NULL_NODE) throw new IllegalArgumentException("NULL NODE");
            final Node<K, E> existNode = repo_.get(node.getId());
            if (existNode == null || node == existNode) return null;
            node.prev = existNode.prev;
            node.next = existNode.next;
            node.prev.next = node;
            node.next.prev = node;
            gc(existNode);
            return existNode;
        }

        /**
         * BASE UNIT JOB
         * @return the e
         */
        private final E unlinkFirst() {
            if (count_.intValue() == 0) return null;
            final Node<K, E> h = head_.next;
            head_.next = h.next;
            h.notReal();
            count_.decrementAndGet();
            listener_.processItemRemoved(h.element);
            notFull_.signal();
            return h.element;
        }

        /**
         * BASE UNIT JOB
         * @return the e
         */
        private final E unlinkLast() {
            if (count_.intValue() == 0) return null;
            final Node<K, E> p = tail_.prev;
            tail_.prev = p.prev;
            p.notReal();
            count_.decrementAndGet();
            listener_.processItemRemoved(p.element);
            notFull_.signal();
            return p.element;
        }

        /**
         * BASE UNIT JOB
         * @param node the node
         */
        private final E unlinkSolidly(final Node<K, E> node) {
            if (node == NULL_NODE) throw new IllegalArgumentException("NULL NODE");
            final Node<K, E> existNode = repo_.remove(node.getId());
            if (existNode == null) throw new NoSuchElementException("EXISTS");
            final Node<K, E> p = node.prev;
            final Node<K, E> n = node.next;
            p.next = n;
            n.prev = p;
            fullCount_.decrementAndGet();
            if (node.isReal()) count_.decrementAndGet();
            gc(node);
            node.notReal();
            notFull_.signal();
            return node.element;
        }

        /**
         * Write object. 일단 LinkedBlockingDeque에 있어서 옮겨 놓긴 했지만 고려되지 않았음
         * @param os the os
         * @throws IOException Signals that an I/O exception has occurred.
         */
        private void writeObject(final ObjectOutputStream os) throws IOException {
            final Lock lock = lock_;
            lock.lock();
            try {
                os.defaultWriteObject();
                for (Node<K, E> n = head_.next; n.isReal(); n = n.next)
                    os.writeObject(n.element);
                os.writeObject(null);
            } finally {
                lock.unlock();
            }
        }

        private abstract class AbstractItr implements Iterator<E> {
            private Node<K, E> next;
            private E nextItem;
            private Node<K, E> lastRet;

            private AbstractItr() {
                final Lock lock = lock_;
                lock.lock();
                try {
                    next = firstNode();
                    nextItem = isValid(next) ? next.element : null;
                } finally {
                    lock.unlock();
                }
            }

            // (non-Javadoc)
            // @see java.util.Iterator#hasNext()
            @Override public boolean hasNext() {
                return isValid(next);
            }

            // (non-Javadoc)
            // @see java.util.Iterator#next()
            @Override public E next() {
                if (!isValid(next)) throw new NoSuchElementException();
                lastRet = next;
                final E e = nextItem;
                advance();
                return e;
            }

            // (non-Javadoc)
            // @see java.util.Iterator#remove()
            @Override public void remove() {
                final Node<K, E> n = lastRet;
                if (isValid(n)) throw new IllegalStateException();
                lastRet = null;
                if (n.element != null) removeLastOccurrence(n);
            }

            abstract Node<K, E> firstNode();

            abstract Node<K, E> nextNode(Node<K, E> n);

            private void advance() {
                final Lock lock = lock_;
                lock.lock();
                try {
                    next = succ(next);
                    nextItem = isValid(next) ? next.element : null;
                } finally {
                    lock.unlock();
                }
            }

            private final boolean isValid(final Node<K, E> node) {
                return node != null && node.isReal();
            }

            private final Node<K, E> succ(Node<K, E> n) {
                for (;;) {
                    final Node<K, E> s = nextNode(n);
                    if (!isValid(s)) return null;
                    else if (s.element != null) return s;
                    else if (s == n) return firstNode();
                    else n = s;
                }
            }
        }

        private final class DescendingItr extends AbstractItr {
            // (non-Javadoc)
            // @see com.tmax.probus.dq.collection.DqQueueFactory.DqQueue.AbstractItr#firstNode()
            @Override Node<K, E> firstNode() {
                return tail_.prev;
            }

            // (non-Javadoc)
            // @see com.tmax.probus.dq.collection.DqQueueFactory.DqQueue.AbstractItr#nextNode(com.tmax.probus.dq.collection.DqQueueFactory.DqQueue.Node)
            @Override Node<K, E> nextNode(final Node<K, E> n) {
                return n.prev;
            }
        }

        private final class Itr extends AbstractItr {
            // (non-Javadoc)
            // @see com.tmax.probus.dq.collection.DqQueueFactory.DqQueue.AbstractItr#firstNode()
            @Override Node<K, E> firstNode() {
                return head_.next;
            }

            // (non-Javadoc)
            // @see com.tmax.probus.dq.collection.DqQueueFactory.DqQueue.AbstractItr#nextNode(com.tmax.probus.dq.collection.DqQueueFactory.DqQueue.Node)
            @Override Node<K, E> nextNode(final Node<K, E> n) {
                return n.next;
            }
        }

        private class JustDoNotEventListener implements IDqItemEventListener<E> {
            // (non-Javadoc)
            // @see com.tmax.probus.dq.collection.IDqCollectionListener#processItemAdded(java.lang.Object)
            @Override public void processItemAdded(final E e) {
            }

            // (non-Javadoc)
            // @see com.tmax.probus.dq.collection.IDqCollectionListener#processItemRemoved(java.lang.Object)
            @Override public void processItemRemoved(final E e) {
            }
        }

        private static final class Node<K, E extends IDqElement<K>> implements Serializable {
            /** The Constant serialVersionUID. */
            private static final long serialVersionUID = -3008752467874171657L;
            private Node<K, E> prev;
            private Node<K, E> next;
            private final E element;
            /** 현재 노드가 head와 tail사이에 존재하는지의 여부이다. */
            private boolean isReal_ = true;
            private transient long timestamp = -1L;

            private Node(final E obj) {
                element = obj;
            }

            // (non-Javadoc)
            // @see java.lang.Object#equals(java.lang.Object)
            @Override public final boolean equals(final Object obj) {
                if (obj == null || element == null) return false;
                if (obj instanceof Node) return element.equals(((Node<?, ?>) obj).element);
                return super.equals(obj);
            }

            // (non-Javadoc)
            // @see java.lang.Object#hashCode()
            @Override public final int hashCode() {
                if (element == null) return 0;
                return element.hashCode();
            }

            /**
             * Gets the elapsed time from timestamp till now.<br/>
             * timestamp가 음수인 경우는 head-tail구간에서 제외되지 않았다고 본다.
             * @return the elapsed time
             */
            private final long getElapsedTime() {
                if (timestamp < 0) return 0;
                return System.nanoTime() - NANO_BASE - timestamp;
            }

            private final K getId() {
                if (element != null) return element.getId();
                return null;
            }

            private final boolean isReal() {
                return this.isReal_;
            }

            /**
             * head와 tail 사이에서 제외되었음을 설정한다.<br/>
             * 논리적 삭제가 되는 순간에 timestamp를 설정하여 timeout 등의 용도로 사용한다.
             */
            private final void notReal() {
                isReal_ = false;
                stampTime();
            }

            private final void setReal(final boolean isReal) {
                this.isReal_ = isReal;
            }

            /**
             * Stamp time.
             * @return the long
             */
            private final long stampTime() {
                timestamp = System.nanoTime() - NANO_BASE;
                return timestamp;
            }
        }
    }
}
