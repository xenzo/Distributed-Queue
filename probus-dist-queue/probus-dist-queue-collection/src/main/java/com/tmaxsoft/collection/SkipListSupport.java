package com.tmaxsoft.collection;

import java.util.AbstractMap;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;

/**
 * The Class SkipListSupport.
 * @param <K> the key type
 * @param <V> the value type
 */
class SkipListSupport<K, V> extends LinkedDequeSupport<V> {
        /** The random seed. */
        private transient int _randomSeed = new Random(System.currentTimeMillis()).nextInt() | 0x0100;
        /** The bottom head index. */
        private final transient HeadIndex<K, V> _bottomHeadIndex = new HeadIndex<K, V>(NULL_NODE, null, null, 1);
        /** The topmost head index of the skiplist. */
        private final transient AtomicReference<HeadIndex<K, V>> _topmostHeadIndex = new AtomicReference<HeadIndex<K, V>>(_bottomHeadIndex);
        /**
         * The comparator used to maintain order in this map, or null if using
         * natural ordering.
         */
        private final transient Comparator<K> _comparator;

        /**
         * Instantiates a new skip list support.
         */
        SkipListSupport() {
                this(null);
        }

        /**
         * Instantiates a new skip list support.
         * @param comparator the comparator
         */
        SkipListSupport(final Comparator<K> comparator) {
                this(comparator, null);
        }

        SkipListSupport(final Comparator<K> keyComparator, final Comparator<V> valueComparator) {
                super(valueComparator);
                _comparator = keyComparator;
        }

        /**
         * Key iterator.
         * @return the iterator
         */
        public final Iterator<K> keyIterator() {
                return new KeyIter();
        }

        /**
         * Value iterator.
         * @return the iterator
         */
        public final Iterator<V> valueIterator() {
                return new ValueIter();
        }

        /**
         * Entry iterator.
         * @return the iterator
         */
        public final Iterator<Entry<K, V>> entryIterator() {
                return new EntryIter();
        }

        final Iterator<Index<K, V>> indexIterator() {
                return new IndexIter();
        }

        /**
         * find first.
         * @return the index
         */
        private final Index<K, V> _findFIrst() {
                Index<K, V> index = _bottomHeadIndex.right();
                while (index != null && index.indexesDeletedNode())
                        index = index.right();
                return index;
        }

        /**
         * If using comparator, return a ComparableUsingComparator, else cast
         * key as Comparable, which may cause ClassCastException, which is
         * propagated back to caller.
         * @param key the key
         * @return the comparable
         * @throws ClassCastException the class cast exception
         */
        @SuppressWarnings("unchecked")
        private final Comparable<K> _comparable(final K key) throws ClassCastException {
                if (key == null) throw new NullPointerException();
                if (_comparator != null) return new ComparableUsingComparator<K>(key, _comparator);
                else return (Comparable<K>) key;
        }

        /* ---------------- Traversal -------------- */

        /**
         * Do get.
         * @param key the key
         * @return the v
         */
        final V doGet(final K key) {
                final Comparable<K> c = _comparable(key);
                /*
                 * Loop needed here and elsewhere in case value field goes
                 * null just as it is about to be returned, in which case we
                 * lost a race with a deletion, so must retry.
                 */
                while (true) {
                        final Node<V> n = _findNode(c);
                        if (n == null) return null;
                        if (!n.isDeleted()) return n.element();
                }
        }

