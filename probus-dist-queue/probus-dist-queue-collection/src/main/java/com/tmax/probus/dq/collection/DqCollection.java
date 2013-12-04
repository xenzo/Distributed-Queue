/*
 * DqCollection.java Version 1.0 Jul 18, 2012
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
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicMarkableReference;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;


/**
 * Distributed SQM 구현을 위한 Queue 컬렉션이다. LinkedBlockingDeque를 참고하여 만들었으므로 참고하기
 * 바란다.
 * <p/>
 * 다른 점은 이 클래스에 있는 LinkedBlockingDeque에 존재하는 메소드들은 데이터의 추가는 하지만 삭제는 하지 않고 삭제하는
 * 척만 한다.</br> 데이터의 삭제는 내부에 정의한 Map을 통하여 일어난다. 이렇게 한 이유는 큐에 들어온 데이터를 원격지로 전송하고
 * 응답을 받은 후에 삭제해야 하기 때문이다.<br/>
 * 삭제 작업이 Queue처럼 head, tail에서만 이루어지는 것이 아니라 응답에 따라 무작위로 일어나기 때문에 Map을 사용했다. <br/>
 * 데이터 추가시에는 Map과 Queue에 저장되고 데이터 제거시에는 head와 tail의 참조값만 변경하여 데이터가 제거된 것처럼 보이게
 * 하고 실제로 응답이 도착하면 데이터를 제거한다.
 * <p/>
 * 또 한가지는 원형큐 형태로 정의되어 있다는 것이다.<br/>
 * head와 tail은 하나의 같은 빈 노드이고 이 값은 바뀌지 않고 head의 next값과 tail의 prev 값만 변경하여
 * position을 바꾼다.
 * <p/>
 * 그리고 주의 할 점은 큐에 데이터가 추가되고 완전히 비워지면 새로운 Node의 리스트가 생성될 수 있다는 것이다. 왜냐하면 데이터는 응답을
 * 전송 받은 후에 삭제하기 때문에 Node 리스트가 계속 존재하는데 논리적 삭제로 인해서 head와 tail이 초기화 상태에서 다시 데이터를
 * 받게 되면 새로운 Node 리스트가 생성되고 기존에 남겨진 Node들은 내부의 Map을 통해서만 접근 가능하게 된다.<br/>
 * 또 중복 데이터의 추가가 불가능하다. 응답이 온 경우 중복데이터의 삭제가 애매하여 그렇게 하였다.
 * @param <K> the key type
 * @param <E> the element type
 * @author Kim, Dong iL
 * @see java.util.concurrent.LinkedBlockingQueue
 * @see java.util.concurrent.LinkedBlockingDeque
 */
class DqCollection<K, E extends IDqElement<K>> implements IDqCollectionOperator<K, E>, Serializable {
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 771471529351045470L;
    /** The Constant NANO_BASE. */
    private static final long NANO_BASE = System.nanoTime();
    /** The seed generator. */
    /* skip list filed */
    private transient int randomSeed;
    /** Special value used to identify base-level header */
    private static final Object BASE_HEADER = new Object();
    /** The topmost head index of the skiplist. */
    private transient volatile AtomicReference<HeadIndex<K, E>> headIndex_;
    private HeadIndex<K, E> bottomHeadIndex_;
    /**
     * The comparator used to maintain order in this map, or null if using
     * natural ordering.
     */
    private final Comparator<K> comparator_;
    /* collection field */
    /** The logger. */
    private final transient Logger logger = Logger.getLogger("com.tmax.probus.dq.collection");
    /** The id. */
    private final String id_;
    /** 아이템 추가/제거시 호출되는 리스너. */
    private IDqItemEventListener<E> listener_;
    /** 큐의 head이다. */
    private final transient Node<K, E> head_;
    /** The tail. */
    private final transient Node<K, E> tail_;
    /** head와 tail 사이의 노드 갯수. */
    private final transient AtomicLong count_ = new AtomicLong(0);
    /** repo에 존재하는 전체 노드 갯수. */
    private final transient AtomicLong fullCount_ = new AtomicLong(0);
    /** The lock_. */
    private final transient Lock lock_ = new ReentrantLock();
    /** Wait queue for waiting takes. */
    private final transient Condition notEmpty_ = lock_.newCondition();
    /** Wait queue for waiting puts. */
    private final transient Condition notFull_ = lock_.newCondition();
    /** The NULL node. head/tail is this. */
    private final transient Node<K, E> NULL_NODE = new Node<>(null);
    {
        NULL_NODE.setAfter(NULL_NODE);
        NULL_NODE.setBefore(NULL_NODE);
        randomSeed = new Random(System.currentTimeMillis()).nextInt() | 0x0100;
        bottomHeadIndex_ = new HeadIndex<>(new Node<>((E) BASE_HEADER), null, null, 1);
        headIndex_ = new AtomicReference<>(bottomHeadIndex_);
    }

    /**
     * Instantiates a new dq collection.
     * @param id the id
     * @param listener the listener
     * @param comparator the comparator
     */
    DqCollection(final String id, final IDqItemEventListener<E> listener, Comparator<K> comparator) {
        if (id == null) throw new NullPointerException("IDqCollection ID cannot be null");
        id_ = id;
        listener_ = (listener == null ? new DoNothingEventListener() : listener);
        head_ = tail_ = NULL_NODE;
        comparator_ = comparator;
    }

    DqCollection(final String id) {
        this(id, null, null);
    }

    /*----------------------*/
    /* skip list operations */
    /*----------------------*/
    private final boolean casHeadIndex(HeadIndex<K, E> cmp, HeadIndex<K, E> value) {
        return headIndex_.compareAndSet(cmp, value);
    }

    private final HeadIndex<K, E> getHeadIndex() {
        return headIndex_.get();
    }

    /* ---------------- Comparison utilities -------------- */
    /**
     * Represents a key with a comparator as a Comparable. Because most sorted
     * collections seem to use natural ordering on Comparables (Strings,
     * Integers, etc), most internal methods are geared to use them. This is
     * generally faster than checking per-comparison whether to use comparator
     * or comparable because it doesn't require a (Comparable) cast for each
     * comparison. (Optimizers can only sometimes remove such redundant checks
     * themselves.) When Comparators are used, ComparableUsingComparators are
     * created so that they act in the same way as natural orderings. This
     * penalizes use of Comparators vs Comparables, which seems like the right
     * tradeoff.
     */
    private static final class ComparableUsingComparator<K> implements Comparable<K> {
        private final K actualKey;
        private final Comparator<K> cmp;

