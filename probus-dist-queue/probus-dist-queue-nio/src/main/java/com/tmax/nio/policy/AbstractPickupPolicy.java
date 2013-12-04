/*
 * AbstractPickupPolicy.java Version 1.0 May 9, 2013
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
package com.tmax.nio.policy;


import java.nio.channels.SelectableChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import com.tmax.nio.api.ISelector;
import com.tmax.nio.api.ISelectorPickupPolicy;


// TODO: Auto-generated Javadoc
/**
 * The Class AbstractPickupPolicy.
 */
public abstract class AbstractPickupPolicy implements ISelectorPickupPolicy {
    /** Logger for this class. */
    final transient Logger logger = Logger.getLogger("com.tmax.nio");

    /**
     * Creates the info.
     * @return the info
     */
    protected final SequencedPicker createPicker() {
        return new SequencedPicker();
    }

    /**
     * The Class SequencedPicker.
     */
    protected final class SequencedPicker {
        /** The _selectors. */
        private final List<ISelector> _selectors = new ArrayList<ISelector>();
        /** The _seq. */
        private final AtomicInteger _seq = new AtomicInteger();

        /**
         * Instantiates a new sequenced picker.
         */
        private SequencedPicker() {
        }

        /**
         * return selector sequentially.
         * @return the selector
         */
        protected final ISelector getSelector() {
            final int idx = _seq.incrementAndGet();
            final int size = _selectors.size();
            final int seq = size > 0 ? idx % size : 0;
            if (idx >= Integer.MAX_VALUE) _seq.set(seq);
            return size > 0 ? _selectors.get(seq) : null;
        }

        /**
         * Adds the selector.
         * @param selector the selector
         */
        protected final void addSelector(final ISelector selector) {
            if (!_selectors.contains(selector)) _selectors.add(selector);
        }

        /**
         * Stop and clear.
         */
        protected final void stopAndClear() {
            for (final ISelector selector : _selectors)
                selector.stop();
            _selectors.clear();
        }

        /**
         * Gets the selector for.
         * @param channel the channel
         * @param op the op
         * @return the selector for
         */
        protected final ISelector getSelectorFor(final SelectableChannel channel, final int op) {
            for (final ISelector selector : _selectors) {
                if (selector.isRegisteredFor(channel, op)) return selector;
            }
            return null;
        }

        /**
         * Removes the selector.
         * @param selector the selector
         * @return true, if successful
         */
        protected final boolean removeSelector(final ISelector selector) {
            return _selectors.remove(selector);
        }
    }//end Info
}//end AbstractPickupPolicy