        /**
         * Returns node holding key or null if no such, clearing out any deleted
         * nodes seen along the way. Repeatedly traverses at base-level looking
         * for key starting at predecessor returned from findPredecessor,
         * processing base-level deletions as encountered. Some callers rely on
         * this side-effect of clearing deleted nodes. Restarts occur, at
         * traversal step centered on node n, if: (1) After reading n's next
         * field, n is no longer assumed predecessor b's current successor,
         * which means that we don't have a consistent 3-node snapshot and so
         * cannot unlink any subsequent deleted nodes encountered. (2) n's value
         * field is null, indicating n is deleted, in which case we help out an
         * ongoing structural deletion before retrying. Even though there are
         * cases where such unlinking doesn't require restart, they aren't
         * sorted out here because doing so would not usually outweigh cost of
         * restarting. (3) n is a marker or n's predecessor's value field is
         * null, indicating (among other possibilities) that findPredecessor
         * returned a deleted node. We can't unlink the node because we don't
         * know its predecessor, so rely on another call to findPredecessor to
         * notice and return some earlier predecessor, which it will do. This
         * check is only strictly needed at beginning of loop, (and the b.value
         * check isn't strictly needed at all) but is done each iteration to
         * help avoid contention with other threads by callers that will fail to
         * be able to change links, and so will retry anyway. The traversal
         * loops in doPut, doRemove, and findNear all include the same three
         * kinds of checks. And specialized versions appear in findFirst, and
         * findLast and their variants. They can't easily share code because
         * each uses the reads of fields held in locals occurring in the orders
         * they were performed.
         * @param key the key
         * @return node holding key, or null if no such
         */
        private final Node<V> _findNode(final Comparable<K> key) {
                final Index<K, V> index = _findIndex(key);
                if (index != null) return index.node();
                return null;
        }

        /**
         * Find index.
         * @param key the key
         * @return the index
         */
        private final Index<K, V> _findIndex(final Comparable<K> key) {
                while (true) {
                        Index<K, V> b = _findPredecessorIndex(key);
                        while (true) {
                                final Index<K, V> n = b.right();
                                if (n == null) return null;
                                if (!_isValidStatus(b, n)) break;
                                final int c = key.compareTo(n.key());
                                if (c == 0) return n;
                                if (c < 0) return null;
                                b = n;
                        }
                }
        }

        /**
         * Returns a base-level node with key strictly less than given key, or
         * the base-level header if there is no such node. Also unlinks indexes
         * to deleted nodes found along the way. Callers rely on this
         * side-effect of clearing indices to deleted nodes.
         * @param key the key
         * @return a predecessor of key
         */
        private final Index<K, V> _findPredecessorIndex(final Comparable<K> key) {
                if (key == null) throw new NullPointerException(); // don't postpone errors
                while (true) {
                        Index<K, V> q = _getHeadIndex();
                        while (true) {
                                final Index<K, V> r = q.right();
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
                                final Index<K, V> d = q.down();
                                if (d != null) q = d;
                                else return q;
                        }
                }
        }

        /**
         * _get head index.
         * @return the head index
         */
        private final HeadIndex<K, V> _getHeadIndex() {
                return _topmostHeadIndex.get();
        }

        /**
         * Checks if is valid status.
         * @param predecessor the predecessor
         * @param successor the successor
         * @return true, if is valid status
         */
        private final boolean _isValidStatus(final Index<K, V> predecessor, final Index<K, V> successor) {
                return successor == predecessor.right()
                    && !successor.indexesDeletedNode()
                    && !predecessor.indexesDeletedNode();
        }

        /* ---------------- Insertion -------------- */

        /**
         * Adds element if not present.
         * @param key the key
         * @param value the value
         * @return the added node, or null if already present
         */
        final Node<V> putNode(final K key, final V value) {
                final Comparable<K> k = _comparable(key);
                while (true) {
                        Index<K, V> b = _findPredecessorIndex(k);
                        while (true) {
                                final Index<K, V> n = b.right();
                                if (n != null) {
                                        if (!_isValidStatus(b, n)) break;
                                        final int c = k.compareTo(n.key());
                                        if (c > 0) {
                                                b = n;
                                                continue;
                                        }
                                        if (c == 0) return null;//already exists
                                }
                                final Node<V> node = newNode(value);
                                final Index<K, V> z = _newBottomIndex(key, node);
                                if (!b.link(n, z)) break;
                                final int level = _randomLevel();
                                if (level > 1) _buildIndex(key, z, level);
                                return node;
                        }
                }
        }

