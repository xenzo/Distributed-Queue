package com.tmaxsoft.collection;


import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Comparator;
import java.util.Iterator;


/** The Class TwoPhaseDequeSupport.
 * @param <K> the key type
 * @param <V> the value type */
public class TwoPhaseDeletionDequeSupport<K, V> extends SkipListSupport<K, V> implements Externalizable {
        /** The Constant serialVersionUID. */
        private static final long serialVersionUID = 5878705947077241457L;

        /** Instantiates a new two phase deque support.
         * @param capacity the capacity */
        protected TwoPhaseDeletionDequeSupport() {
                this(null);
        }

        protected TwoPhaseDeletionDequeSupport(final Comparator<K> comparator) {
                this(comparator, null);
        }

        protected TwoPhaseDeletionDequeSupport(final Comparator<K> keyComparator, final Comparator<V> valueComparator) {
                super(keyComparator, valueComparator);
        }

        /** Do offer first.
         * @param value the value
         * @return true, if successful */
        protected final boolean doOfferFirst(final K key, final V value) {
                Node<V> node;
                if ((node = putNode(key, value)) == null) return false; //already exists
                while (!linkFirst(node));
                return true;
        }

        /** Do offer last.
         * @param value the value
         * @return true, if successful */
        protected final boolean doOfferLast(final K key, final V value) {
                Node<V> node;
                if ((node = putNode(key, value)) == null) return false; //already exists
                while (!linkLast(node));
                return true;
        }

        /** Do poll first.
         * @return the value */
        protected final V doPollFirst() {
                return unlinkFirst();
        }

        /** Do poll last.
         * @return the value */
        protected final V doPollLast() {
                return unlinkLast();
        }

        /** Do remove solidily.
         * @param okey the okey
         * @return the value */
        protected final V doRemoveSolidily(final K okey) {
                return doRemove(okey);
        }

        /** Gets value for key using findNode.
         * @param okey the key
         * @return the value, or null if absent */
        protected final V doGetSolidly(final K okey) {
                return doGet(okey);
        }

        /** @InheritDoc */
        @Override
        public void writeExternal(final ObjectOutput out) throws IOException {
                for (Iterator<Index<K, V>> iter = indexIterator(); iter.hasNext();) {
                        Index<K, V> next = iter.next();
                        out.writeObject(next.key());
                        out.writeObject(next.value());
                        out.writeBoolean(next.node().isReal());
                }
        }

        /** @InheritDoc */
        @SuppressWarnings("unchecked")
        @Override
        public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
                K key;
                V value;
                while ((key = (K) in.readObject()) != null) {
                        value = (V) in.readObject();
                        boolean isReal = in.readBoolean();
                        Node<V> node = putNode(key, value);
                        if (node != null) {
                                while (!linkFirst(node));
                                if (!isReal) node.delete();
                        }
                }
        }
}
