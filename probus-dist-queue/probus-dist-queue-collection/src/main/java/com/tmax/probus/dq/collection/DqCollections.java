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


import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.AbstractQueue;
import java.util.Collection;
import java.util.Iterator;
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
 * A factory for creating DqQueue objects.
 */
public class DqCollections {
    public static <K, E extends IDqElement<K>> IDqSolidOperator<K, E> convert2SolidOperator(Collection<E> collection) {
        return (IDqSolidOperator<K, E>) collection;
    }

    /**
     * New blocking deque.
     * @param <E> the element type
     * @return the blocking deque
     */
    public static <E extends IDqElement<String>> BlockingDeque<E> newBlockingDeque(String id) {
        return new DqCollection<String, E>(id);
    }

    /**
     * New blocking queue.
     * @param <E> the element type
     * @return the blocking queue
     */
    public static <E extends IDqElement<String>> BlockingQueue<E> newBlockingQueue(String id) {
        return new DqCollection<String, E>(id);
    }

    /**
     * New dq map.
     * @param <K> the key type
     * @param <E> the element type
     * @return the i dq map
     */
    public static <K, E extends IDqElement<K>> IDqMap<K, E> newDqMap(String id) {
        return new DqCollection<K, E>(id);
    }

    /**
     * New dq stack.
     * @param <E> the element type
     * @return the i dq stack
     */
    public static <E extends IDqElement<String>> IDqStack<E> newDqStack(String id) {
        return new DqCollection<String, E>(id);
    }

    /**
     * New blocking deque.
     * @param <E> the element type
     * @return the blocking deque
     */
    public static <E extends IDqElement<String>> BlockingDeque<E> newBlockingDeque(String id, int maxSize, int maxCapacity) {
        return new DqCollection<String, E>(id, maxSize, maxCapacity);
    }

    /**
     * New blocking queue.
     * @param <E> the element type
     * @return the blocking queue
     */
    public static <E extends IDqElement<String>> BlockingQueue<E> newBlockingQueue(String id, int maxSize, int maxCapacity) {
        return new DqCollection<String, E>(id, maxSize, maxCapacity);
    }

    /**
     * New dq map.
     * @param <K> the key type
     * @param <E> the element type
     * @return the i dq map
     */
    public static <K, E extends IDqElement<K>> IDqMap<K, E> newDqMap(String id, int maxSize, int maxCapacity) {
        return new DqCollection<K, E>(id, maxSize, maxCapacity);
    }

    /**
     * New dq stack.
     * @param <E> the element type
     * @return the i dq stack
     */
    public static <E extends IDqElement<String>> IDqStack<E> newDqStack(String id, int maxSize, int maxCapacity) {
        return new DqCollection<String, E>(id, maxSize, maxCapacity);
    }

    /**
     * Distributed SQM 구현을 위한 Queue 컬렉션이다. LinkedBlockingDeque를 참고하여 만들었으므로 참고하기
     * 바란다.
     * <p/>
     * 다른 점은 LinkedBlockingDeque로부터 재정의한 메소드들은 데이터의 추가는 하지만 삭제는 하지 않고 삭제하는 척만
     * 한다.</br> 데이터의 삭제는 내부에 정의한 Map을 통하여 일어난다. 이렇게 한 이유는 큐에 들어온 데이터를 원격지로 전송하고
     * 응답을 받은 후에 삭제해야 하기 때문이다. <br/>
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
     * @see java.util.concurrent.LinkedBlockingQueue,
     *      java.util.concurrent.LinkedBlockingDeque
     */
    private static final class DqCollection<K, E extends IDqElement<K>> extends AbstractQueue<E>
            implements IDqSolidOperator<K, E>, IDqMap<K, E>, IDqStack<E>, BlockingDeque<E>,
            BlockingQueue<E>, Serializable {
        /** The id_. */
        final String id_;
        /** The Constant serialVersionUID. */
        private static final long serialVersionUID = 771471529351045470L;
        /** Logger for this class. */
        private final transient Logger logger = Logger.getLogger("com.tmax.probus.dq.collection");
        /** The repo_. */
        private final ConcurrentMap<K, Node<K, E>> repo_;
        /** 큐의 head이다. */
        private final Node<K, E> head_;
        /** The tail_. */
        private final Node<K, E> tail_;
        /** The capacity. */
        private int maxSize_;
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
         * @param id the id
         */
        private DqCollection(final String id) {
            this(id, Integer.MAX_VALUE, 128);
        }

        /**
         * Instantiates a new dq backup queue.
         * @param id the id
         * @param maxValue the max value
         * @param mapCapacity the map capacity
         */
        private DqCollection(final String id, final int maxSize, final int mapCapacity) {
            if (id == null || maxSize <= 0 || mapCapacity <= 0) throw new IllegalArgumentException();
            id_ = id;
            repo_ = new ConcurrentHashMap<K, Node<K, E>>(mapCapacity);
            maxSize_ = maxSize;
            head_ = tail_ = NULL_NODE;
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
                for (final E e : collection) {
                    if (linkLast(new Node<K, E>(e))) modified = true;
                }
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
                count.set(0);
                notFull.signalAll();
            } finally {
                lock.unlock();
            }
        }