        /**
         * Creates and adds index nodes for the given node.
         * @param bottom the node
         * @param level the level of the index
         */
        private final void _buildIndex(final K key, final Index<K, V> bottom, final int level) {
                final HeadIndex<K, V> h = _getHeadIndex();
                final int max = h.level();
                if (level <= max) {
                        Index<K, V> idx = bottom;
                        for (int i = 1; i <= level; ++i)
                                idx = _newIndex(key, idx, null);
                        _attachIndex(idx, h, level);
                } else { // Add a new level
                        /*
                         * To reduce interference by other threads checking for
                         * empty levels in tryReduceLevel, new levels are added
                         * with initialized right pointers. Which in turn requires
                         * keeping levels in an array to access them while
                         * creating new head index nodes from the opposite
                         * direction.
                         */
                        final int l = max + 1;
                        @SuppressWarnings("unchecked") final Index<K, V>[] idxs = new Index[l + 1];
                        Index<K, V> idx = bottom;
                        for (int i = 1; i <= l; ++i)
                                idxs[i] = idx = _newIndex(key, idx, null);
                        HeadIndex<K, V> oldh;
                        int k;
                        while (true) {
                                oldh = _getHeadIndex();
                                final int oldLevel = oldh._level;
                                if (l <= oldLevel) { // lost race to add level
                                        k = l;
                                        break;
                                }
                                HeadIndex<K, V> newh = oldh;
                                final Node<V> oldbase = oldh.node();
                                for (int j = oldLevel + 1; j <= l; ++j)
                                        newh = new HeadIndex<K, V>(oldbase, newh, idxs[j], j);
                                if (_casTopmostHeadIndex(oldh, newh)) {
                                        k = oldLevel;
                                        break;
                                }
                        }
                        _attachIndex(idxs[k], oldh, k);
                }
        }

        /**
         * Adds given index nodes from given level down to 1.
         * @param idx the topmost index node being inserted
         * @param h the value of head to use to insert. This must be snapshotted
         * by callers to provide correct insertion level
         * @param indexLevel the level of the index
         */
        private final void _attachIndex(final Index<K, V> idx, final HeadIndex<K, V> h, final int indexLevel) {
                // Track next level to insert in case of retries
                int insertionLevel = indexLevel;
                final Comparable<K> key = _comparable(idx.key());
                if (key == null) throw new NullPointerException();
                // Similar to findPredecessor, but adding index nodes along path to key.
                while (true) {
                        int j = h.level();
                        Index<K, V> q = h;
                        Index<K, V> t = idx;
                        while (true) {
                                final Index<K, V> r = q.right();
                                if (r != null) {
                                        // compare before deletion check avoids needing recheck
                                        if (r.indexesDeletedNode()) {
                                                if (!q.unlink(r)) break;
                                                continue;
                                        }
                                        final int c = key.compareTo(r.key());
                                        if (c > 0) {
                                                q = r;
                                                continue;
                                        }
                                }
                                if (j == insertionLevel) {
                                        // Don't insert index if node already deleted
                                        if (t.indexesDeletedNode()) {
                                                _findNode(key); // cleans up
                                                return;
                                        }
                                        if (!q.link(r, t)) break; // restart
                                        if (--insertionLevel == 0) {
                                                // need final deletion check before return
                                                if (t.indexesDeletedNode()) _findNode(key);
                                                return;
                                        }
                                }
                                if (--j >= insertionLevel && j < indexLevel) t = t._down;
                                q = q._down;
                        }
                }
        }

        /**
         * Returns a random level for inserting a new node. Hardwired to k=1,
         * p=0.5, max 31 (see above and Pugh's "Skip List Cookbook
         * ", sec 3.4). This uses the simplest of the generators described in George Marsaglia's "
         * Xorshift RNGs" paper. This is not a high-quality generator but is
         * acceptable here.
         * @return the int
         */
        private final int _randomLevel() {
                int x = _randomSeed;
                x ^= x << 13;
                x ^= x >>> 17;
                _randomSeed = x ^= x << 5;
                if ((x & 0x80000001) != 0) // test highest and lowest bits
                        return 0;
                int level = 1;
                while (((x >>>= 1) & 1) != 0)
                        ++level;
                return level;
        }

