/*
 * DqSelectorBase.java Version 1.0 Jan 26, 2012
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

import java.io.IOException;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.ClosedSelectorException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

import com.tmaxsoft.nio.api.IReactor;
import com.tmaxsoft.nio.api.ISelector;

/**
 * The Class DqSelectorBase.
 */
abstract class AbstractReactor implements IReactor {
        /** Logger for this class. */
        final transient Logger logger = Logger.getLogger("com.tmaxsoft.nio");
// Operation

        /**
         * On connection closed.
         * @param channel the channel
         */
        protected void onConnectionClosed(final SelectableChannel channel) {
        }

        /**
         * Handle accept.
         * @param selector the selector
         * @param channel the channel
         * @throws IOException Signals that an I/O exception has occurred.
         */
        abstract protected void handleAccept(ISelector selector, ServerSocketChannel channel) throws IOException;

        /**
         * Handle connect.
         * @param selector the selector
         * @param channel the channel
         * @throws IOException Signals that an I/O exception has occurred.
         */
        abstract protected void handleConnect(ISelector selector, SelectableChannel channel) throws IOException;

        /**
         * Handle read.
         * @param selector the selector
         * @param channel the channel
         * @throws IOException Signals that an I/O exception has occurred.
         */
        abstract protected void handleRead(ISelector selector, SelectableChannel channel) throws IOException;

        /**
         * Handle write.
         * @param selector the selector
         * @param channel the channel
         * @throws IOException Signals that an I/O exception has occurred.
         */
        abstract protected void handleWrite(ISelector selector, SelectableChannel channel) throws IOException;

        /**
         * The Class AbstractSelectorDispatcher.
         * <p/>
         * <pre>
         * run
         * |-init
         * |-dispatchLoop(반복)
         * | |-processChangeRequest
         * | |-dispatch
         * |-destroy
         * </pre>
         */
        abstract class AbstractSelectDispatcher implements Runnable, ISelector {
                /** The selector_. */
                private Selector _selector = null;
                /** The change queue_. */
                private Queue<ChangeRequest> _changeQueue = null;
                /** The is continue_. */
                private volatile boolean _isContinue = false;
                /** The executor_. */
                private final ExecutorService _executor;
                /** The dispatcher thread_. */
                private Thread _dispatchThread;
                /** The is running_. */
                private volatile boolean _isRunning = false;
                /** The error count_. */
                private int _errorCount = 0;

                /**
                 * Instantiates a new selector dispatcher.
                 * @param dispatchExecutor the dispatch executor
                 */
                protected AbstractSelectDispatcher(final ExecutorService dispatchExecutor) {
                        if (dispatchExecutor == null) throw new NullPointerException();
                        _executor = dispatchExecutor;
                }

                /** @InheritDoc */
                @Override public int keyCount() {
                        return selector().keys().size();
                }

                /** @InheritDoc */
                @Override public boolean isRunning() {
                        return _isRunning;
                }

                /**
                 * Checks if is continue.
                 * @return true, if checks if is continue
                 */
                private final boolean _isContinue() {
                        return _isContinue;
                }

                /**
                 * Selector.
                 * @return the selector
                 */
                final Selector selector() {
                        return _selector;
                }

                /**
                 * Gets the selector fail limit.
                 * @return the selector fail limit
                 */
                abstract protected int getSelectorFailLimit();

                /**
                 * Gets the selector time out.
                 * @return the selector time out
                 */
                abstract protected long getSelectorTimeOut();
// Add change request

                /**
                 * Adds the ops.
                 * @param channel the channel
                 * @param ops the ops
                 */
                final void addOps(final SelectableChannel channel, final int ops) {
                        _addChangeRequest(new ChangeRequest(ChangeType.ADD_OPS, channel, ops));
                }

                /**
                 * Change ops.
                 * @param channel the channel
                 * @param ops the ops
                 */
                final void changeOps(final SelectableChannel channel, final int ops) {
                        _addChangeRequest(new ChangeRequest(ChangeType.CHANGE_OPS, channel, ops));
                }

                /**
                 * Close channel.
                 * @param channel the channel
                 */
                final void closeChannel(final SelectableChannel channel) {
                        _addChangeRequest(new ChangeRequest(ChangeType.CLOSE_CHANNEL, channel, -1));
                }

                /**
                 * Deregister channel.
                 * @param channel the channel
                 */
                final void deregisterChannel(final SelectableChannel channel) {
                        _addChangeRequest(new ChangeRequest(ChangeType.DEREGISTER, channel, -1));
                }

                /**
                 * Register.
                 * @param channel the channel
                 * @param ops the ops
                 */
                final void registerChannel(final SelectableChannel channel, final int ops) {
                        _addChangeRequest(new ChangeRequest(ChangeType.REGISTER, channel, ops));
                }

                /**
                 * Removes the ops.
                 * @param channel the channel
                 * @param ops the ops
                 */
                final void removeOps(final SelectableChannel channel, final int ops) {
                        _addChangeRequest(new ChangeRequest(ChangeType.REMOVE_OPS, channel, ops));
                }

