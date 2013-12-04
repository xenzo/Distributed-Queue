package com.tmax.collection;


import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Iterator;


/**
 * The Class TwoPhaseDequeSupport.
 * @param <K> the key type
 * @param <V> the value type
 */
public class TwoPhaseDequeSupport<K, V> extends SkipListSupport<K, V> implements Externalizable {
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 5878705947077241457L;
    /** The capacity. */
    private final transient long _capacity;

    /**
     * Instantiates a new two phase deque support.
     */
    protected TwoPhaseDequeSupport() {
        this(Long.MAX_VALUE);
    }

    /**
     * Instantiates a new two phase deque support.
     * @param capacity the capacity
     */
    protected TwoPhaseDequeSupport(final long capacity) {
        _capacity = capacity;
    }

    /**
     * Checks if is full.
     * @return true, if is full
     */
    protected final boolean isFull() {
        return realCount() >= _capacity;
    }

    /**
     * Checks if is empty.
     * @return true, if is empty
     */
    protected final boolean isEmpty() {
        return !(realCount() > 0L);
    }

    /**
     * Do offer first.
     * @param value the value
     * @return true, if successful
     */
    protected final boolean doOfferFirst(final K key, final V value) {
        Node<V> node;
        if ((node = putNode(key, value)) == null) return false; //already exists
        return linkFirst(node);
    }

    /**
     * Do offer last.
     * @param value the value
     * @return true, if successful
     */
    protected final boolean doOfferLast(final K key, final V value) {
        Node<V> node;
        if ((node = putNode(key, value)) == null) return false; //already exists
        return linkLast(node);
    }

    /**
     * Do poll first.
     * @return the value
     */
    protected final V doPollFirst() {
        return unlinkFirst();
    }

    /**
     * Do poll last.
     * @return the value
     */
    protected final V doPollLast() {
        return unlinkLast();
    }

    /**
     * Do remove solidily.
     * @param okey the okey
     * @return the value
     */
    protected final V doRemoveSolidily(final K okey) {
        return doRemove(okey);
    }

    /**
     * Gets value for key using findNode.
     * @param okey the key
     * @return the value, or null if absent
     */
    protected final V doGetSolidly(final K okey) {
        return doGet(okey);
    }

    /** @InheritDoc */
    @Override public void writeExternal(final ObjectOutput out) throws IOException {
        for (Iterator<Index<K, V>> iter = indexIterator(); iter.hasNext();) {
            Index<K, V> next = iter.next();
            out.writeObject(next.key());
            out.writeObject(next.value());
            out.writeBoolean(next.node().isReal());
        }
    }

    /** @InheritDoc */
    @SuppressWarnings("unchecked") @Override public void readExternal(final ObjectInput in)
            throws IOException, ClassNotFoundException {
        K key;
        V value;
        while ((key = (K) in.readObject()) != null) {
            value = (V) in.readObject();
            boolean isReal = in.readBoolean();
            Node<V> node = putNode(key, value);
            if (node != null) {
                if (!isReal) node.delete();
                linkFirst(node);
            }
        }
    }
}