        /**
         * New index.
         * @param key the key
         * @param node the node
         * @param down the down
         * @param right the right
         * @return the index
         */
        Index<K, V> newIndex(final K key, final Node<V> node, final Index<K, V> down, final Index<K, V> right) {
                if (key == null) throw new NullPointerException();
                return node == null ? new Index<K, V>(key, down, right) : new Index<K, V>(key, node, down, right);
        }

        /**
         * New index.
         * @param key the key
         * @param down the down
         * @param right the right
         * @return the index
         */
        private final Index<K, V> _newIndex(final K key, final Index<K, V> down, final Index<K, V> right) {
                return newIndex(key, down.node(), down, right);
        }

        /**
         * New first index.
         * @param key the key
         * @param node the node
         * @return the index
         */
        private final Index<K, V> _newBottomIndex(final K key, final Node<V> node) {
                return newIndex(key, node, null, null);
        }

        /**
         * _cas topmost head index.
         * @param cmp the cmp
         * @param value the value
         * @return true, if successful
         */
        private final boolean _casTopmostHeadIndex(final HeadIndex<K, V> cmp, final HeadIndex<K, V> value) {
                return _topmostHeadIndex.compareAndSet(cmp, value);
        }

        /* ---------------- Deletion -------------- */

        /**
         * Do remove.
         * @param okey the okey
         * @return the v
         */
        final V doRemove(final K okey) {
                return doRemove(okey, null);
        }

        /**
         * Main deletion method. Locates node, nulls value, appends a deletion
         * marker, unlinks predecessor, removes associated index nodes, and
         * possibly reduces head index level. Index nodes are cleared out simply
         * by calling findPredecessor. which unlinks indexes to deleted nodes
         * found along path to key, which will include the indexes to this node.
         * This is done unconditionally. We can't check beforehand whether there
         * are index nodes because it might be the case that some or all indexes
         * hadn't been inserted yet for this node during initial search for it,
         * and we'd like to ensure lack of garbage retention, so must call to be
         * sure.
         * @param okey the key
         * @param value if non-null, the value that must be associated with key
         * @return the node, or null if not found
         */
        final V doRemove(final K okey, final V value) {
                final Comparable<K> key = _comparable(okey);
                final Index<K, V> b = _findPredecessorIndex(key);
                return _remove(b, key, value);
        }

        /**
         * _remove.
         * @param basis the basis
         * @param key the key
         * @param value the value
         * @return the v
         */
        private final V _remove(final Index<K, V> basis, final Comparable<K> key, final V value) {
                while (true) {
                        Index<K, V> b = basis;
                        while (true) {
                                final Index<K, V> n = b.right();
                                if (n == null) return null;
                                if (!_isValidStatus(b, n)) break;
                                final int c = key.compareTo(n.key());
                                if (c < 0) return null;
                                if (c > 0) {
                                        b = n;
                                        continue;
                                }
                                final V v = n.value();
                                if (value != null && !value.equals(v)) return null;
                                return _deleteNode(n);
                        }
                }
        }

        /**
         * delete node.
         * @param index the index
         * @return the v
         */
        private final V _deleteNode(final Index<K, V> index) {
                Node<V> node = null;
                if (index == null || (node = index.node()) == null || node.element() == null)
                        throw new NullPointerException();
                if (node.isDeleted()
                    || (node.isReal() && !node.delete())
                    || !node.deleteSolidly()) return null;
//                if (node.isDeleted()) return null;
//                if (node.isReal()) node.delete();
//                node.deleteSolidly();
                if (_getHeadIndex().right() == null) _tryReduceLevel();
                return node.element();
        }