        // (non-Javadoc)
        // @see java.util.AbstractCollection#contains(java.lang.Object)
        @Override public final boolean contains(final Object o) {
            if (o == null) return false;
            final Node<K, E> node = repo_.get(((E) o).getId());
            return node != null && node.isReal;
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
        // @see com.tmax.probus.dq.collection.IDqSolidOperator#findSolidly(java.lang.Object)
        @Override public final E findSolidly(final K key) {
            final Node<K, E> node = repo_.get(key);
            if (node == null) return null;
            return node.element;
        }

        // (non-Javadoc)
        // @see com.tmax.probus.dq.collection.IDqSolidOperator#fullSize()
        @Override public final int fullSize() {
            return fullCount.intValue();
        }

        // (non-Javadoc)
        // @see com.tmax.probus.dq.collection.IDqMap#get(java.lang.Object)
        @Override public final E get(final K key) {
            final Node<K, E> node = repo_.get(key);
            if (node == null || !node.isReal) return null;
            return node.element;
        }

        // (non-Javadoc)
        // @see java.util.Deque#getFirst()
        @Override public final E getFirst() {
            final E e = peekFirst();
            if (e == null) throw new NoSuchElementException();
            return e;
        }

        // (non-Javadoc)
        // @see java.util.Deque#getLast()
        @Override public final E getLast() {
            final E e = peekLast();
            if (e == null) throw new NoSuchElementException();
            return e;
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
        @Override public final boolean offerFirst(final E e, final long timeout, final TimeUnit unit)
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
        // @see java.util.concurrent.BlockingDeque#offerLast(java.lang.Object)
        @Override public final boolean offerLast(final E e) {
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
        @Override public final boolean offerLast(final E e, final long timeout, final TimeUnit unit)
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
                    nanos = notEmpty.awaitNanos(nanos);
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
                    nanos = notEmpty.awaitNanos(nanos);
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
        @Override public final void putLast(final E e) throws InterruptedException {
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
        // @see com.tmax.probus.dq.collection.IDqSolidOperator#putSolidly(java.lang.Object, java.lang.Object)
        @Override public E putSolidly(final K key, final E value) {
            if (value == null) throw new NullPointerException();
            final Lock lock = lock_;
            lock.lock();
            try {
                final Node<K, E> node = new Node<K, E>(value);
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
            return maxSize_ - count.intValue();
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
                final Node<K, E> node = repo_.get(((E) o).getId());
                if (node == null || !node.isReal) return false;
                unlinkSolidly(node);
                return linkLast(node) && unlinkLast() != null;
            } finally {
                lock.unlock();
            }
        }

        // (non-Javadoc)
        // @see com.tmax.probus.dq.collection.IDqSolidOperator#removeSolidly(java.lang.Object)
        @Override public final E removeSolidly(final K key) {
            final Lock lock = lock_;
            lock.lock();
            try {
                final Node<K, E> node = repo_.get(key);
                if (node == null) throw new NoSuchElementException();
                unlinkSolidly(node);
                return node.element;
            } finally {
                lock.unlock();
            }
        }

        // (non-Javadoc)
        // @see java.util.AbstractCollection#size()
        @Override public final int size() {
            return count.intValue();
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
                    notEmpty.await();
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
                    notEmpty.await();
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
                final Object[] a = new Object[count.intValue()];
                int k = 0;
                for (Node<K, E> n = head_.next; n.isReal; n = n.next)
                    a[k++] = n.element;
                return a;
            } finally {
                lock.unlock();
            }
        }

        // (non-Javadoc)
        // @see java.util.AbstractCollection#toArray(T[])
        @Override public final <T extends Object> T[] toArray(T[] a) {
            final Lock lock = lock_;
            lock.lock();
            try {
                if (a.length < count.intValue()) a = (T[]) Array.newInstance(a.getClass().getComponentType(), count.intValue());
                int k = 0;
                for (Node<K, E> n = head_.next; n.isReal; n = n.next)
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
                if (!first.isReal) return "[]";
                final StringBuilder sb = new StringBuilder("[");
                while (true) {
                    sb.append(first.element);
                    first = first.next;
                    if (!first.isReal) return sb.append("]").toString();
                    sb.append(',').append(' ');
                }
            } finally {
                lock.unlock();
            }
        }

        /**
         * Gets the id.
         * @return the id
         */
        public final String getId() {
            return id_;
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
            return count.intValue() >= maxSize_ || fullCount.intValue() >= Integer.MAX_VALUE;
        }

        /**
         * Link first.
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
            node.isReal = true;
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
            node.isReal = true;
            count.incrementAndGet();
            fullCount.incrementAndGet();
            notEmpty.signal();
            return true;
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
            count.set(0);
            for (;;) {
                final E e = (E) is.readObject();
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
         * Unlink first.
         * @return the e
         */
        private final E unlinkFirst() {
            if (count.intValue() == 0) return null;
            final Node<K, E> h = head_.next;
            head_.next = h.next;
            h.isReal = false;
            count.decrementAndGet();
            notFull.signal();
            return h.element;
        }

        /**
         * Unlink last.
         * @return the e
         */
        private final E unlinkLast() {
            if (count.intValue() == 0) return null;
            final Node<K, E> p = tail_.prev;
            tail_.prev = p.prev;
            p.isReal = false;
            count.decrementAndGet();
            notFull.signal();
            return p.element;
        }

        /**
         * Unlink solidly.
         * @param node the node
         */
        private final void unlinkSolidly(final Node<K, E> node) {
            if (node == NULL_NODE) throw new IllegalArgumentException("NULL NODE");
            final Node<K, E> existNode = repo_.get(node.getId());
            if (existNode == null) throw new NoSuchElementException("EXISTS");
            final Node<K, E> p = node.prev;
            final Node<K, E> n = node.next;
            p.next = n;
            n.prev = p;
            fullCount.decrementAndGet();
            if (node.isReal) count.decrementAndGet();
            gc(node);
            node.isReal = false;
            notFull.signal();
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
                for (Node<K, E> n = head_.next; n.isReal; n = n.next) {
                    os.writeObject(n.element);
                }
                os.writeObject(null);
            } finally {
                lock.unlock();
            }
        }

        /**
         * The Class AbstractItr.
         */
        private abstract class AbstractItr implements Iterator<E> {
            /** The next. */
            Node<K, E> next;
            /** The next item. */
            E nextItem;
            /** The last ret. */
            Node<K, E> lastRet;

            /**
             * Instantiates a new abstract itr.
             */
            public AbstractItr() {
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

            /**
             * First node.
             * @return the node
             */
            abstract Node<K, E> firstNode();

            /**
             * Next node.
             * @param n the n
             * @return the node
             */
            abstract Node<K, E> nextNode(Node<K, E> n);

            /**
             * Advance.
             */
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

            /**
             * Checks if is valid.
             * @param node the node
             * @return true, if is valid
             */
            private final boolean isValid(final Node<K, E> node) {
                return node != null && node.isReal;
            }

            /**
             * Succ.
             * @param n the n
             * @return the node
             */
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

        /**
         * The Class DescendingItr.
         */
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

        /**
         * The Class Itr.
         */
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

        /**
         * The Class Node.
         * @param <K> the key type
         * @param <E> the element type
         */
        private static final class Node<K, E extends IDqElement<K>> {
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
             * Gets the id.
             * @return the id
             */
            final K getId() {
                if (element != null) return element.getId();
                return null;
            }
        }
    }
}
