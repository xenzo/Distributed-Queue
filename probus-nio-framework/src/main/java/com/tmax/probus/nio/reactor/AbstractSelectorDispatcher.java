/*
 * AbstractSelectorDispatcher.java Version 1.0 May 22, 2012
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


import static java.util.logging.Level.FINER;
import static java.util.logging.Level.FINEST;
import static java.util.logging.Level.SEVERE;
import static java.util.logging.Level.WARNING;

import java.io.IOException;
import java.net.Socket;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.ClosedSelectorException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

import com.tmax.probus.nio.api.ILifeCycle;


/**
 * The Class AbstractSelectorDispatcher.
 * <p/>
 * <p/>
 * 
 * <pre>
 * run
 * |-init
 * |-dispatchLoop(반복)
 * | |-beforeChangeRequest
 * | |-processChangeRequest
 * | |-afterChangeRequest
 * | |-dispatch
 * |-destroy
 * </pre>
 */
public abstract class AbstractSelectorDispatcher implements Runnable, ILifeCycle {
    /** Logger for this class. */
    private final transient Logger logger = Logger.getLogger("com.tmax.probus.nio.reactor");
    /** The selector_. */
    private Selector _selector = null;
    /** The change queue_. */
    private Queue<ChangeRequest> _changeQueue = null;
    private Queue<Runnable> _pendingQueue = null;
    /** The is continue_. */
    private volatile boolean _isContinue = false;
    /** The name_. */
    private final String _id;
    /** The executor_. */
    private final ExecutorService _executor;
    /** The dispatcher thread_. */
    private Thread _dispatchThread;
    /** The is running_. */
    private volatile boolean _isRunning = false;
    /** The error count_. */
    private int _selectorErrorCount = 0;
    private int _selectorFailLimit = 10;
    private long _selectorTimeout = 500L;

    /**
     * Instantiates a new selector dispatcher.
     * @param id the name
     * @param dispatchExecutor the dispatch executor
     */
    protected AbstractSelectorDispatcher(final String id, final ExecutorService dispatchExecutor) {
        _id = id;
        _executor = dispatchExecutor;
    }

    public int keyCount() {
        return selector().keys().size();
    }

    public boolean isRunning() {
        return _isRunning;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return _id;
    }

    /**
     * Gets the selector fail limit.
     * @return the selector fail limit
     */
    private final int getSelectorFailLimit() {
        return _selectorFailLimit;
    }

    protected void setSelectorFailLimit(int limit) {
        _selectorFailLimit = limit;
    }

    /**
     * Gets the selector time out.
     * @return the selector time out
     */
    private final long getSelectorTimeOut() {
        return _selectorTimeout;
    }

    protected void setSelectorTimeOut(long timeout) {
        _selectorTimeout = timeout;
    }

    /**
     * Checks if is continue.
     * @return true, if checks if is continue
     */
    private final boolean isContinue() {
        return _isContinue;
    }

    protected final Selector selector() {
        return _selector;
    }

//Add change request
    protected final void addOps(final SelectableChannel channel, final int ops) {
        if (!isRegisted(channel)) register(channel, ops);
        else if (!isRegisted(channel, ops)) _addChangeRequest(new ChangeRequest(ChangeType.ADD_OPS, channel, ops));
    }

    protected final void changeOps(final SelectableChannel channel, final int ops) {
        final SelectionKey key = channel.keyFor(selector());
        if (!isRegisted(channel)) register(channel, ops);
        else if (key.interestOps() != ops) _addChangeRequest(new ChangeRequest(ChangeType.CHANGE_OPS, channel, ops));
    }

    protected final void closeChannel(final SelectableChannel channel) {
        _addChangeRequest(new ChangeRequest(ChangeType.CLOSE_CHANNEL, channel, -1));
    }

    protected final void deregister(final SelectableChannel channel) {
        if (isRegisted(channel)) _addChangeRequest(new ChangeRequest(ChangeType.DEREGISTER, channel, -1));
    }

    protected final void register(final SelectableChannel channel, final int ops) {
        if (!isRegisted(channel)) _addChangeRequest(new ChangeRequest(ChangeType.REGISTER, channel, ops));
        else if (!isRegisted(channel, ops)) addOps(channel, ops);
    }