        private ComparableUsingComparator(K key, Comparator<K> cmp) {
            this.actualKey = key;
            this.cmp = cmp;
        }

        /** {@inheritDoc} */
        @Override public int compareTo(K k2) {
            return cmp.compare(actualKey, k2);
        }
    }

    /**
     * If using comparator, return a ComparableUsingComparator, else cast key as
     * Comparable, which may cause ClassCastException, which is propagated back
     * to caller.
     */
    @SuppressWarnings("unchecked") private final Comparable<K> comparable(K key)
            throws ClassCastException {
        if (key == null) throw new NullPointerException();
        if (comparator_ != null) return new ComparableUsingComparator<>(key, comparator_);
        else return (Comparable<K>) key;
    }

    /**
     * Compares using comparator or natural ordering. Used when the
     * ComparableUsingComparator approach doesn't apply.
     */
    @SuppressWarnings("unchecked") private final int compare(K k1, K k2) throws ClassCastException {
        final Comparator<K> cmp = comparator_;
        if (cmp != null) return cmp.compare(k1, k2);
        else return ((Comparable<K>) k1).compareTo(k2);
    }

    /* ---------------- Traversal -------------- */
    /**
     * Returns a base-level node with key strictly less than given key, or the
     * base-level header if there is no such node. Also unlinks indexes to
     * deleted nodes found along the way. Callers rely on this side-effect of
     * clearing indices to deleted nodes.
     * @param key the key
     * @return a predecessor of key
     */
    private final Index<K, E> findPredecessorIndex(Comparable<K> key) {
        if (key == null) throw new NullPointerException(); // don't postpone errors
        while (true) {
            Index<K, E> q = getHeadIndex();
            while (true) {
                Index<K, E> r = q.getRight();
                if (r != null) {
                    if (r.indexesDeletedNode()) {
                        if (!q.unlink(r)) break;
                        continue;
                    }
                    if (key.compareTo(r.key()) > 0) {
                        q = r;
                        continue;
                    }
                }
                Index<K, E> d = q.down();
                if (d != null) q = d;
                else return q;
            }
        }
    }

    /**
     * Returns node holding key or null if no such, clearing out any deleted
     * nodes seen along the way. Repeatedly traverses at base-level looking for
     * key starting at predecessor returned from findPredecessor, processing
     * base-level deletions as encountered. Some callers rely on this
     * side-effect of clearing deleted nodes. Restarts occur, at traversal step
     * centered on node n, if: (1) After reading n's next field, n is no longer
     * assumed predecessor b's current successor, which means that we don't have
     * a consistent 3-node snapshot and so cannot unlink any subsequent deleted
     * nodes encountered. (2) n's value field is null, indicating n is deleted,
     * in which case we help out an ongoing structural deletion before retrying.
     * Even though there are cases where such unlinking doesn't require restart,
     * they aren't sorted out here because doing so would not usually outweigh
     * cost of restarting. (3) n is a marker or n's predecessor's value field is
     * null, indicating (among other possibilities) that findPredecessor
     * returned a deleted node. We can't unlink the node because we don't know
     * its predecessor, so rely on another call to findPredecessor to notice and
     * return some earlier predecessor, which it will do. This check is only
     * strictly needed at beginning of loop, (and the b.value check isn't
     * strictly needed at all) but is done each iteration to help avoid
     * contention with other threads by callers that will fail to be able to
     * change links, and so will retry anyway. The traversal loops in doPut,
     * doRemove, and findNear all include the same three kinds of checks. And
     * specialized versions appear in findFirst, and findLast and their
     * variants. They can't easily share code because each uses the reads of
     * fields held in locals occurring in the orders they were performed.
     * @param key the key
     * @return node holding key, or null if no such
     */
    private final Node<K, E> findNode(Comparable<K> key) {
        Index<K, E> index = findIndex(key);
        if (index != null) return index.node();
        return null;
    }

    private final Node<K, E> findNode(K key) {
        Index<K, E> index = findIndex(key);
        if (index != null) return index.node();
        return null;
    }

    /**
     * Checks if is valid status.
     * @param predecessor the predecessor
     * @param successor the successor
     * @return true, if is valid status
     */
    private final boolean isValidStatus(Index<K, E> predecessor, Index<K, E> successor) {
        return successor == predecessor.getRight() && !successor.indexesDeletedNode()
                && !predecessor.indexesDeletedNode();
    }

    /**
     * Find index.
     * @param key the key
     * @return the index
     */
    private final Index<K, E> findIndex(Comparable<K> key) {
        while (true) {
            Index<K, E> b = findPredecessorIndex(key);
            while (true) {
                Index<K, E> n = b.getRight();
                if (n == null) return null;
                if (!isValidStatus(b, n)) break;
                int c = key.compareTo(n.key());
                if (c == 0) return n;
                if (c < 0) return null;
                b = n;
            }
        }
    }

    /**
     * Find index.
     * @param okey the okey
     * @return the index
     */
    private final Index<K, E> findIndex(K okey) {
        Comparable<K> key = comparable(okey);
        return findIndex(key);
    }

    /**
     * Gets value for key using findNode.
     * @param okey the key
     * @return the value, or null if absent
     */
    private final E doGet(K okey) {
        Comparable<K> key = comparable(okey);
        /*
         * Loop needed here and elsewhere in case value field goes
         * null just as it is about to be returned, in which case we
         * lost a race with a deletion, so must retry.
         */
        while (true) {
            Node<K, E> n = findNode(key);
            if (n == null) return null;
            if (!n.isDeleted()) return n.getElement();
        }
    }

    /* ---------------- Insertion -------------- */
    /**
     * Main insertion method. Adds element if not present, or replaces value if
     * present and onlyIfAbsent is false.
     * @param kkey the key
     * @param value the value that must be associated with key
     * @param onlyIfAbsent if should not insert if already present
     * @return the old value, or null if newly inserted
     */
    private final E doPut(Node<K, E> node, boolean onlyIfAbsent) {
        Comparable<K> key = comparable(node.getId());
        while (true) {
            Index<K, E> b = findPredecessorIndex(key);
            while (true) {
                Index<K, E> n = b.getRight();
                if (n != null) {
                    if (!isValidStatus(b, n)) break;
                    int c = key.compareTo(n.key());
                    if (c > 0) {
                        b = n;
                        continue;
                    }
                    if (c == 0) {
                        E v = n.value();
                        if (onlyIfAbsent || n.casValue(v, node.getElement())) return v;
                        else break;
                    }
                }
                Index<K, E> z = newFirstIndex(node);
                if (!b.link(n, z)) break;
                int level = randomLevel();
                if (level > 0) insertIndex(z, level);
                return null;
            }
        }
    }

