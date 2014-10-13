/*
 * SqmDeque.java Version 1.0 Feb 22, 2014
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
package com.tmaxsoft.collection;


import static java.util.logging.Level.*;

import java.util.AbstractMap;
import java.util.Comparator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;
import java.util.logging.Logger;

import com.tmaxsoft.collection.IEventListener.EventType;


/** The Class BlockingDequeSupport.
 * @param <K> the key type
 * @param <V> the value type */
public class BlockingDequeSupport<K, V> extends TwoPhaseDeletionDequeSupport<K, Map.Entry<K, V>> {
        /** Logger for this class */
        private final transient Logger logger = Logger.getLogger("com.tmaxsoft.collection.deque");
        /** The capacity. */
        private final transient long _capacity;
        private IEventListener<K, V> _listener = new IEventListener<K, V>() {
                @Override
                public void eventOccurred(final EventType type, final K key, final V value) {
                        if (logger.isLoggable(FINEST)) logger.logp(FINEST, getClass().getName(), "eventOccurred", "it will do nothing. it is dummy listener.", new Object[] { type, key, value });
                }
        };
        private final Sync _sync = new Sync();

        public BlockingDequeSupport() {
                this(Long.MAX_VALUE);
        }

        public BlockingDequeSupport(final long capacity) {
                this(capacity, null, null);
        }

        public BlockingDequeSupport(final long capacity, final Comparator<K> keyComparator, final Comparator<V> valueComparator) {
                super(keyComparator,
                      valueComparator != null
                                      ? new Comparator<Map.Entry<K, V>>() {
                                              @Override
                                              public int compare(final Entry<K, V> entry1, final Entry<K, V> entry2) {
                                                      return valueComparator.compare(entry1.getValue(), entry2.getValue());
                                              }
                                      }
                                      : null);
                _capacity = capacity > 0 ? capacity : Long.MAX_VALUE;
        }

        public final boolean offerFirst(final K key, final V value) {
                return offerFirst(newEntry(key, value));
        }

        public final boolean offerFirst(final Map.Entry<K, V> entry) {
                if (isFull()) return false;
                final boolean ret = doOfferFirst(entry.getKey(), entry);
                if (ret) _getListener().eventOccurred(EventType.NEW, entry.getKey(), entry.getValue());
                return ret;
        }

        public final boolean offerLast(final K key, final V value) {
                return offerLast(newEntry(key, value));
        }

        public final boolean offerLast(final Map.Entry<K, V> entry) {
                if (isFull()) return false;
                final boolean ret = doOfferLast(entry.getKey(), entry);
                if (ret) _getListener().eventOccurred(EventType.NEW, entry.getKey(), entry.getValue());
                return ret;
        }

        public final V pollFirst() {
                if (isEmpty()) return null;
                final Entry<K, V> entry;
                if ((entry = doPollFirst()) != null) {
                        assert entry.getKey() != null;
                        _getListener().eventOccurred(EventType.INVALID, entry.getKey(), entry.getValue());
                        return entry.getValue();
                }
                return null;
        }

        public final V pollLast() {
                if (isEmpty()) return null;
                final Entry<K, V> entry;
                if ((entry = doPollLast()) != null) {
                        assert entry.getKey() != null;
                        _getListener().eventOccurred(EventType.INVALID, entry.getKey(), entry.getValue());
                        return entry.getValue();
                }
                return null;
        }

        public final V remove(final K key) {
                final Entry<K, V> entry;
                if ((entry = doRemoveSolidily(key)) != null) {
                        assert entry.getKey() != null;
                        assert entry.getKey().equals(key);
                        _getListener().eventOccurred(EventType.DISCARD, key, entry.getValue());
                        return entry.getValue();
                }
                return null;
        }

        public final void setListener(final IEventListener<K, V> listener) {
                if (listener == null) throw new IllegalArgumentException();
                _listener = listener;
        }

        /** Checks if is empty.
         * @return true, if is empty */
        protected final boolean isEmpty() {
                return !(realCount() > 0L);
        }

        /** Checks if is full.
         * @return true, if is full */
        protected final boolean isFull() {
                return totalCount() >= _capacity;
        }

        protected Map.Entry<K, V> newEntry(final K key, final V value) {
                return new AbstractMap.SimpleImmutableEntry<K, V>(key, value);
        }

        private final IEventListener<K, V> _getListener() {
                return _listener;
        }

        /** The Class Sync. */
        private final static class Sync extends AbstractQueuedSynchronizer {
                private static final long serialVersionUID = 4701131964595873271L;

                /** @InheritDoc */
                @Override
                protected boolean isHeldExclusively() {
                        return super.isHeldExclusively();
                }

                /** @InheritDoc */
                @Override
                protected boolean tryAcquire(final int arg) {
                        return super.tryAcquire(arg);
                }

                /** @InheritDoc */
                @Override
                protected int tryAcquireShared(final int arg) {
                        return super.tryAcquireShared(arg);
                }

                /** @InheritDoc */
                @Override
                protected boolean tryRelease(final int arg) {
                        return super.tryRelease(arg);
                }

                /** @InheritDoc */
                @Override
                protected boolean tryReleaseShared(final int arg) {
                        return super.tryReleaseShared(arg);
                }
        }
}
//end SqmDeque