    protected final void removeOps(final SelectableChannel channel, final int ops) {
        if (isRegisted(channel, ops)) _addChangeRequest(new ChangeRequest(ChangeType.REMOVE_OPS, channel, ops));
    }

    protected boolean isRegisted(final SelectableChannel channel) {
        return channel.keyFor(selector()) == null;
    }

    protected boolean isRegisted(final SelectableChannel channel, final int ops) {
        final SelectionKey key = channel.keyFor(selector());
        if (key != null) {
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
        if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "addChangeRequest(ChangeRequest=" + request + ")", "start");
        _changeQueue.add(request);
        _wakeupSelector();
        if (logger.isLoggable(FINER)) logger.exiting(getClass().getName(), "addChangeRequest(ChangeRequest)", "end");
    }

    protected final void addPendingJob(final Runnable job) {
        _pendingQueue.add(job);
        _wakeupSelector();
    }

//Lifecycle
    /** @InheritDoc */
    public void start() {
        if (!isContinue()) {
            _isContinue = true;
            if (_executor != null) _executor.execute(this);
        }
    }

    /** @InheritDoc */
    public void stop() {
        if (isContinue()) {
            _isContinue = false;
            _wakeupSelector();
        }
    }

    private final void _wakeupSelector() {
        if (_dispatchThread != null && _dispatchThread != Thread.currentThread()) selector().wakeup();
    }

    /** @InheritDoc */
    public void init() {
        if (!isContinue()) throw new IllegalStateException();
        _dispatchThread = Thread.currentThread();
        if (_changeQueue == null) _changeQueue = new LinkedBlockingQueue<ChangeRequest>();
        if (_pendingQueue == null) _pendingQueue = new LinkedBlockingQueue<Runnable>();
        try {
            _selector = Selector.open();
        } catch (final IOException ex) {
            logger.logp(SEVERE, getClass().getName(), "init()", "", ex);
        }
    }

    /** @InheritDoc */
    public void destroy() {
        for (final SelectionKey key : selector().keys()) {
            try {
                key.channel().close();
                key.cancel();
            } catch (final IOException ex) {
                logger.log(WARNING, ex.getMessage(), ex);
            }
        }
        try {
            if (selector() != null) selector().close();
        } catch (final IOException ex) {
            logger.logp(SEVERE, getClass().getName(), "destroy()", ex.getMessage(), ex);
        }
        if (_changeQueue != null) {
            final Queue<ChangeRequest> queue = _changeQueue;
            _changeQueue = null;
            queue.clear();
        }
        if (_pendingQueue != null) {
            final Queue<Runnable> queue = _pendingQueue;
            _pendingQueue = null;
            queue.clear();
        }
    }