    /**
     * New first index.
     * @param node the node
     * @return the index
     */
    private final Index<K, E> newFirstIndex(Node<K, E> node) {
        return new Index<>(node, null, null);
    }

    /**
     * Returns a random level for inserting a new node. Hardwired to k=1, p=0.5,
     * max 31 (see above and Pugh's "Skip List Cookbook
     * ", sec 3.4). This uses the simplest of the generators described in George Marsaglia's "
     * Xorshift RNGs" paper. This is not a high-quality generator but is
     * acceptable here.
     */
    private final int randomLevel() {
        int x = randomSeed;
        x ^= x << 13;
        x ^= x >>> 17;
        randomSeed = x ^= x << 5;
        if ((x & 0x80000001) != 0) // test highest and lowest bits
        return 0;
        int level = 1;
        while (((x >>>= 1) & 1) != 0)
            ++level;
        return level;
    }

    /**
     * Creates and adds index nodes for the given node.
     * @param z the node
     * @param level the level of the index
     */
    private final void insertIndex(Index<K, E> z, int level) {
        HeadIndex<K, E> h = getHeadIndex();
        int max = h.level;
        if (level <= max) {
            Index<K, E> idx = z;
            for (int i = 1; i <= level; ++i)
                idx = new Index<>(idx, null);
            addIndex(idx, h, level);
        } else { // Add a new level
            /*
             * To reduce interference by other threads checking for
             * empty levels in tryReduceLevel, new levels are added
             * with initialized right pointers. Which in turn requires
             * keeping levels in an array to access them while
             * creating new head index nodes from the opposite
             * direction.
             */
            int newLevel = max + 1;
            @SuppressWarnings("unchecked") Index<K, E>[] idxs = new Index[newLevel + 1];
            Index<K, E> idx = z;
            for (int i = 1; i <= newLevel; ++i)
                idxs[i] = idx = new Index<>(idx, null);
            HeadIndex<K, E> oldh;
            int k;
            while (true) {
                oldh = getHeadIndex();
                int oldLevel = oldh.level;
                if (newLevel <= oldLevel) { // lost race to add level
                    k = newLevel;
                    break;
                }
                HeadIndex<K, E> newh = oldh;
                Node<K, E> oldbase = oldh.node();
                for (int j = oldLevel + 1; j <= newLevel; ++j)
                    newh = new HeadIndex<>(oldbase, newh, idxs[j], j);
                if (casHeadIndex(oldh, newh)) {
                    k = oldLevel;
                    break;
                }
            }
            addIndex(idxs[k], oldh, k);
        }
    }

    /**
     * Adds given index nodes from given level down to 1.
     * @param idx the topmost index node being inserted
     * @param h the value of head to use to insert. This must be snapshotted by
     *            callers to provide correct insertion level
     * @param indexLevel the level of the index
     */
    private final void addIndex(Index<K, E> idx, HeadIndex<K, E> h, int indexLevel) {
        // Track next level to insert in case of retries
        int insertionLevel = indexLevel;
        Comparable<K> key = comparable(idx.key());
        if (key == null) throw new NullPointerException();
        // Similar to findPredecessor, but adding index nodes along path to key.
        while (true) {
            int j = h.level;
            Index<K, E> q = h;
            Index<K, E> t = idx;
            while (true) {
                Index<K, E> r = q.getRight();
                if (r != null) {
                    // compare before deletion check avoids needing recheck
                    if (r.indexesDeletedNode()) {
                        if (!q.unlink(r)) break;
                        continue;
                    }
                    int c = key.compareTo(r.key());
                    if (c > 0) {
                        q = r;
                        continue;
                    }
                }
                if (j == insertionLevel) {
                    // Don't insert index if node already deleted
                    if (t.indexesDeletedNode()) {
                        findNode(key); // cleans up
                        return;
                    }
                    if (!q.link(r, t)) break; // restart
                    if (--insertionLevel == 0) {
                        // need final deletion check before return
                        if (t.indexesDeletedNode()) findNode(key);
                        return;
                    }
                }
                if (--j >= insertionLevel && j < indexLevel) t = t.down;
                q = q.down;
                r = q.getRight();
            }
        }
    }

    /* ---------------- Deletion -------------- */
    /**
     * Main deletion method. Locates node, nulls value, appends a deletion
     * marker, unlinks predecessor, removes associated index nodes, and possibly
     * reduces head index level. Index nodes are cleared out simply by calling
     * findPredecessor. which unlinks indexes to deleted nodes found along path
     * to key, which will include the indexes to this node. This is done
     * unconditionally. We can't check beforehand whether there are index nodes
     * because it might be the case that some or all indexes hadn't been
     * inserted yet for this node during initial search for it, and we'd like to
     * ensure lack of garbage retention, so must call to be sure.
     * @param okey the key
     * @param value if non-null, the value that must be associated with key
     * @return the node, or null if not found
     */
    private final E doRemove(K okey, E value) {
        Comparable<K> key = comparable(okey);
        Index<K, E> b = findPredecessorIndex(key);
        return removeNode(b, key, value);
    }

    private final E removeNode(Index<K, E> basis, Comparable<K> key, E value) {
        while (true) {
            Index<K, E> b = basis;
            while (true) {
                Index<K, E> n = b.getRight();
                if (n == null) return null;
                Index<K, E> f = n.getRight();
                if (!isValidStatus(b, n)) break;
                int c = key.compareTo(n.key());
                if (c < 0) return null;
                if (c > 0) {
                    b = n;
                    continue;
                }
                E v = n.value();
                if (value != null && !value.equals(v)) return null;
                if (!n.casValue(v, null)) break;
                if (!b.casRight(n, f)) findNode(key);
                else {
                    findPredecessorIndex(key);
                    if (getHeadIndex().getRight() == null) tryReduceLevel();
                }
                return v;
            }
        }
    }