                /** @InheritDoc */
                @Override public boolean isRegistered(final SelectableChannel channel) {
                        return channel.keyFor(selector()) != null;
                }

                /** @InheritDoc */
                @Override public boolean isRegisteredFor(final SelectableChannel channel, final int ops) {
                        final SelectionKey key = channel.keyFor(selector());
                        if (isRegistered(channel)) {
                                final int interestOps = key.interestOps();
                                return (interestOps & ops) == ops;
                        }
                        return false;
                }

                /**
                 * Adds the change request.
                 * @param request the request
                 */
                private final void _addChangeRequest(final ChangeRequest request) {
                        if (logger.isLoggable(FINER))
                                logger.entering(getClass().getName(), "addChangeRequest(ChangeRequest=" + request + ")",
                                    "start");
                        _changeQueue.add(request);
                        _wakeupSelector();
                        if (logger.isLoggable(FINER))
                                logger.exiting(getClass().getName(), "addChangeRequest(ChangeRequest)", "end");
                }
// Lifecycle

                /** @InheritDoc */
                @Override public void start() {
                        if (!isRunning()) {
                                _isContinue = true;
                                if (_executor != null) _executor.execute(this);
                        }
                }

                /** @InheritDoc */
                @Override public void stop() {
                        if (isRunning()) {
                                _isContinue = false;
                                _wakeupSelector();
                        }
                }

                /**
                 * _wakeup selector.
                 */
                private final void _wakeupSelector() {
                        if (_dispatchThread != null && _dispatchThread != Thread.currentThread()) selector().wakeup();
                }

                /** @InheritDoc */
                @Override public void init() {
                        if (!_isContinue()) throw new IllegalStateException();
                        _dispatchThread = Thread.currentThread();
                        if (_changeQueue == null) _changeQueue = new LinkedBlockingQueue<ChangeRequest>();
                        try {
                                _selector = Selector.open();
                        } catch (final IOException ex) {
                                logger.logp(SEVERE, getClass().getName(), "init()", ex.getMessage(), ex);
                        }
                }

                /** @InheritDoc */
                @Override public void destroy() {
                        for (final SelectionKey key : selector().keys()) {
                                _terminateChannel(key.channel());
                                _cancelKey(key);
                        }
                        try {
                                if (selector() != null) selector().close();
                        } catch (final IOException ex) {
                                logger.logp(SEVERE, getClass().getName(), "destroy()", ex.getMessage(), ex);
                        }
                        if (_changeQueue != null) {
                                final Queue<ChangeRequest> queue = _changeQueue;
                                _changeQueue = null;
                                logger.logp(WARNING, getClass().getName(), "destroy",
                                    "not processed request remanins : " + queue.size(), queue);
                                queue.clear();
                        }
                }

                /** @InheritDoc */
                @Override public void run() {
                        if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "run()", "start");
                        _isRunning = true;
                        try {
                                init();
                                _dispatchLoop();
                                destroy();
                        } finally {
                                _isRunning = false;
                        }
                        if (logger.isLoggable(FINER)) logger.exiting(getClass().getName(), "run()", "end");
                }