        /**
         * Possibly reduce head level if it has no nodes. This method can
         * (rarely) make mistakes, in which case levels can disappear even
         * though they are about to contain index nodes. This impacts
         * performance, not correctness. To minimize mistakes as well as to
         * reduce hysteresis, the level is reduced by one only if the topmost
         * three levels look empty. Also, if the removed level looks non-empty
         * after CAS, we try to change it back quick before anyone notices our
         * mistake! (This trick works pretty well because this method will
         * practically never make mistakes unless current thread stalls
         * immediately before first CAS, in which case it is very unlikely to
         * stall again immediately afterwards, so will recover.) We put up with
         * all this rather than just let levels grow because otherwise, even a
         * small map that has undergone a large number of insertions and
         * removals will have a lot of levels, slowing down access more than
         * would an occasional unwanted reduction.
         */
        private final void _tryReduceLevel() {
                final HeadIndex<K, V> h = _getHeadIndex();
                HeadIndex<K, V> d;
                HeadIndex<K, V> e;
                if (h.level() > 3
                    && (d = (HeadIndex<K, V>) h.down()) != null
                    && (e = (HeadIndex<K, V>) d.down()) != null
                    && e.right() == null
                    && d.right() == null
                    && h.right() == null
                    && _casTopmostHeadIndex(h, d)
                    && h.right() != null)
                        _casTopmostHeadIndex(d, h); // try to backout
        }

        /**
         * Nodes heading each level keep track of their level.
         * @param <S> the generic type
         * @param <T> the generic type
         */
        private final class HeadIndex<S, T> extends Index<S, T> {
                /** The Constant serialVersionUID. */
                private static final long serialVersionUID = 3547797323824693918L;
                /** The level. */
                private final int _level;

                /**
                 * Instantiates a new head index.
                 * @param down the down
                 * @param right the right
                 */
                private HeadIndex(final HeadIndex<S, T> down, final Index<S, T> right) {
                        this(down, right, down.level() + 1);
                }

                /**
                 * Instantiates a new head index.
                 * @param down the down
                 * @param right the right
                 * @param level the level
                 */
                private HeadIndex(final Index<S, T> down, final Index<S, T> right, final int level) {
                        this(down.node(), down, right, level);
                }

                /**
                 * Instantiates a new head index.
                 * @param node the node
                 * @param down the down
                 * @param right the right
                 * @param level the level
                 */
                private HeadIndex(final Node<T> node, final Index<S, T> down, final Index<S, T> right,
                    final int level) {
                        super(null, node, down, right);
                        this._level = level;
                }

                /**
                 * Level.
                 * @return the int
                 */
                final int level() {
                        return _level;
                }
        }

        /**
         * Index nodes represent the levels of the skip list. Note that even
         * though both Nodes and Indexes have forward-pointing fields, they have
         * different types and are handled in different ways, that can't nicely
         * be captured by placing field in a shared abstract class.
         * @param <I> the generic type
         * @param <J> the generic type
         */
        class Index<I, J> extends AtomicReference<Index<I, J>> {
                /** The Constant serialVersionUID. */
                private static final long serialVersionUID = 6717821668181651376L;
                /** The _node. */
                private final Node<J> _node;
                /** The _down. */
                private final Index<I, J> _down;
                /** The _key. */
                private final I _key;

                /**
                 * Instantiates a new index.
                 * @param key the key
                 * @param down the down
                 * @param right the right
                 */
                private Index(final I key, final Index<I, J> down, final Index<I, J> right) {
                        this(key, down.node(), down, right);
                }

                /**
                 * Instantiates a new index.
                 * @param key the key
                 * @param node the node
                 * @param down the down
                 * @param right the right
                 */
                Index(final I key, final Node<J> node, final Index<I, J> down, final Index<I, J> right) {
                        super(right);
                        _key = key;
                        this._node = node;
                        this._down = down;
                }

                /**
                 * Down.
                 * @return the index
                 */
                final Index<I, J> down() {
                        return _down;
                }

                /**
                 * Node.
                 * @return the node
                 */
                final Node<J> node() {
                        return _node;
                }

                /**
                 * Right.
                 * @return the index
                 */
                final Index<I, J> right() {
                        return get();
                }

