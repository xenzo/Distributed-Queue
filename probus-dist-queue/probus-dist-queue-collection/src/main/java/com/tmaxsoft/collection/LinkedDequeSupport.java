package com.tmaxsoft.collection;


import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicMarkableReference;


/**
 * The Class LinkedDequeSupport.
 * @param <V> the value type
 * @author xenzo
 */
class LinkedDequeSupport<V> {
        /** The nano base. */
        final transient long NANO_BASE = System.nanoTime();
        /** The null node. */
        final transient Node<V> NULL_NODE = new Node<V>(null);
        /** The head. */
        private final transient Node<V> _head = NULL_NODE;
        /** The tail. */
        private final transient Node<V> _tail = NULL_NODE;
        /** The real count. */
        private final transient AtomicLong _realCount = new AtomicLong(0L);
        /** repo에 존재하는 전체 노드 갯수. */
        private final transient AtomicLong _totalCount = new AtomicLong(0L);
        /** priority queue로 사용할 경우 */
        private final Comparator<V> _comparator;
        {
                NULL_NODE._setNext(NULL_NODE);
                NULL_NODE._setPrev(NULL_NODE);
        }

        LinkedDequeSupport() {
                _comparator = null;
        }

        LinkedDequeSupport(Comparator<V> comparator) {
                _comparator = comparator;
        }

        /**
         * Asc iterator.
         * @return the iterator
         */
        public Iterator<V> ascIterator() {
                return new AscIter();
        }

        /**
         * Desc iterator.
         * @return the iterator
         */
        public Iterator<V> descIterator() {
                return new DescIter();
        }

        /**
         * Real count.
         * @return the long
         */
        public final long realCount() {
                return _realCount.get();
        }

        /**
         * Count solidly.
         * @return the long
         */
        public final long totalCount() {
                return _totalCount.get();
        }

        /**
         * Tail.
         * @return the node
         */
        final Node<V> tail() {
                return _tail.prev();
        }

        /**
         * Head.
         * @return the node
         */
        final Node<V> head() {
                return _head.next();
        }

        /**
         * Link first.
         * @param node the node
         * @return true, if successful
         */
        final boolean linkFirst(final Node<V> node) {
                if (node == null) throw new NullPointerException();
                if (node == NULL_NODE) throw new IllegalArgumentException("NULL NODE");
                if (_comparator != null) return linkByPriority(node);
                return head().prepend(node);
        }

        /**
         * Link last.
         * @param node the node
         * @return true, if successful
         */
        final boolean linkLast(final Node<V> node) {
                if (node == null) throw new NullPointerException();
                if (node == NULL_NODE) throw new IllegalArgumentException("NULL NODE");
                if (_comparator != null) return linkByPriority(node);
                return tail().append(node);
        }

        final boolean linkByPriority(final Node<V> node) {
                if (node == null) throw new NullPointerException();
                if (node == NULL_NODE) throw new IllegalArgumentException("NULL NODE");
                Node<V> pos = _findPriorityPosition(node);
                return pos.append(node);
        }

        private final Node<V> _findPriorityPosition(final Node<V> node) {
                final Comparable<V> c = _comparable(node);
                Node<V> p;
                Node<V> n;
                do {
                        p = head();
                        n = head();
                        while (!p.isDeleted() && !n.isBase() && c.compareTo(n.element()) > 0) {
                                p = n;
                                do {
                                        n = n._getNext();
                                } while (n.isDeleted());
                        }
                } while (p.isDeleted());
                return p;
        }

        @SuppressWarnings("unchecked") private final Comparable<V> _comparable(final Node<V> node)
                        throws ClassCastException {
                if (node == null) throw new NullPointerException();
                if (_comparator != null) return new ComparableUsingComparator<V>(node.element(), _comparator);
                else return (Comparable<V>) node.element();
        }

        /**
         * Unlink first.
         * @return the v
         */
        final V unlinkFirst() {
                final Node<V> b = head();
                while (true) {
                        if (b.isBase()) return null;
                        if (b.delete()) return b.element();
                }
        }

        /**
         * Unlink last.
         * @return the v
         */
        final V unlinkLast() {
                final Node<V> b = tail();
                while (true) {
                        if (b.isBase()) return null;
                        if (b.delete()) return b.element();
                }
        }