    /** @InheritDoc */
    public void run() {
        if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "run()", "start");
        _isRunning = true;
        try {
            init();
            dispatchLoop();
            destroy();
        } finally {
            _isRunning = false;
        }
        if (logger.isLoggable(FINER)) logger.exiting(getClass().getName(), "run()", "end");
    }

    /** Dispatch loop. */
    private final void dispatchLoop() {
        if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "dispatchLoop()", "start");
        while (isContinue())
            try {
                if (getSelectorFailLimit() > 0 && _selectorErrorCount > getSelectorFailLimit() && replaceSelector())
                    _selectorErrorCount = 0;
                processPendingJobs();
                processChangeRequest();
                dispatch();
            } catch (final ClosedSelectorException e) {
                logger.logp(SEVERE, getClass().getName(), "dispatchLoop()", e.getMessage(), e);
                if (!replaceSelector()) {
                    _selectorErrorCount = 0;
                    break;
                }
            } catch (final Throwable t) {
                logger.logp(SEVERE, getClass().getName(), "dispatchLoop()", "", t);
                _selectorErrorCount++;
            }
        if (logger.isLoggable(FINER)) logger.exiting(getClass().getName(), "dispatchLoop()", "end");
    }

    protected final void processPendingJobs() {
        Runnable runnable = null;
        while ((runnable = _pendingQueue.poll()) != null)
            runnable.run();
    }

    /** Process change request. */
    private final void processChangeRequest() {
        ChangeRequest request = null;
        while ((request = _changeQueue.poll()) != null) {
            final SelectionKey key = request.channel.keyFor(selector());
            if (logger.isLoggable(FINEST)) logger.logp(FINEST, getClass().getName(), "processChangeRequest()",
                "change request(" + request + "), selection key(" + key + ")");
            switch (request.type) {
            case ADD_OPS:
                if (key != null && key.isValid()) key.interestOps(key.interestOps() | (request.channel.validOps() & request.ops));
                break;
            case CHANGE_OPS:
                if (key != null && key.isValid()) key.interestOps(request.channel.validOps() & request.ops);
                break;
            case REMOVE_OPS:
                if (key != null && key.isValid())
                    key.interestOps(key.interestOps() ^ (key.interestOps() & request.ops));
                break;
            case REGISTER:
                try {
                    request.channel.register(selector(), request.ops);
                } catch (final ClosedChannelException ex) {
                    logger.logp(WARNING, getClass().getName(), "processChangeRequest()", "REGESTER:" + ex.getMessage(), ex);
                }
                break;
            case CLOSE_CHANNEL:
                if (key != null) {
                    terminateChannel(request.channel);
                }
                //break;
            case DEREGISTER:
                if (key != null) {
                    cancelKey(key);
                }
                break;
            }
        }
    }

    /**
     * Close channel.
     * @param request the request
     */
    private final void terminateChannel(final SelectableChannel channel) {
        try {
            channel.close();
            onConnectionClosed(channel);
        } catch (final IOException ex) {
            logger.logp(WARNING, getClass().getName(), "processChangeRequest()", "CLOSE_CHANNEL" + ex.getMessage(), ex);
        }
    }

    /**
     * Cancel key.
     * @param key the key
     */
    private final void cancelKey(final SelectionKey key) {
        if (key.isValid()) key.interestOps(0);
        key.attach(null);
        key.cancel();
    }

    /** Select one. */
    private final void dispatch() {
        if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "dispatch()", "start");
        try {
            final int nKeys = selector().select(getSelectorTimeOut());
            if (nKeys < 1) return;
            final Iterator<SelectionKey> selectedKeys = selector().selectedKeys().iterator();
            while (selectedKeys.hasNext()) {
                final SelectionKey key = selectedKeys.next();
                selectedKeys.remove();
                final SelectableChannel channel = processSelectedKey(key);
                afterEveryDispatch(channel, key.readyOps());
            }
        } catch (final IOException ex) {
            logger.logp(SEVERE, getClass().getName(), "dispatch()", ex.getMessage(), ex);
            _selectorErrorCount++;
        }
        if (logger.isLoggable(FINER)) logger.exiting(getClass().getName(), "dispatch()", "end");
    }

    /**
     * After every dispatch.
     * @param channel the channel
     * @param readyOps the ready ops
     */
    protected void afterEveryDispatch(final SelectableChannel channel, final int readyOps) {
    }

    /**
     * Process selected key.
     * @param aKey the key
     * @return the selectable channel
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private SelectableChannel processSelectedKey(final SelectionKey aKey) throws IOException {
        SelectableChannel channel = aKey.channel();
        try {
            if (!aKey.isValid()) return null;
            if (aKey.isAcceptable()) channel = handleAccept(aKey);
            if (aKey.isConnectable()) handleConnect(aKey);
            if (aKey.isReadable()) handleRead(aKey);
            if (aKey.isWritable()) handleWrite(aKey);
        } catch (final CancelledKeyException cke) {
            logger.logp(WARNING, getClass().getName(), "processSelectedKey()", cke.getMessage(), cke);
            closeChannel(aKey.channel());
        }
        return channel;
    }

//Operation
    protected SelectableChannel handleAccept(final SelectionKey key) throws IOException {
        final SocketChannel channel = _accept(key);
        if (isConnected(channel)) {
            initSocket(channel.socket());
            onConnectionConnected(channel);
            handOffAfterAccept(channel);
        }
        return channel;
    }

    /**
     * Accept.
     * @param key the key
     * @return the socket channel
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private final SocketChannel _accept(final SelectionKey key) throws IOException {
        final ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
        final SocketChannel channel = serverChannel.accept();
        channel.configureBlocking(false);
        return channel;
    }

    /**
     * Inits the socket.
     * @param socket the socket
     */
    abstract protected void initSocket(final Socket socket);

    /**
     * Hand off after accept.
     * @param channel the channel
     */
    abstract protected void handOffAfterAccept(SocketChannel channel);

    protected SelectableChannel handleConnect(final SelectionKey key) throws IOException {
        final SocketChannel channel = (SocketChannel) key.channel();
        if (isConnected(channel)) {
            removeOps(channel, SelectionKey.OP_CONNECT);
            initSocket(channel.socket());
            onConnectionConnected(channel);
            handOffAfterConnect(channel);
        }
        return channel;
    }

    /**
     * Checks if is connected.
     * @param channel the channel
     * @return true, if is connected
     * @throws IOException Signals that an I/O exception has occurred.
     */
    protected final boolean isConnected(final SocketChannel channel) throws IOException {
        return channel != null && channel.finishConnect();
    }

    /**
     * Hand off after connect.
     * @param channel the channel
     */
    abstract protected void handOffAfterConnect(SocketChannel channel);

    protected SelectableChannel handleRead(final SelectionKey key) throws IOException {
        final SocketChannel channel = (SocketChannel) key.channel();
        byte[] msg = null;
        if ((msg = readMessage(channel)) != null) {
            removeOps(channel, SelectionKey.OP_READ);
            onMessageReceived(channel, msg);
            handOffAfterRead(channel);
        }
        return channel;
    }

    /**
     * channel로 부터 데이터를 읽는다.
     * @param channel the channel
     * @return 완결된 하나의 메세지를 다 읽게 되면 true를 반환
     * @throws IOException the IO exception
     */
    abstract protected byte[] readMessage(final SocketChannel channel) throws IOException;

    /**
     * Hand off after read.
     * @param channel the channel
     */
    abstract protected void handOffAfterRead(SocketChannel channel);

    protected SelectableChannel handleWrite(final SelectionKey key) throws IOException {
        final SocketChannel channel = (SocketChannel) key.channel();
        if (sendMessage(channel)) {
            removeOps(channel, SelectionKey.OP_WRITE);
            handOffAfterWrite(channel);
        }
        return channel;
    }

    /**
     * channel에 메세지들을 전송한다.
     * @param channel the channel
     * @return 더 이상 보낼 메세지가 없으면 true를 반환
     * @throws IOException the IO exception
     */
    abstract protected boolean sendMessage(final SocketChannel channel) throws IOException;

    /**
     * Hand off after write.
     * @param channel the channel
     */
    abstract protected void handOffAfterWrite(SocketChannel channel);

    /**
     * Replace selector.
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private boolean replaceSelector() {
        if (logger.isLoggable(FINER)) logger.entering("AbstractSelectorDispatcher", "replaceSelector()", "start");
        try {
            final Selector oldSelector = selector();
            Selector newSelector = Selector.open();
            for (final SelectionKey key : oldSelector.keys()) {
                if (key.isValid()) key.channel().register(newSelector, key.interestOps(), key.attachment());
                key.cancel();
            }
            _selector = newSelector;
            logger.logp(WARNING, getClass().getName(), "replaceSelector", "Selector replaced successfully.", newSelector);
            oldSelector.close();
        } catch (IOException ex) {
            logger.log(WARNING, ex.getMessage(), ex);
            return false;
        }
        if (logger.isLoggable(FINER)) logger.exiting("AbstractSelectorDispatcher", "replaceSelector()", "end");
        return true;
    }

    /**
     * On connection closed.
     * @param channel the channel
     */
    protected void onConnectionClosed(SelectableChannel channel) {
    }

    /**
     * On connection connected.
     * @param channel the channel
     */
    protected void onConnectionConnected(final SelectableChannel channel) {
    }

    /**
     * On message received.
     * @param channel the channel
     * @param msg the msg
     */
    protected void onMessageReceived(final SocketChannel channel, final byte[] msg) {
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
        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder();
            sb.append("type=" + type);
            if (ops > 0) sb.append(", ops=").append(ops);
            if (channel != null) sb.append(", channel=").append(channel);
            return sb.toString();
        }
    }//end ChangeRequest

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
    }//end ChangeType
}
