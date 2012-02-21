/*
 * AbstractReactor.java Version 1.0 Feb 9, 2012
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
package com.tmax.probus.nio.reactor;


import static java.util.logging.Level.*;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

import com.tmax.probus.nio.api.IReactor;


/**
 * The Class AbstractReactor.
 */
public abstract class AbstractReactor implements IReactor {
    /** The Constant SELECTOR_TIMEOUT(ms). */
    private static final int SELECTOR_TIMEOUT = 500;
    /** Logger for this class. */
    private final transient Logger logger = Logger.getLogger("com.tmax.probus.nio");
    /** The pending jobs_. */
    private Queue<Runnable> pendingJobs_ = new LinkedBlockingQueue<Runnable>();
    /** The pending change request_. */
    private Queue<ChangeRequest> pendingChangeRequest_ = new LinkedBlockingQueue<ChangeRequest>();
    /** The is running_. */
    private volatile boolean isRunning_;

    // (non-Javadoc)
    // @see com.tmax.probus.nio.IReactor#addPendingJob(java.lang.Runnable)
    @Override public void addPendingJob(final Runnable runnable) {
        if (logger.isLoggable(FINER)) logger.entering("AbstractReactor", "addPendingJob(Runnable=" + runnable + ")", "start");
        pendingJobs_.add(runnable);
        if (logger.isLoggable(FINER)) logger.exiting("AbstractReactor", "addPendingJob(Runnable)", "end");
    }

    // (non-Javadoc)
    // @see com.tmax.probus.nio.IReactor#changeOpts(java.nio.channels.SelectableChannel, int)
    @Override public void changeOpts(final SelectableChannel channel, final int opts) {
        if (logger.isLoggable(FINER)) logger.entering("AbstractReactor", "changeOpts(SelectableChannel=" + channel + ", int=" + opts + ")", "start");
        addChangeRequest(new ChangeRequest(ChangeRequestType.CHANGE_OPTS, channel, opts));
        if (logger.isLoggable(FINER)) logger.exiting("AbstractReactor", "changeOpts(SelectableChannel, int)", "end");
    }

    // (non-Javadoc)
    // @see com.tmax.probus.nio.IReactor#deregister(java.nio.channels.SelectableChannel)
    @Override public void deregister(final SelectableChannel channel) {
        if (logger.isLoggable(FINER)) logger.entering("AbstractReactor", "deregister(SelectableChannel=" + channel + ")", "start");
        addChangeRequest(new ChangeRequest(ChangeRequestType.DEREGISTER, channel, 0));
        if (logger.isLoggable(FINER)) logger.exiting("AbstractReactor", "deregister(SelectableChannel)", "end");
    }

    // (non-Javadoc)
    // @see com.tmax.probus.nio.IReactor#getIoReactor()
    /**
     * Gets the io reactor.
     * @return the io reactor
     */
    public IReactor getIoReactor() {
        return this;
    }

    // (non-Javadoc)
    // @see com.tmax.probus.nio.IReactor#register(java.nio.channels.SocketChannel, int)
    @Override public void register(final SelectableChannel channel, final int op) {
        register(channel, op, null);
    }

    // (non-Javadoc)
    // @see com.tmax.probus.nio.IReactor#register(java.nio.channels.SocketChannel, int, java.lang.Object)
    @Override public void register(final SelectableChannel channel, final int op, final Object attachment) {
        if (logger.isLoggable(FINER)) logger.entering("AbstractReactor", "register(SelectableChannel=" + channel + ", int=" + op + ", Object=" + attachment + ")", "start");
        addChangeRequest(new ChangeRequest(ChangeRequestType.REGISTER, channel, op, attachment));
        if (logger.isLoggable(FINER)) logger.exiting("AbstractReactor", "register(SelectableChannel, int, Object)", "end");
    }

    // (non-Javadoc)
    // @see java.lang.Runnable#run()
    @Override public void run() {
        if (logger.isLoggable(FINER)) logger.entering("AbstractReactor", "run()", "start");
        init();
        final Selector selector = getSelector();
        while (isRunning()) {
            if (selector == null) break;
            try {
                processChangeRequest();
                processPendingJobs();
                final int nKeys = selector.select(getSelectorTimeout());
                if (nKeys <= 0) continue;
                final Iterator<SelectionKey> selectedKeys = selector.selectedKeys().iterator();
                while (selectedKeys.hasNext()) {
                    final SelectionKey key = selectedKeys.next();
                    selectedKeys.remove();
                    if (!key.isValid()) continue;
                    if (key.isAcceptable()) handleAccept(key);
                    if (key.isConnectable()) handleConnect(key);
                    if (key.isReadable()) handleRead(key);
                    if (key.isWritable()) handleWrite(key);
                }
            } catch (final Exception ex) {
                logger.logp(SEVERE, "AbstractReactor", "run()", "", ex);
            }
        }
        destroy();
        if (logger.isLoggable(FINER)) logger.exiting("AbstractReactor", "run()", "end");
    }

    // (non-Javadoc)
    // @see com.tmax.probus.nio.IReactor#stop()
    @Override public void stop() {
        if (logger.isLoggable(FINER)) logger.entering("AbstractReactor", "stop()", "start");
        isRunning_ = false;
        wakeup();
        if (logger.isLoggable(FINER)) logger.exiting("AbstractReactor", "stop()", "end");
    }

    // (non-Javadoc)
    // @see com.tmax.probus.nio.IReactor#wakeup()
    @Override public final void wakeup() {
        if (logger.isLoggable(FINER)) logger.entering("AbstractReactor", "wakeup()", "start");
        final Selector selector = getSelector();
        if (selector != null) selector.wakeup();
        if (logger.isLoggable(FINER)) logger.exiting("AbstractReactor", "wakeup()", "end");
    }