        /**
         * New node.
         * @param e the e
         * @return the node
         */
        final Node<V> newNode(final V e) {
                if (e == null) throw new NullPointerException(); // cannot make null node
                final Node<V> node = new Node<V>(e);
                return node;
        }

        /**
         * Represents a key with a comparator as a Comparable. Because most
         * sorted collections seem to use natural ordering on Comparables
         * (Strings, Integers, etc), most internal methods are geared to use
         * them. This is generally faster than checking per-comparison whether
         * to use comparator or comparable because it doesn't require a
         * (Comparable) cast for each comparison. (Optimizers can only sometimes
         * remove such redundant checks themselves.) When Comparators are used,
         * ComparableUsingComparators are created so that they act in the same
         * way as natural orderings. This penalizes use of Comparators vs
         * Comparables, which seems like the right tradeoff.
         * @param <I> the generic type
         */
        static final class ComparableUsingComparator<I> implements Comparable<I> {
                /** The actual key. */
                private final I _actualKey;
                /** The cmp. */
                private final Comparator<I> _cmp;

                /**
                 * Instantiates a new comparable using comparator.
                 * @param key the key
                 * @param cmp the cmp
                 */
                ComparableUsingComparator(final I key, final Comparator<I> cmp) {
                        this._actualKey = key;
                        this._cmp = cmp;
                }

                /** {@inheritDoc} */
                @Override public int compareTo(final I k2) {
                        return _cmp.compare(_actualKey, k2);
                }
        }

        /**
         * The Class Node. <br />
         * reference is as next node. <br />
         * mark is as deletion mark.
         * @param <E> the element type
         */
        final class Node<E> extends AtomicMarkableReference<Node<E>> {
                /** The prev. */
                private volatile Node<E> _prev;
                /** The element. */
                private final E _element;
                /** whether real node. */
                private final AtomicBoolean _isReal;
                /** The poll time. */
                private transient long _pollTime = -1L;
                /** The offer time. */
                private transient long _offerTime = -1L;

                /**
                 * Instantiates a new node.
                 * @param data the data
                 */
                private Node(final E data) {
                        this(data, null, null);
                }

                /**
                 * Instantiates a new node.
                 * @param data the data
                 * @param prev the prev
                 * @param next the next
                 */
                private Node(final E data, final Node<E> prev, final Node<E> next) {
                        this(data, prev, next, true);
                }

                /**
                 * Instantiates a new node.
                 * @param data the data
                 * @param prev the prev
                 * @param next the next
                 * @param isReal the is real
                 */
                private Node(final E data, final Node<E> prev, final Node<E> next, final boolean isReal) {
                        super(next, false);
                        _element = data;
                        _prev = prev;
                        _isReal = new AtomicBoolean(isReal);
                }

                /** {@inheritDoc} */
                @SuppressWarnings("rawtypes") @Override public final boolean equals(final Object obj) {
                        if (obj == null || _element == null) return false;
                        if (obj instanceof Node) return element().equals(((Node) obj).element());
                        return super.equals(obj);
                }

                /** {@inheritDoc} */
                @Override public final int hashCode() {
                        if (element() == null) return 0;
                        return element().hashCode();
                }

                /**
                 * cas next.
                 * @param expect the expect
                 * @param node the node
                 * @return true, if successful
                 */
                private final boolean _casNext(final Node<E> expect, final Node<E> node) {
                        return compareAndSet(expect, node, false, false);
                }

                /**
                 * fix next prev.
                 */
                private final void _fixNextPrev() {
                        if (isDeleted()) return;
                        final Node<E> nextNode = _getNext();
                        final Node<E> nextPrevNode = nextNode._getPrev();
                        if (nextPrevNode == this) return;
                        nextNode._setPrev(this);
                }

                /**
                 * get next.
                 * @return the node
                 */
                private final Node<E> _getNext() {
                        return getReference();
                }

                /**
                 * get prev.
                 * @return the node
                 */
                private final Node<E> _getPrev() {
                        return _prev;
                }

                /**
                 * set next.
                 * @param node the node
                 */
                private final void _setNext(final Node<E> node) {
                        set(node, isMarked());
                }

