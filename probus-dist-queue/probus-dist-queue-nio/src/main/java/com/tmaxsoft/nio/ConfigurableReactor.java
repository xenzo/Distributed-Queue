/*
 * NioTcpReactor.java Version 1.0 May 6, 2013
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
package com.tmaxsoft.nio;


import static java.util.logging.Level.*;

import java.nio.channels.SelectableChannel;
import java.nio.channels.ServerSocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.tmaxsoft.nio.api.IReactorConfig;
import com.tmaxsoft.nio.api.ISelector;
import com.tmaxsoft.nio.api.ISelectorPickupPolicy;
import com.tmaxsoft.nio.api.ISession;
import com.tmaxsoft.nio.policy.ConsolidatePickupPolicy;


/**
 * The Class ConfigurableReactor.
 */
public class ConfigurableReactor extends SessionSupportedReactor {
    private IReactorConfig _config;
    private ISelectorPickupPolicy _pickupPolicy;
    private ExecutorService _dispatchExecutor;
    private boolean _isRunning;

    /**
     * Instantiates a new configurable reactor.
     */
    public ConfigurableReactor() {
    }

    /**
     * Sets the pickup policy.
     * @param pickupPolicy the pickupPolicy to set
     */
    public void setPickupPolicy(ISelectorPickupPolicy pickupPolicy) {
        _pickupPolicy = pickupPolicy;
    }

    /**
     * Gets the pickup policy.
     * @return the pickup policy
     */
    protected ISelectorPickupPolicy getPickupPolicy() {
        return _pickupPolicy;
    }

    /**
     * Sets the config.
     * @param config the config to set
     */
    public void setConfig(IReactorConfig config) {
        _config = config;
    }

    /**
     * Gets the config.
     * @return the config
     */
    protected IReactorConfig getConfig() {
        return _config;
    }

    /**
     * Sets the dispatch executor.
     * @param dispatchExecutor the dispatchExecutor to set
     */
    public void setDispatchExecutor(ExecutorService dispatchExecutor) {
        _dispatchExecutor = dispatchExecutor;
    }

    /**
     * Gets the dispatch executor.
     * @return the dispatchExecutor
     */
    protected ExecutorService getDispatchExecutor() {
        return _dispatchExecutor;
    }

    /** @InheritDoc */
    @Override protected ISession newSession(final ISelector selector, final SelectableChannel channel, final ServerSocketChannel server) {
        return null;
    }

    /** @InheritDoc */
    @Override public ISelector getAcceptSelector() {
        return getPickupPolicy().obtainAcceptSelector();
    }

    /** @InheritDoc */
    @Override public ISelector getConnectSelector() {
        return getPickupPolicy().obtainConnectSelector();
    }

    /** @InheritDoc */
    @Override public ISelector getReadSelector() {
        return getPickupPolicy().obtainReadSelector();
    }

    /** @InheritDoc */
    @Override public ISelector getWriteSelector() {
        return getPickupPolicy().obtainWriteSelector();
    }

    /** @InheritDoc */
    @Override public void destroy() {
        _isRunning = false;
        _pickupPolicy.terminate();
        final ExecutorService executor = _dispatchExecutor;
        _dispatchExecutor = null;
        executor.shutdown();
        try {
            executor.awaitTermination(500, TimeUnit.MILLISECONDS);
        } catch (final InterruptedException ex) {
            logger.log(WARNING, ex.getMessage(), ex);
        } finally {
            if (!executor.isShutdown()) executor.shutdownNow();
        }
    }

    /** @InheritDoc */
    @Override public void init() {
        if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "init");
        if (_pickupPolicy == null) _pickupPolicy = new ConsolidatePickupPolicy();
        if (_dispatchExecutor == null) _dispatchExecutor = Executors.newCachedThreadPool();
        _isRunning = true;
    }

    /** @InheritDoc */
    @Override public void start() {
    }

    /** @InheritDoc */
    @Override public void stop() {
    }

    /** @InheritDoc */
    @Override public boolean isRunning() {
        return _isRunning;
    }
}