                /** Dispatch loop. */
                private final void _dispatchLoop() {
                        if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "dispatchLoop()", "start");
                        while (_isContinue()) {
                                try {
                                        if (getSelectorFailLimit() > 0
                                            && _errorCount > getSelectorFailLimit()
                                            && _replaceSelector()) _errorCount = 0;
                                        _dispatch();
                                        _processChangeRequest();// selector종료시 처리할 것은 다 처리해야하므로 dispatch후에 요청처리
                                } catch (final ClosedSelectorException e) {
                                        logger.logp(SEVERE, getClass().getName(), "dispatchLoop()", e.getMessage(), e);
                                        if (!_replaceSelector()) {
                                                _errorCount = 0;
                                                break;
                                        }
                                } catch (final Throwable t) {
                                        logger.logp(SEVERE, getClass().getName(), "dispatchLoop()", t.getMessage(), t);
                                        _errorCount++;
                                }
                        }
                        if (logger.isLoggable(FINER)) logger.exiting(getClass().getName(), "dispatchLoop()", "end");
                }

                /** Process change request. */
                private final void _processChangeRequest() {
                        ChangeRequest req = null;
                        while ((req = _changeQueue.poll()) != null) {
                                final SelectionKey key = req.channel.keyFor(selector());
                                if (logger.isLoggable(FINEST))
                                        logger.logp(FINEST, getClass().getName(), "processChangeRequest()",
                                            "change request(" + req + "), selection key(" + key + ")");
                                switch (req.type) {
                                case ADD_OPS:
                                        if (key != null && key.isValid())
                                                key.interestOps(key.interestOps() | (req.channel.validOps() & req.ops));
                                        break;
                                case CHANGE_OPS:
                                        if (key != null && key.isValid())
                                                key.interestOps(req.channel.validOps() & req.ops);
                                        break;
                                case REMOVE_OPS:
                                        if (key != null && key.isValid())
                                                key.interestOps(key.interestOps() ^ (key.interestOps() & req.ops));
                                        break;
                                case REGISTER:
                                        try {
                                                req.channel.register(selector(), req.ops);
                                        } catch (final ClosedChannelException ex) {
                                                logger.logp(WARNING, getClass().getName(), "processChangeRequest()",
                                                    ex.getMessage(), ex);
                                        }
                                        break;
                                case CLOSE_CHANNEL:
                                        if (key != null) _terminateChannel(req.channel);
                                        // break;
                                case DEREGISTER:
                                        if (key != null) _cancelKey(key);
                                        break;
                                }
                        }
                }

                /**
                 * Close channel.
                 * @param request the request
                 */
                private final void _terminateChannel(final SelectableChannel channel) {
                        try {
                                channel.close();
                                onConnectionClosed(channel);
                        } catch (final IOException ex) {
                                logger.logp(WARNING, getClass().getName(), "processChangeRequest()",
                                    "CLOSE_CHANNEL" + ex.getMessage(), ex);
                        }
                }

                /**
                 * Cancel key.
                 * @param key the key
                 */
                private final void _cancelKey(final SelectionKey key) {
                        if (key.isValid()) key.interestOps(0);
                        key.attach(null);
                        key.cancel();
                }

                /**
                 * Select one.
                 */
                private final void _dispatch() throws IOException {
                        if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "dispatch()", "start");
                        final int nKeys = selector().select(getSelectorTimeOut());
                        if (nKeys < 1) return;
                        final Iterator<SelectionKey> selectedKeys = selector().selectedKeys().iterator();
                        while (selectedKeys.hasNext()) {
                                final SelectionKey key = selectedKeys.next();
                                selectedKeys.remove();
                                _processSelectedKey(key);
                        }
                        if (logger.isLoggable(FINER)) logger.exiting(getClass().getName(), "dispatch()", "end");
                }

                /**
                 * Process selected key.
                 * @param aKey the key
                 * @return the selectable channel
                 * @throws IOException Signals that an I/O exception has occurred.
                 */
                private final void _processSelectedKey(final SelectionKey aKey) throws IOException {
                        try {
                                if (!aKey.isValid()) return;
                                if (aKey.isAcceptable()) handleAccept(this, (ServerSocketChannel) aKey.channel());
                                else {
                                        if (aKey.isConnectable()) handleConnect(this, aKey.channel());
                                        if (aKey.isReadable()) handleRead(this, aKey.channel());
                                        if (aKey.isWritable()) handleWrite(this, aKey.channel());
                                }
                        } catch (final CancelledKeyException cke) {
                                logger.logp(WARNING, getClass().getName(), "processSelectedKey()", cke.getMessage(),
                                    cke);
                                closeChannel(aKey.channel());
                        }
                }

                /**
                 * Replace selector.
                 * @throws IOException Signals that an I/O exception has occurred.
                 */
                private final boolean _replaceSelector() {
                        if (logger.isLoggable(FINER))
                                logger.entering("AbstractSelectorDispatcher", "replaceSelector()", "start");
                        final Selector oldSelector = selector();
                        try {
                                final Selector newSelector = Selector.open();
                                for (final SelectionKey key : oldSelector.keys()) {
                                        if (key.isValid()) key.channel().register(newSelector, key.interestOps(),
                                            key.attachment());
                                        key.cancel();
                                }
                                _selector = newSelector;
                                logger.logp(WARNING, getClass().getName(), "replaceSelector",
                                    "Selector replaced successfully.", newSelector);
                        } catch (final IOException ex) {
                                logger.log(WARNING, ex.getMessage(), ex);
                                return false;
                        }
                        if (logger.isLoggable(FINER))
                                logger.exiting("AbstractSelectorDispatcher", "replaceSelector()", "end");
                        return true;
                }
        }

        /** The Class ChangeRequest. */
        private class ChangeRequest {
                /** The type_. */
                private final ChangeType type;
                /** The channel_. */
                private final SelectableChannel channel;
                /** The ops. */
                private final int ops;

                /**
                 * The Constructor.
                 * @param type the type
                 * @param channel the channel
                 * @param ops the ops
                 */
                private ChangeRequest(final ChangeType type, final SelectableChannel channel, final int ops) {
                        this.type = type;
                        this.channel = channel;
                        this.ops = ops;
                }

                /** {@inheritDoc} */
                @Override public String toString() {
                        final StringBuilder sb = new StringBuilder();
                        sb.append("type=" + type);
                        if (ops > 0) sb.append(", ops=").append(ops);
                        if (channel != null) sb.append(", channel=").append(channel);
                        return sb.toString();
                }
        }// end ChangeRequest

        /** The Enum ChangeType. */
        private enum ChangeType {
                /** The register. */
                REGISTER,
                /** The deregister. */
                DEREGISTER,
                /** The add ops. */
                ADD_OPS,
                /** The change ops. */
                CHANGE_OPS,
                /** The remove ops. */
                REMOVE_OPS,
                /** The close channel. */
                CLOSE_CHANNEL;
        }// end ChangeType
}