                /**
                 * set prev.
                 * @param node the node
                 */
                private final void _setPrev(final Node<E> node) {
                        _prev = node;
                }

                /**
                 * stamp poll time.
                 */
                private final void _stampPollTime() {
                        if (isBase()) return;
                        _pollTime = System.currentTimeMillis() - NANO_BASE;
                }

                /**
                 * stamp offer time.
                 */
                private final void _stampOfferTime() {
                        if (isBase()) return;
                        _offerTime = System.currentTimeMillis() - NANO_BASE;
                }

                /**
                 * Gets the elapsed time from timestamp till now.<br/>
                 * timestamp가 음수인 경우는 head-tail구간에서 제외되지 않았다고 본다.
                 * @return the elapsed time
                 */
                private final long _getActivedTime() {
                        if (_offerTime < 0) return 0;
                        return System.nanoTime() - NANO_BASE - _offerTime;
                }

                /**
                 * get expired time.
                 * @return the long
                 */
                private final long _getExpiredTime() {
                        if (_pollTime < 0) return 0;
                        return System.nanoTime() - NANO_BASE - _pollTime;
                }

                /**
                 * Checks if is active timed out.
                 * @param timeout the timeout
                 * @param unit the unit
                 * @return true, if is active timed out
                 */
                final boolean isActiveTimedOut(final long timeout, final TimeUnit unit) {
                        return _getActivedTime() > unit.toNanos(timeout);
                }

                /**
                 * Checks if is expire timed out.
                 * @param timeout the timeout
                 * @param unit the unit
                 * @return true, if is expire timed out
                 */
                final boolean isExpireTimedOut(final long timeout, final TimeUnit unit) {
                        return _getExpiredTime() > unit.toNanos(timeout);
                }

                /**
                 * Checks if is real.
                 * @return true, if is real
                 */
                final boolean isReal() {
                        return _isReal.get();
                }

                final boolean isNotReal() {
                        return !isReal();
                }

                /**
                 * Checks if is deleted.
                 * @return true, if is deleted
                 */
                final boolean isDeleted() {
                        return isMarked();
                }

                /**
                 * Checks if is base.
                 * @return true, if is base
                 */
                final boolean isBase() {
                        return this == NULL_NODE;
                }

                /**
                 * Element.
                 * @return the e
                 */
                final E element() {
                        return _element;
                }

                /**
                 * return the next valid node.
                 * @return the node
                 */
                final Node<E> next() {
                        Node<E> node = this;
                        while (true) {
                                final Node<E> nextNode = node._getNext();
                                if (nextNode.isReal()) {
                                        node._fixNextPrev();
                                        return nextNode;
                                } else if (nextNode.isDeleted()) {
                                        final Node<E> nextNextNode = nextNode._getNext();
                                        if (nextNode.isDeleted() && node._casNext(nextNode, nextNextNode)) {
                                                //if (!node.isDeleted()) nextNode._dangle();//help GC
                                        } else node = this;
                                } else node = nextNode;
                        }
                }

                /**
                 * dangle.
                 */
//                private void _dangle() {
//                        _prev = this;
//                        set(this, true);
//                }
                /**
                 * return the previous valid node.
                 * @return the node
                 */
                final Node<E> prev() {
                        Node<E> node = this;
                        while (true) {
                                final Node<E> prevNode = node._getPrev();
                                if (prevNode.isReal()) {
                                        prevNode.next();//for fix
                                        return prevNode;
                                } else node = prevNode;
                        }
                }

                /**
                 * append new node.
                 * @param newNode the new node
                 * @return true, if successful
                 */
                final boolean append(final Node<E> newNode) {
                        final Node<E> prevNode = this;
                        final Node<E> nextNode = _getNext();
                        return _insertNode(newNode, prevNode, nextNode);
                }

                /**
                 * Tries to insert a node holding element as predecessor,
                 * failing if no live predecessor can be found to link to.
                 * @param newNode the element
                 * @return the new node, or null on failure
                 */
                final boolean prepend(final Node<E> newNode) {
                        final Node<E> prevNode = _getPrev();
                        final Node<E> nextNode = this;
                        return _insertNode(newNode, prevNode, nextNode);
                }