    /**
     * Possibly reduce head level if it has no nodes. This method can (rarely)
     * make mistakes, in which case levels can disappear even though they are
     * about to contain index nodes. This impacts performance, not correctness.
     * To minimize mistakes as well as to reduce hysteresis, the level is
     * reduced by one only if the topmost three levels look empty. Also, if the
     * removed level looks non-empty after CAS, we try to change it back quick
     * before anyone notices our mistake! (This trick works pretty well because
     * this method will practically never make mistakes unless current thread
     * stalls immediately before first CAS, in which case it is very unlikely to
     * stall again immediately afterwards, so will recover.) We put up with all
     * this rather than just let levels grow because otherwise, even a small map
     * that has undergone a large number of insertions and removals will have a
     * lot of levels, slowing down access more than would an occasional unwanted
     * reduction.
     */
    private final void tryReduceLevel() {
        HeadIndex<K, E> h = getHeadIndex();
        HeadIndex<K, E> d;
        HeadIndex<K, E> e;
        if (h.level > 3 && (d = (HeadIndex<K, E>) h.down()) != null && (e = (HeadIndex<K, E>) d.down()) != null
                && e.getRight() == null && d.getRight() == null && h.getRight() == null && casHeadIndex(h, d)
                && h.getRight() != null) casHeadIndex(d, h); // try to backout
    }

    /*----------------*/
    /* collection api */
    /*----------------*/
    public final boolean addAll(final Collection<? extends E> collection) {
        if (collection == null) throw new NullPointerException();
        boolean modified = false;
        for (final E e : collection)
            if (linkLast(newNode(e))) modified = true;
        return modified;
    }

    public final void clear() {
        final Lock lock = lock_;
        lock.lock();
        try {
            head_.setAfter(NULL_NODE);
            tail_.setBefore(NULL_NODE);
            count_.set(0);
            notFull_.signalAll();
        } finally {
            lock.unlock();
        }
    }

    public final boolean contains(final Object o) {
        if (o == null) return false;
        @SuppressWarnings("unchecked") final Node<K, E> node = findNode(comparable(((E) o).getIdentifier()));
        return node != null && node.isReal();
    }

    //    public final Iterator<E> iterator() {
    //        return new Itr();
    //    }
    /* --------- */
    /* deque api */
    /* --------- */
    //    public final void addFirst(final E e) {
    //        if (!offerFirst(e)) throw new IllegalStateException("FULL-or-EXISTS");
    //    }
    //
    //    public final void addLast(final E e) {
    //        if (!offerLast(e)) throw new IllegalStateException("FULL-or-EXISTS");
    //    }
    //
    //
    //    public final E pollFirst() {
    //        return unlinkFirstQ();
    //    }
    //
    //    public final E pollFirst(final long timeout, final TimeUnit unit) throws InterruptedException {
    //        long nanos = unit.toNanos(timeout);
    //        E e = null;
    //        while ((e = unlinkFirstQ()) == null) {
    //            if (nanos <= 0) return null;
    //            final Lock lock = lock_;
    //            lock.lockInterruptibly();
    //            try {
    //                nanos = notEmpty_.awaitNanos(nanos);
    //            } finally {
    //                lock.unlock();
    //            }
    //        }
    //        return e;
    //    }
    //
    //    public final E pollLast() {
    //        return unlinkLastQ();
    //    }
    //
    //    public final E pollLast(final long timeout, final TimeUnit unit) throws InterruptedException {
    //        long nanos = unit.toNanos(timeout);
    //        E e = null;
    //        while ((e = unlinkLastQ()) == null) {
    //            if (nanos <= 0) return null;
    //            final Lock lock = lock_;
    //            lock.lockInterruptibly();
    //            try {
    //                nanos = notEmpty_.awaitNanos(nanos);
    //            } finally {
    //                lock.unlock();
    //            }
    //        }
    //        return e;
    //    }
    //
    //    public final boolean offerFirst(final E e) {
    //        return linkFirst(newNode(e));
    //    }
    //
    //    public final boolean offerFirst(final E e, final long timeout, final TimeUnit unit)
    //            throws InterruptedException {
    //        final Node<K, E> node = newNode(e);
    //        long nanos = unit.toNanos(timeout);
    //        while (!linkFirst(node)) {
    //            if (nanos <= 0) return false;
    //            final Lock lock = lock_;
    //            lock.lockInterruptibly();
    //            try {
    //                nanos = notFull_.awaitNanos(nanos);
    //            } finally {
    //                lock.unlock();
    //            }
    //        }
    //        return true;
    //    }
    //
    //    public final boolean offerLast(final E e) {
    //        return linkLast(newNode(e));
    //    }
    //
    //    public final boolean offerLast(final E e, final long timeout, final TimeUnit unit)
    //            throws InterruptedException {
    //        final Node<K, E> node = newNode(e);
    //        long nanos = unit.toNanos(timeout);
    //        while (!linkLast(node)) {
    //            if (nanos <= 0) return false;
    //            final Lock lock = lock_;
    //            lock.lockInterruptibly();
    //            try {
    //                nanos = notFull_.awaitNanos(nanos);
    //            } finally {
    //                lock.unlock();
    //            }
    //        }
    //        return true;
    //    }
    //
    //    public final E takeFirst() throws InterruptedException {
    //        E e = null;
    //        while ((e = unlinkFirstQ()) == null) {
    //            final Lock lock = lock_;
    //            lock.lock();
    //            try {
    //                notEmpty_.await();
    //            } finally {
    //                lock.unlock();
    //            }
    //        }
    //        return e;
    //    }
    //
    //    public final E takeLast() throws InterruptedException {
    //        E e = null;
    //        while ((e = unlinkLastQ()) == null) {
    //            final Lock lock = lock_;
    //            lock.lock();
    //            try {
    //                notEmpty_.await();
    //            } finally {
    //                lock.unlock();
    //            }
    //        }
    //        return e;
    //    }
    //
    //    public final void putFirst(final E e) throws InterruptedException {
    //        final Node<K, E> node = newNode(e);
    //        while (!linkFirst(node)) {
    //            final Lock lock = lock_;
    //            lock.lock();
    //            try {
    //                notFull_.await();
    //            } finally {
    //                lock.unlock();
    //            }
    //        }
    //    }
    //
    //    public final void putLast(final E e) throws InterruptedException {
    //        final Node<K, E> node = newNode(e);
    //        while (!linkLast(node)) {
    //            final Lock lock = lock_;
    //            lock.lock();
    //            try {
    //                notFull_.await();
    //            } finally {
    //                lock.unlock();
    //            }
    //        }
    //    }
    //
    //    public final E removeFirst() {
    //        final E e = pollFirst();
    //        if (e == null) throw new NoSuchElementException();
    //        return e;
    //    }
    //
    //    public final boolean removeFirstOccurrence(final Object o) {
    //        if (o == null) return false;
    //        @SuppressWarnings("unchecked") final Node<K, E> node = findNode(comparable((((E) o).getIdentifier())));
    //        if (node == null || !node.isReal()) return false;
    //        return node.delete();
    //    }
    //
    //    public final E removeLast() {
    //        final E e = pollLast();
    //        if (e == null) throw new NoSuchElementException();
    //        return e;
    //    }
    //
    //    public final boolean removeLastOccurrence(final Object o) {
    //        return removeFirstOccurrence(o);
    //    }
    //
    //    public final Iterator<E> descendingIterator() {
    //        return new DescendingItr();
    //    }
    /** {@inheritDoc} */
    @Override public final int size() {
        return count_.intValue();
    }