    /**
     * Destroy.
     */
    protected void destroy() {
        if (logger.isLoggable(FINER)) logger.entering("AbstractReactor", "destroy()", "start");
        final Queue<ChangeRequest> changeQueue = pendingChangeRequest_;
        pendingChangeRequest_ = null;
        changeQueue.clear();
        final Queue<Runnable> jobQueue = pendingJobs_;
        pendingJobs_ = null;
        jobQueue.clear();
        if (logger.isLoggable(FINER)) logger.exiting("AbstractReactor", "destroy()", "end");
    }

    /**
     * Gets the pending job executor.
     * @return the pending job executor
     */
    protected Executor getPendingJobExecutor() {
        return new Executor() {
            @Override public void execute(final Runnable job) {
                if (logger.isLoggable(FINER)) logger.entering("SequencialExecutor", "execute(Runnable=" + job + ")", "start");
                job.run();
                if (logger.isLoggable(FINER)) logger.exiting("SequencialExecutor", "execute(Runnable)", "end");
            }
        };
    }

    /**
     * Gets the selector.
     * @return the selector
     */
    protected abstract Selector getSelector();

    /**
     * Gets the selector timeout.
     * @return the selector timeout
     */
    protected long getSelectorTimeout() {
        return SELECTOR_TIMEOUT;
    }

    /**
     * Handle accept.
     * @param key the key
     * @return true, if successful
     * @throws IOException
     */
    protected void handleAccept(final SelectionKey key) throws IOException {
        throw new UnsupportedOperationException();
    }

    /**
     * Handle connect.
     * @param key the key
     * @return true, if successful
     * @throws IOException
     */
    protected void handleConnect(final SelectionKey key) throws IOException {
        throw new UnsupportedOperationException();
    }

    /**
     * Handle read.
     * @param key the key
     * @return true, if successful
     * @throws IOException
     */
    protected void handleRead(final SelectionKey key) throws IOException {
        throw new UnsupportedOperationException();
    }

    /**
     * Handle write.
     * @param key the key
     * @return true, if successful
     * @throws IOException
     */
    protected void handleWrite(final SelectionKey key) throws IOException {
        throw new UnsupportedOperationException();
    }

    /**
     * Inits the.
     */
    protected void init() {
        if (logger.isLoggable(FINER)) logger.entering("AbstractReactor", "init()", "start");
        isRunning_ = true;
        if (logger.isLoggable(FINER)) logger.exiting("AbstractReactor", "init()", "end");
    }

    /**
     * Checks if is running.
     * @return true, if is running
     */
    protected boolean isRunning() {
        return isRunning_;
    }

    /**
     * Adds the change request.
     * @param changeRequest the change request
     */
    private final void addChangeRequest(final ChangeRequest changeRequest) {
        if (logger.isLoggable(FINER)) logger.entering("AbstractReactor", "addChangeRequest(ChangeRequest=" + changeRequest + ")", "start");
        pendingChangeRequest_.add(changeRequest);
        wakeup();
        if (logger.isLoggable(FINER)) logger.exiting("AbstractReactor", "addChangeRequest(ChangeRequest)", "end");
    }

    /**
     * Process change request.
     * @throws ClosedChannelException
     */
    private final void processChangeRequest() throws IOException {
        if (logger.isLoggable(FINER)) logger.entering("AbstractReactor", "processChangeRequest()", "start");
        ChangeRequest request = null;
        while ((request = pendingChangeRequest_.poll()) != null) {
            switch (request.type) {
            case CHANGE_OPTS:
                request.channel.keyFor(getSelector()).interestOps(request.opt);
                break;
            case REGISTER:
                request.channel.register(getSelector(), request.opt, request.attachment);
                break;
            case DEREGISTER:
                final SelectionKey key = request.channel.keyFor(getSelector());
                key.attach(null);
                if (key != null) key.cancel();
                break;
            }
        }
        if (logger.isLoggable(FINER)) logger.exiting("AbstractReactor", "processChangeRequest()", "end");
    }

    /**
     * Process pending jobs.
     */
    private final void processPendingJobs() {
        if (logger.isLoggable(FINER)) logger.entering("AbstractReactor", "processPendingJobs()", "start");
        final Executor executor = getPendingJobExecutor();
        Runnable runnable = null;
        while ((runnable = pendingJobs_.poll()) != null)
            executor.execute(runnable);
        if (logger.isLoggable(FINER)) logger.exiting("AbstractReactor", "processPendingJobs()", "end");
    }

    final class ChangeRequest {
        /** The type. */
        final ChangeRequestType type;
        /** The channel. */
        SelectableChannel channel;
        /** The opt. */
        int opt;
        /** The attachment. */
        Object attachment;

        /**
         * Instantiates a new change request.
         * @param type the type
         * @param channel the channel
         * @param opt the opt
         * @param abstractReactor TODO
         */
        ChangeRequest(final ChangeRequestType type, final SelectableChannel channel, final int opt) {
            this(type, channel, opt, null);
        }

        /**
         * Instantiates a new change request.
         * @param type the type
         * @param channel the channel
         * @param opt the op
         * @param attachment the attachment
         * @param abstractReactor TODO
         */
        ChangeRequest(final ChangeRequestType type, final SelectableChannel channel, final int opt, final Object attachment) {
            this.type = type;
            this.channel = channel;
            this.opt = opt;
            this.attachment = attachment;
        }
    }

    enum ChangeRequestType {
        /** The change opts. */
        CHANGE_OPTS,
        /** The REGISTER. */
        REGISTER,
        /** The DEREGISTER. */
        DEREGISTER;
    }
}