                /**
                 * Returns true if the node this indexes has been deleted.
                 * @return true if indexed node is known to be deleted
                 */
                final boolean indexesDeletedNode() {
                        if (_node == null) return true;
                        return _node.isDeleted();
                }

                /**
                 * Key.
                 * @return the i
                 */
                I key() {
                        return _key;
                }

                /**
                 * Value.
                 * @return the j
                 */
                final J value() {
                        return _node.element();
                }

                /**
                 * Link.
                 * @param succ the succ
                 * @param newSucc the new succ
                 * @return true, if successful
                 */
                final boolean link(final Index<I, J> succ, final Index<I, J> newSucc) {
                        newSucc._setRight(succ);
                        return !indexesDeletedNode() && casRight(succ, newSucc);
                }

                /**
                 * Unlink.
                 * @param succ the succ
                 * @return true, if successful
                 */
                final boolean unlink(final Index<I, J> succ) {
                        if (!indexesDeletedNode() && casRight(succ, succ.right())) {
                                succ._setRight(this);//help GC
                                return true;
                        }
                        return false;
                }

                /**
                 * Cas right.
                 * @param cmp the cmp
                 * @param val the val
                 * @return true, if successful
                 */
                final boolean casRight(final Index<I, J> cmp, final Index<I, J> val) {
                        return compareAndSet(cmp, val);
                }

                /**
                 * _set right.
                 * @param idx the idx
                 */
                private final void _setRight(final Index<I, J> idx) {
                        set(idx);
                }
        }

        /**
         * The Class Itr.
         * @param <T> the generic type
         */
        private abstract class SolidItr<T> implements Iterator<T> {
                /** The last returned. */
                Index<K, V> _lastReturned;
                /** The next. */
                Index<K, V> _next;

                /**
                 * Instantiates a new itr.
                 */
                private SolidItr() {
                        _next = _findFIrst();
                }

                /**
                 * Advance.
                 */
                final void advance() {
                        if (_next == null) throw new NoSuchElementException();
                        _lastReturned = _next;
                        do {
                                _next = _next.right();
                        } while (_next != null && _next.indexesDeletedNode());
                }

                /**
                 * Checks for next.
                 * @return true, if successful
                 */
                @Override
                public final boolean hasNext() {
                        return _next != null && !_next.indexesDeletedNode();
                }

                /**
                 * Removes the.
                 */
                @Override
                public final void remove() {
                        final Index<K, V> l = _lastReturned;
                        if (l == null || l.indexesDeletedNode()) throw new IllegalStateException();
                        _deleteNode(l);
                }
        }

        private final class IndexIter extends SolidItr<Index<K, V>> {
                /** @InheritDoc */
                @Override
                public Index<K, V> next() {
                        Index<K, V> next;
                        do {
                                next = _next;
                                advance();
                        } while (next.indexesDeletedNode());
                        return next;
                }
        }

        /**
         * The Class KeyIter.
         */
        private final class KeyIter extends SolidItr<K> {
                /**
                 * Next.
                 * @return the k
                 */
                @Override
                public final K next() {
                        Index<K, V> next;
                        do {
                                next = _next;
                                advance();
                        } while (next.indexesDeletedNode());
                        return next.key();
                }
        }

        /**
         * The Class ValueIter.
         */
        private final class ValueIter extends SolidItr<V> {
                /**
                 * Next.
                 * @return the v
                 */
                @Override
                public final V next() {
                        Index<K, V> next;
                        do {
                                next = _next;
                                advance();
                        } while (next.indexesDeletedNode());
                        return next.value();
                }
        }

        /**
         * The Class EntryIter.
         */
        private final class EntryIter extends SolidItr<Entry<K, V>> {
                /**
                 * Next.
                 * @return the entry
                 */
                @Override
                public final Entry<K, V> next() {
                        Index<K, V> next;
                        do {
                                next = _next;
                                advance();
                        } while (next.indexesDeletedNode());
                        return new AbstractMap.SimpleEntry<K, V>(next.key(), next.value());
                }
        }
}
