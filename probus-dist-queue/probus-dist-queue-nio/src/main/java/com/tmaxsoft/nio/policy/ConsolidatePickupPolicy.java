/*
 * SequentialPickPolicy.java Version 1.0 May 4, 2013
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
package com.tmaxsoft.nio.policy;


import java.nio.channels.SelectableChannel;

import com.tmaxsoft.nio.api.ISelector;


/**
 * The Class SequentialPickPolicy.
 */
public class ConsolidatePickupPolicy extends AbstractPickupPolicy {
    private final SequencedPicker _picker;

    /**
     * Instantiates a new sequential pick policy.
     * @param selectors the selectors
     */
    public ConsolidatePickupPolicy() {
        _picker = createPicker();
    }

    /** @InheritDoc */
    @Override public ISelector obtainReadSelector() {
        return _picker.getSelector();
    }

    /** @InheritDoc */
    @Override public ISelector obtainWriteSelector() {
        return _picker.getSelector();
    }

    /** @InheritDoc */
    @Override public ISelector obtainAcceptSelector() {
        return _picker.getSelector();
    }

    /** @InheritDoc */
    @Override public ISelector obtainConnectSelector() {
        return _picker.getSelector();
    }

    /** @InheritDoc */
    @Override public void terminate() {
        _picker.stopAndClear();
    }

    /** @InheritDoc */
    @Override public ISelector searchSelectorFor(final SelectableChannel channel, final int op) {
        return _picker.getSelectorFor(channel, op);
    }

    /** @InheritDoc */
    @Override public boolean removeSelector(final ISelector selector) {
        return _picker.removeSelector(selector);
    }

    /** @InheritDoc */
    @Override public void addAcceptSelector(final ISelector selector) {
        _picker.addSelector(selector);
    }

    /** @InheritDoc */
    @Override public void addConnectSelector(final ISelector selector) {
        _picker.addSelector(selector);
    }

    /** @InheritDoc */
    @Override public void addReadSelector(final ISelector selector) {
        _picker.addSelector(selector);
    }

    /** @InheritDoc */
    @Override public void addWriteSelector(final ISelector selector) {
        _picker.addSelector(selector);
    }
}