                /**
                 * insert node.
                 * @param newNode the new node
                 * @param prevNode the prev node
                 * @param nextNode the next node
                 * @return true, if successful
                 */
                private final boolean _insertNode(final Node<E> newNode, final Node<E> prevNode, final Node<E> nextNode) {
                        newNode._setPrev(prevNode);
                        newNode._setNext(nextNode);
                        if (!prevNode.isDeleted() && prevNode._casNext(nextNode, newNode)) {
                                newNode._fixNextPrev();
                                _realCount.incrementAndGet();
                                _totalCount.incrementAndGet();
                                _stampOfferTime();
                                return true;
                        }
                        return false;
                }

                /**
                 * Delete.
                 * @return true, if successful
                 */
                final boolean delete() {
                        if (_markNotReal()) {
                                _realCount.decrementAndGet();
                                _stampPollTime();
                                return true;
                        }
                        return false;
                }

                /**
                 * Awake.
                 * @return true, if successful
                 */
                final boolean awake() {
                        if (!isDeleted() && _markReal()) {
                                _realCount.incrementAndGet();
                                _stampOfferTime();
                                _pollTime = -1L;
                                return true;
                        }
                        return false;
                }

                /**
                 * Delete solidly.
                 * @return true, if successful
                 */
                final boolean deleteSolidly() {
                        Node<E> nextNode = null;
                        while (true) {
                                if (isBase() || isReal() || isDeleted()) return false;
                                nextNode = _getNext();
                                if (_markDeleted(nextNode)) break;
                        }
//            nextNode.prev();
                        _totalCount.decrementAndGet();
                        return true;
                }

                /**
                 * mark not real.
                 * @return true, if successful
                 */
                private boolean _markNotReal() {
                        return !isBase() && _isReal.compareAndSet(true, false);
                }

                /**
                 * mark real.
                 * @return true, if successful
                 */
                private boolean _markReal() {
                        return !isBase() && _isReal.compareAndSet(false, true);
                }

                /**
                 * mark deleted.
                 * @param expect the expect
                 * @return true, if successful
                 */
                private final boolean _markDeleted(final Node<E> expect) {
                        return !isBase() && compareAndSet(expect, expect, false, true);
                }
        }

        /**
         * The Class Itr.
         * @param <T> the generic type
         */
        private abstract class Itr<T> implements Iterator<T> {
                /** The _last returned. */
                Node<V> _lastReturned;
                /** The _next. */
                Node<V> _next;

                /**
                 * Advance.
                 */
                final void advance() {
                        if (_next == null || _next.isBase()) throw new NoSuchElementException();
                        _lastReturned = _next;
                        _next = forwardOp();
                }

                /**
                 * Forward op.
                 * @return the node
                 */
                protected abstract Node<V> forwardOp();

                /**
                 * Checks for next.
                 * @return true, if successful
                 */
                @Override public final boolean hasNext() {
                        return _next != null && !_next.isBase();
                }

                /**
                 * Removes the.
                 */
                @Override public final void remove() {
                        final Node<V> l = _lastReturned;
                        if (l == null || l.isDeleted() || (l.isReal() && !l.delete()) || !l.deleteSolidly())
                                throw new IllegalStateException();
                }
        }

        /**
         * The Class AscIter.
         */
        final class AscIter extends Itr<V> {
                /**
                 * Instantiates a new asc iter.
                 */
                private AscIter() {
                        _next = head().next();
                }

                /**
                 * Next.
                 * @return the v
                 */
                @Override public V next() {
                        Node<V> next;
                        do {
                                next = _next;
                                advance();
                        } while (next.isNotReal());
                        return next.element();
                }

                /**
                 * Forward op.
                 * @return the node
                 */
                @Override protected Node<V> forwardOp() {
                        return _next.next();
                }
        }

        /**
         * The Class DescIter.
         */
        final class DescIter extends Itr<V> {
                /**
                 * Instantiates a new desc iter.
                 */
                private DescIter() {
                        _next = tail().prev();
                }

                /**
                 * Next.
                 * @return the v
                 */
                @Override public V next() {
                        Node<V> next;
                        do {
                                next = _next;
                                advance();
                        } while (next.isNotReal());
                        return next.element();
                }

                /**
                 * Forward op.
                 * @return the node
                 */
                @Override protected Node<V> forwardOp() {
                        return _next.prev();
                }
        }
}