    /* ------------------- */
    /* solid operation api */
    /* ------------------- */
    /** {@inheritDoc} */
    @Override public final String getId() {
        return id_;
    }

    /** {@inheritDoc} */
    @Override public List<E> clearTimedOutSolidly(final long timeout, final TimeUnit unit) {
        if (logger.isLoggable(FINER)) logger.entering("DqCollection",
            "clearTimedOutSolidly(long=" + timeout + ", TimeUnit=" + unit + ")", "start");
        if (timeout < 0) throw new IllegalArgumentException();
        final List<E> olds = new ArrayList<>();
        final long limit = unit.toNanos(timeout);
        final Lock lock = lock_;
        lock.lock();
        try {
            for (final Node<K, E> node : bottomHeadIndex_.getRight())
                if (node.getElapsedTime() > limit && !node.isReal()) olds.add(unlinkNodeSolidly(node));
        } finally {
            lock.unlock();
        }
        if (logger.isLoggable(FINER)) logger.exiting("DqCollection", "clearTimedOutSolidly(long, TimeUnit)",
            "end - return value=" + olds);
        return olds;
    }

    /** {@inheritDoc} */
    @Override public List<E> getTimedOutSolidly(final long timeout, final TimeUnit unit) {
        if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "getTimedOutSolidly");
        if (timeout < 0) throw new IllegalArgumentException();
        final List<E> olds = new ArrayList<>();
        final long limit = unit.toNanos(timeout);
        for (final Node<K, E> node : bottomHeadIndex_.getRight())
            if (node.getElapsedTime() > limit && !node.isReal()) olds.add(node.getElement());
        if (logger.isLoggable(FINER)) logger.exiting(getClass().getName(), "getTimedOutSolidly");
        return olds;
    }

    /**
     * Gets the expired list.
     * @return the expired list
     */
    public List<E> getExpiredList() {
        return getTimedOutSolidly(0, TimeUnit.NANOSECONDS);
    }

    /** {@inheritDoc} */
    @Override public E get(K key) {
        Node<K, E> node = findNode(comparable(key));
        return node.isReal() ? node.getElement() : null;
    }

    /** {@inheritDoc} */
    @Override public E remove(K key) {
        if (key == null) throw new NullPointerException();
        Node<K, E> node = findNode(comparable(key));
        return node != null && node.delete() ? node.getElement() : null;
    }

    /** {@inheritDoc} */
    @Override public E remove(K key, E expectValue) {
        if (key == null) throw new NullPointerException();
        Node<K, E> node = findNode(comparable(key));
        if (node == null || expectValue == null || !expectValue.equals(node.getElement())) return null;
        return node.delete() ? node.getElement() : null;
    }

    /** {@inheritDoc} */
    @Override public boolean linkFirst(E value) {
        Node<K, E> node = newNode(value);
        return linkFirst(node);
    }

    /** {@inheritDoc} */
    @Override public boolean linkLast(E value) {
        Node<K, E> node = newNode(value);
        return linkLast(node);
    }

    /** {@inheritDoc} */
    @Override public E unlinkFirst() {
        return unlinkFirstQ();
    }

    /** {@inheritDoc} */
    @Override public E unlinkLast() {
        return unlinkLastQ();
    }

    /** {@inheritDoc} */
    @Override public E putSolidly(final K key, final E value, final boolean putIfAbsent) {
        final Node<K, E> node = newNode(value);
        final E exists = getSolidly(key);
        if (exists == null && linkLast(node)) return null;
        else if (!putIfAbsent) return replaceSolidly(node);
        return value;
    }

    /** {@inheritDoc} */
    @Override public final E removeSolidly(final K key) {
        if (key == null) throw new NullPointerException();
        final Node<K, E> node = findNode(comparable(key));
        if (node == null) return null;
        return unlinkNodeSolidly(node);
    }

    /** {@inheritDoc} */
    @Override public E removeSolidly(K key, E expectValue) {
        if (key == null) throw new NullPointerException();
        final Node<K, E> node = findNode(comparable(key));
        if (node == null || expectValue == null || !expectValue.equals(node.getElement())) return null;
        return unlinkNodeSolidly(node);
    }

    /** {@inheritDoc} */
    @Override public final E getSolidly(final K key) {
        return doGet(key);
    }

    /** {@inheritDoc} */
    @Override public final int sizeSolidly() {
        return fullCount_.intValue();
    }

    /**
     * Replace real.
     * @param node the node
     * @return the node
     */
    private final E replaceSolidly(final Node<K, E> node) {
        if (node == null) throw new NullPointerException();
        if (node == NULL_NODE) throw new IllegalArgumentException("NULL NODE");
        final Node<K, E> existNode = findNode(comparable(node.getId()));
        E oldElement = existNode.getElement();
        if (existNode != null && node != existNode && existNode.casElement(oldElement, node.getElement()))
            return oldElement;
        return null;
    }

    /**
     * BASE UNIT JOB.
     * @param node the node
     * @return the e
     */
    private final E unlinkNodeSolidly(final Node<K, E> node) {
        if (node == null) throw new NullPointerException();
        if (node == NULL_NODE) throw new IllegalArgumentException("NULL NODE");
        final E existNode = doRemove(node.getId(), node.getElement());
        if (existNode == null) throw new NoSuchElementException("NOT EXISTS");
        if (node.isReal() && node.delete()) afterUnlink(node);
        fullCount_.decrementAndGet();
        node.gc();
        return node.getElement();
    }

    /** {@inheritDoc} */
    @Override public final E getFirst() {
        return head_.successor().getElement();
    }

    /** {@inheritDoc} */
    @Override public final E getLast() {
        return tail_.predecessor().getElement();
    }

    /** {@inheritDoc} */
    @Override public E unlinkFirst(long timeout, TimeUnit unit) throws InterruptedException {
        long nanos = unit.toNanos(timeout);
        final Lock lock = lock_;
        lock.lockInterruptibly();
        try {
            E x;
            while ((x = unlinkFirstQ()) == null) {
                if (nanos <= 0) return null;
                nanos = notEmpty_.awaitNanos(nanos);
            }
            return x;
        } finally {
            lock.unlock();
        }
    }

    /** {@inheritDoc} */
    @Override public E takeFirst() throws InterruptedException {
        final Lock lock = lock_;
        lock.lock();
        try {
            E x;
            while ((x = unlinkFirstQ()) == null)
                notEmpty_.await();
            return x;
        } finally {
            lock.unlock();
        }
    }

    /** {@inheritDoc} */
    @Override public E unlinkLast(long timeout, TimeUnit unit) throws InterruptedException {
        long nanos = unit.toNanos(timeout);
        final Lock lock = lock_;
        lock.lockInterruptibly();
        try {
            E x;
            while ((x = unlinkLastQ()) == null) {
                if (nanos <= 0) return null;
                nanos = notEmpty_.awaitNanos(nanos);
            }
            return x;
        } finally {
            lock.unlock();
        }
    }

    /** {@inheritDoc} */
    @Override public E takeLast() throws InterruptedException {
        final Lock lock = lock_;
        lock.lock();
        try {
            E x;
            while ((x = unlinkLastQ()) == null)
                notEmpty_.await();
            return x;
        } finally {
            lock.unlock();
        }
    }

    /*----------*/
    /* unit job */
    /*----------*/
    private final void linkFirstQ(Node<K, E> node) {
        while (!head_.successor().prepend(node));
    }

    private final void linkLastQ(Node<K, E> node) {
        while (!tail_.predecessor().append(node));
    }

    private final boolean linkFirst(final Node<K, E> node) {
        if (node == null) throw new NullPointerException();
        if (node == NULL_NODE) throw new IllegalArgumentException("NULL NODE");
        if (isFull()) return false;
        if (doPut(node, true) != null) return false;
        linkFirstQ(node);
        afterLink(node);
        return true;
    }

    /**
     * BASE UNIT JOB.
     * @param node the node
     * @return true, if successful
     */
    private final boolean linkLast(final Node<K, E> node) {
        if (node == null) throw new NullPointerException();
        if (node == NULL_NODE) throw new IllegalArgumentException("NULL NODE");
        if (isFull()) return false;
        if (doPut(node, true) != null) return false;
        linkLastQ(node);
        afterLink(node);
        return true;
    }

    /** @param node */
    private final void afterLink(final Node<K, E> node) {
        count_.incrementAndGet();
        fullCount_.incrementAndGet();
        listener_.processItemAdded(node.getElement());
        notEmpty_.signal();
    }

    /**
     * BASE UNIT JOB.
     * @return the e
     */
    private final E unlinkFirstQ() {
        while (true) {
            Node<K, E> h = head_.successor();
            if (h == NULL_NODE) return null;
            if (h.delete()) {
                afterUnlink(h);
                return h.getElement();
            }
        }
    }

    /** @param node */
    private void afterUnlink(Node<K, E> node) {
        count_.decrementAndGet();
        listener_.processItemRemoved(node.getElement());
        notFull_.signal();
    }

    /**
     * BASE UNIT JOB.
     * @return the e
     */
    private final E unlinkLastQ() {
        while (true) {
            Node<K, E> p = tail_.predecessor();
            if (p == NULL_NODE) return null;
            if (p.delete()) {
                afterUnlink(p);
                return p.getElement();
            }
        }
    }

    /** {@inheritDoc} */
    @Override public E restoreToFirst(K key) {
        if (key == null) throw new NullPointerException();
        Node<K, E> node = findNode(key);
        if (node != null && node != NULL_NODE && !node.isReal() && !node.isDeleted()) {
            node.markReal();
            linkFirstQ(node);
            return node.getElement();
        }
        return null;
    }

    /** {@inheritDoc} */
    @Override public E restoreToLast(K key) {
        if (key == null) throw new NullPointerException();
        Node<K, E> node = findNode(key);
        if (node != null && node != NULL_NODE && !node.isReal() && !node.isDeleted()) {
            node.markReal();
            linkLastQ(node);
            return node.getElement();
        }
        return null;
    }

    /**
     * New node.
     * @param e the e
     * @return the node
     */
    private final Node<K, E> newNode(final E e) {
        if (e == null) throw new NullPointerException(); // cannot make null node
        final Node<K, E> node = new Node<>(e);
        return node;
    }

    /**
     * Checks if is full.
     * @return true, if is full
     */
    protected final boolean isFull() {
        return false;
    }

    /**
     * Read object. 일단 LinkedBlockingDeque에 있어서 옮겨 놓긴 했지만 고려되지 않았음
     * @param is the is
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws ClassNotFoundException the class not found exception
     */
    private void readObject(final ObjectInputStream is) throws IOException, ClassNotFoundException {
        is.defaultReadObject();
        count_.set(0);
        while (true) {
            @SuppressWarnings("unchecked") final E e = (E) is.readObject();
            if (e == null) break;
            //            add(e);
        }
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
            for (Node<K, E> n = head_.getAfter(); n.isReal(); n = n.getAfter())
                os.writeObject(n.getElement());
            os.writeObject(null);
        } finally {
            lock.unlock();
        }
    }

    //
    //    /** The Class AbstractItr. */
    //    private abstract class AbstractItr implements Iterator<E> {
    //        /** The next. */
    //        private Node<K, E> next;
    //        /** The next item. */
    //        private E nextItem;
    //        /** The last ret. */
    //        private Node<K, E> lastRet;
    //
    //        /** Instantiates a new abstract itr. */
    //        private AbstractItr() {
    //            final Lock lock = lock_;
    //            lock.lock();
    //            try {
    //                next = firstNode();
    //                nextItem = isValid(next) ? next.getElement() : null;
    //            } finally {
    //                lock.unlock();
    //            }
    //        }
    //
    //        /**
    //         * Checks for next.
    //         * @return true, if successful
    //         */
    //        @Override public boolean hasNext() {
    //            return isValid(next);
    //        }
    //
    //        /**
    //         * Next.
    //         * @return the e
    //         */
    //        @Override public E next() {
    //            if (!isValid(next)) throw new NoSuchElementException();
    //            lastRet = next;
    //            final E e = nextItem;
    //            advance();
    //            return e;
    //        }
    //
    //        /** Removes the. */
    //        @Override public void remove() {
    //            final Node<K, E> n = lastRet;
    //            if (isValid(n)) throw new IllegalStateException();
    //            lastRet = null;
    //            //            if (n.getElement() != null) removeLastOccurrence(n);
    //        }
    //
    //        /**
    //         * First node.
    //         * @return the node
    //         */
    //        abstract Node<K, E> firstNode();
    //
    //        /**
    //         * Next node.
    //         * @param n the n
    //         * @return the node
    //         */
    //        abstract Node<K, E> nextNode(Node<K, E> n);
    //
    //        /** Advance. */
    //        private void advance() {
    //            final Lock lock = lock_;
    //            lock.lock();
    //            try {
    //                next = succ(next);
    //                nextItem = isValid(next) ? next.getElement() : null;
    //            } finally {
    //                lock.unlock();
    //            }
    //        }
    //
    //        /**
    //         * Checks if is valid.
    //         * @param node the node
    //         * @return true, if is valid
    //         */
    //        private final boolean isValid(final Node<K, E> node) {
    //            return node != null && node.isReal();
    //        }
    //
    //        /**
    //         * Succ.
    //         * @param n the n
    //         * @return the node
    //         */
    //        private final Node<K, E> succ(Node<K, E> n) {
    //            for (;;) {
    //                final Node<K, E> s = nextNode(n);
    //                if (!isValid(s)) return null;
    //                else if (s.getElement() != null) return s;
    //                else if (s == n) return firstNode();
    //                else n = s;
    //            }
    //        }
    //    }
    //
    //    /** The Class DescendingItr. */
    //    private final class DescendingItr extends AbstractItr {
    //        /**
    //         * First node.
    //         * @return the node
    //         */
    //        @Override Node<K, E> firstNode() {
    //            return tail_.getBefore();
    //        }
    //
    //        /**
    //         * Next node.
    //         * @param n the n
    //         * @return the node
    //         */
    //        @Override Node<K, E> nextNode(final Node<K, E> n) {
    //            return n.getBefore();
    //        }
    //    }
    //
    //    /** The Class Itr. */
    //    private final class Itr extends AbstractItr {
    //        /**
    //         * First node.
    //         * @return the node
    //         */
    //        @Override Node<K, E> firstNode() {
    //            return head_.getAfter();
    //        }
    //
    //        /**
    //         * Next node.
    //         * @param n the n
    //         * @return the node
    //         */
    //        @Override Node<K, E> nextNode(final Node<K, E> n) {
    //            return n.getAfter();
    //        }
    //    }
    /**
     * The listener interface for receiving doNothingEvent events. The class
     * that is interested in processing a doNothingEvent event implements this
     * interface, and the object created with that class is registered with a
     * component using the component's
     * <code>addDoNothingEventListener<code> method. When the doNothingEvent event
     * occurs, that object's appropriate method is invoked.
     */
    private class DoNothingEventListener implements IDqItemEventListener<E> {
        /**
         * Process item added.
         * @param e the e
         */
        @Override public void processItemAdded(final E e) {
        }

        /**
         * Process item removed.
         * @param e the e
         */
        @Override public void processItemRemoved(final E e) {
        }
    }

    /**
     * The Class Node. 노드의 상태값은 mark 되어
     * @param <X> the key type
     * @param <Y> the element type
     */
    private static final class Node<X, Y extends IDqElement<X>> extends
            AtomicMarkableReference<Node<X, Y>>
            implements Serializable {
        /** The Constant serialVersionUID. */
        private static final long serialVersionUID = -3008752467874171657L;
        /** The prev. */
        private AtomicReference<Node<X, Y>> before_;
        /** The element. */
        private final AtomicReference<Y> element_;
        /** The timestamp. */
        private transient long timestamp_ = -1L;

        /**
         * Instantiates a new node.
         * @param obj the obj
         */
        private Node(final Y obj) {
            this(obj, null);
        }

        /**
         * Instantiates a new node.
         * @param obj the obj
         * @param next the next e
         */
        private Node(final Y obj, Node<X, Y> next) {
            this(obj, null, next);
        }

        /**
         * Instantiates a new node.
         * @param obj the obj
         * @param pre the pre
         * @param next the next
         */
        private Node(final Y obj, Node<X, Y> pre, Node<X, Y> next) {
            super(next, false);
            element_ = new AtomicReference<>(obj);
            before_ = new AtomicReference<>(pre);
        }

        private final boolean markNotReal(Node<X, Y> expect) {
            final boolean marked = attemptMark(expect, true);
            if (marked) stampTime();
            return marked;
        }

        private final void markReal() {
            timestamp_ = -1L;
            set(null, false);
        }

        private final boolean casAfter(Node<X, Y> expect, Node<X, Y> node) {
            return compareAndSet(expect, node, false, false);
        }

        private final Node<X, Y> getBefore() {
            return before_.get();
        }

        private final Node<X, Y> getAfter() {
            return getReference();
        }

        public final boolean delete() {
            Node<X, Y> nextNode = null;
            while (true) {
                if (isMarked()) return false;
                nextNode = getAfter();
                if (markNotReal(nextNode)) break;
            }
            nextNode.predecessor();
            return true;
        }

        public final Node<X, Y> successor() {
            Node<X, Y> nextNode = getAfter();
            while (true) {
                if (!nextNode.isMarked()) {
                    reflectForward();
                    return nextNode;
                }
                Node<X, Y> nextNextNode = nextNode.getAfter();
                casAfter(nextNode, nextNextNode);
                nextNode = nextNextNode;
            }
        }

        private final void reflectForward() {
            if (isMarked()) return;
            final Node<X, Y> nextNode = getAfter();
            final Node<X, Y> nextPrevNode = nextNode.getBefore();
            if (nextPrevNode == this) return;
            if (!nextNode.isMarked()) nextNode.setBefore(this);
        }

        /**
         * Returns the apparent predecessor of target by searching forward for
         * it, starting at this node, patching up pointers while traversing.
         * Used by predecessor().
         * @return target's predecessor, or null if not found
         */
        private final Node<X, Y> fixForwardUntil(Node<X, Y> target) {
            Node<X, Y> node = this;
            while (true) {
                if (isMarked() || target.isMarked()) return null;
                Node<X, Y> nextNode = node.successor();
                if (nextNode == target) return node;
                node = nextNode;
            }
        }

        public final Node<X, Y> predecessor() {
            Node<X, Y> node = this;
            while (true) {
                Node<X, Y> prevNode = node.getBefore();
                Node<X, Y> prevNextNode = prevNode.getAfter();
                if (prevNode.isMarked()) node = prevNode;
                else if (prevNextNode == this) return prevNode;
                else {
                    Node<X, Y> mayBeBack = prevNode.fixForwardUntil(this);
                    if (mayBeBack != null) return mayBeBack;
                }
            }
        }

        /**
         * Tries to insert a node holding element as successor, failing if this
         * node is deleted.
         * @param newNode the element
         * @return the new node, or null on failure
         */
        public final boolean append(Node<X, Y> newNode) {
            final Node<X, Y> prevNode = this;
            while (true) {
                if (isMarked()) return false;
                Node<X, Y> nextNode = successor();
                newNode.setBefore(prevNode);
                newNode.setAfter(nextNode);
                if (prevNode.casAfter(nextNode, newNode)) {
                    newNode.reflectForward();
                    return true;
                }
            }
        }

        /**
         * Tries to insert a node holding element as predecessor, failing if no
         * live predecessor can be found to link to.
         * @param newNode the element
         * @return the new node, or null on failure
         */
        public final boolean prepend(Node<X, Y> newNode) {
            Node<X, Y> nextNode = this;
            while (true) {
                if (isMarked()) return false;
                Node<X, Y> prevNode = predecessor();
                newNode.setBefore(prevNode);
                newNode.setAfter(nextNode);
                if (prevNode.casAfter(nextNode, newNode)) {
                    newNode.reflectForward();
                    return true;
                }
            }
        }

        private final void setBefore(Node<X, Y> node) {
            before_.set(node);
        }

        private final void setAfter(Node<X, Y> node) {
            set(node, false);
        }

        /** help GC. */
        final void gc() {
            if (!isMarked() || !isDeleted()) return;
            setBefore(null);
            set(null, true);
        }

        private final Y getElement() {
            return element_.get();
        }

        private final boolean casElement(Y elem, Y newElem) {
            return element_.compareAndSet(elem, newElem);
        }

        /** {@inheritDoc} */
        @Override public final boolean equals(final Object obj) {
            if (obj == null || element_ == null || getElement() == null) return false;
            if (obj instanceof Node) return getElement().equals(((Node) obj).getElement());
            return super.equals(obj);
        }

        /** {@inheritDoc} */
        @Override public final int hashCode() {
            if (getElement() == null) return 0;
            return getElement().hashCode();
        }

        /**
         * Gets the elapsed time from timestamp till now.<br/>
         * timestamp가 음수인 경우는 head-tail구간에서 제외되지 않았다고 본다.
         * @return the elapsed time
         */
        private final long getElapsedTime() {
            if (timestamp_ < 0) return 0;
            return System.nanoTime() - NANO_BASE - timestamp_;
        }

        /**
         * Gets the id.
         * @return the id
         */
        private final X getId() {
            if (getElement() != null && !isBaseHeader()) return getElement().getIdentifier();
            return null;
        }

        /**
         * Checks if is real.
         * @return true, if is real
         */
        private final boolean isReal() {
            return !isBaseHeader() && !isMarked();
        }

        /**
         * Stamp time.
         * @return the long
         */
        private final long stampTime() {
            timestamp_ = System.nanoTime() - NANO_BASE;
            return timestamp_;
        }

        private final boolean isDeleted() {
            return !isBaseHeader() && getElement() == null;
        }

        /**
         * Checks if is base header.
         * @return true, if is base header
         */
        private final boolean isBaseHeader() {
            return element_ == BASE_HEADER;
        }

        private final AbstractMap.SimpleImmutableEntry<X, Y> createSnapshot() {
            if (isBaseHeader() || getElement() == null) return null;
            return new AbstractMap.SimpleImmutableEntry<>(getId(), getElement());
        }
    }

    /** Nodes heading each level keep track of their level. */
    private static final class HeadIndex<S, T extends IDqElement<S>> extends Index<S, T> {
        /** The Constant serialVersionUID. */
        private static final long serialVersionUID = 3547797323824693918L;
        final int level;

        private HeadIndex(Node<S, T> node, Index<S, T> down, Index<S, T> right, int level) {
            super(node, down, right);
            this.level = level;
        }

        private HeadIndex(Index<S, T> down, Index<S, T> right, int level) {
            super(down, right);
            this.level = level;
        }

        private HeadIndex(HeadIndex<S, T> down, Index<S, T> right) {
            super(down, right);
            this.level = down.level + 1;
        }
    }

    /* ---------------- Indexing -------------- */
    /**
     * Index nodes represent the levels of the skip list. Note that even though
     * both Nodes and Indexes have forward-pointing fields, they have different
     * types and are handled in different ways, that can't nicely be captured by
     * placing field in a shared abstract class.
     */
    private static class Index<I, J extends IDqElement<I>> extends AtomicReference<Index<I, J>>
            implements Iterable<Node<I, J>>, Iterator<Node<I, J>> {
        /** The Constant serialVersionUID. */
        private static final long serialVersionUID = 6717821668181651376L;
        private final Node<I, J> node;
        private final Index<I, J> down;

        Index(Node<I, J> node, Index<I, J> down, Index<I, J> right) {
            super(right);
            this.node = node;
            this.down = down;
        }

        Index(Index<I, J> down, Index<I, J> right) {
            this(down.node, down, right);
        }

        final boolean casRight(Index<I, J> cmp, Index<I, J> val) {
            return compareAndSet(cmp, val);
        }

        final I key() {
            return node.getId();
        }

        final boolean casValue(J expect, J newValue) {
            return node.casElement(expect, newValue);
        }

        final J value() {
            return node.getElement();
        }

        final Index<I, J> down() {
            return down;
        }

        final Node<I, J> node() {
            return node;
        }

        final void setRight(Index<I, J> val) {
            set(val);
        }

        final Index<I, J> getRight() {
            return get();
        }

        final boolean link(Index<I, J> succ, Index<I, J> newSucc) {
            newSucc.setRight(succ);
            return !indexesDeletedNode() && casRight(succ, newSucc);
        }

        final boolean unlink(Index<I, J> succ) {
            return !indexesDeletedNode() && casRight(succ, succ.getRight());
        }

        /**
         * Returns true if the node this indexes has been deleted.
         * @return true if indexed node is known to be deleted
         */
        final boolean indexesDeletedNode() {
            if (node == null) return true;
            return node.isDeleted();
        }

        /** {@inheritDoc} */
        @Override public boolean hasNext() {
            Index<I, J> idx = getRight();
            return idx != null && idx.indexesDeletedNode();
        }

        /** {@inheritDoc} */
        @Override public Node<I, J> next() {
            return getRight().node;
        }

        /** {@inheritDoc} */
        @Override public void remove() {
            throw new UnsupportedOperationException();
        }

        /** {@inheritDoc} */
        @Override public Iterator<Node<I, J>> iterator() {
            return this;
        }
    }
}
