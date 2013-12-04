/*
 * SeparatePickupPolicy.java Version 1.0 May 8, 2013
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


import static java.util.logging.Level.*;

import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;

import com.tmax.nio.api.ISelector;


/**
 * The Class SeparatePickupPolicy.
 */
public class SeparatePickupPolicy extends AbstractPickupPolicy {
    private final SequencedPicker _acceptPicker, _connectPicker, _readPicker, _writePicker;

    /**
     * Instantiates a new separate pickup policy.
     * @param prefix the prefix
     * @param service the service
     * @param creator the creator
     * @param acceptConfig the accept config
     * @param connectConfig the connect config
     * @param readConfig the read config
     * @param writeConfig the write config
     */
    public SeparatePickupPolicy() {
        _acceptPicker = createPicker();
        _connectPicker = createPicker();
        _readPicker = createPicker();
        _writePicker = createPicker();
    }

    /** @InheritDoc */
    @Override public ISelector obtainAcceptSelector() {
        return _acceptPicker.getSelector();
    }

    /** @InheritDoc */
    @Override public ISelector obtainConnectSelector() {
        return _connectPicker.getSelector();
    }

    /** @InheritDoc */
    @Override public ISelector obtainReadSelector() {
        return _readPicker.getSelector();
    }

    /** @InheritDoc */
    @Override public ISelector obtainWriteSelector() {
        return _writePicker.getSelector();
    }

    /** @InheritDoc */
    @Override public void addAcceptSelector(final ISelector selector) {
        _acceptPicker.addSelector(selector);
    }

    /** @InheritDoc */
    @Override public void addConnectSelector(final ISelector selector) {
        _connectPicker.addSelector(selector);
    }

    /** @InheritDoc */
    @Override public void addReadSelector(final ISelector selector) {
        _readPicker.addSelector(selector);
    }

    /** @InheritDoc */
    @Override public void addWriteSelector(final ISelector selector) {
        _writePicker.addSelector(selector);
    }

    /** @InheritDoc */
    @Override public void terminate() {
        if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "terminate");
        _acceptPicker.stopAndClear();
        _connectPicker.stopAndClear();
        _readPicker.stopAndClear();
        _writePicker.stopAndClear();
        if (logger.isLoggable(FINER)) logger.exiting(getClass().getName(), "terminate");
    }

    /** @InheritDoc */
    @Override public ISelector searchSelectorFor(final SelectableChannel channel, final int op) {
        SequencedPicker picker = null;
        switch (op) {
        case SelectionKey.OP_ACCEPT:
            picker = _acceptPicker;
            break;
        case SelectionKey.OP_CONNECT:
            picker = _connectPicker;
            break;
        case SelectionKey.OP_READ:
            picker = _readPicker;
            break;
        case SelectionKey.OP_WRITE:
            picker = _writePicker;
            break;
        }
        return picker == null ? null : picker.getSelectorFor(channel, op);
    }

    /** @InheritDoc */
    @Override public boolean removeSelector(final ISelector selector) {
        return _acceptPicker.removeSelector(selector)
                | _connectPicker.removeSelector(selector)
                | _readPicker.removeSelector(selector)
                | _writePicker.removeSelector(selector);
    }
}
