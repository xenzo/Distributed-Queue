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


import static java.util.logging.Level.*;

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
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

import com.tmax.probus.nio.api.ISelectorDispatcher;


/**
 * The Class AbstractSelectorDispatcher.
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
public abstract class AbstractSelectorDispatcher implements ISelectorDispatcher {
    /** Logger for this class. */
    private final transient Logger logger = Logger.getLogger("com.tmax.probus.nio.reactor");
    /** The selector_. */
    protected Selector selector_;
    /** The change queue_. */
    private Queue<ChangeRequest> changeQueue_ = null;
    /** The is continue_. */
    private volatile boolean isContinue_ = false;
    /** The name_. */
    private final String name_;
    /** The executor_. */
    private final ExecutorService executor_;
    /** The dispatcher thread_. */
    private Thread dispatcherThread_;
    /** The is running_. */
    private volatile boolean isRunning_ = false;
    /** The error count_. */
    private int selectorErrorCount_ = 0;

    /**
     * Instantiates a new selector dispatcher.
     * @param name the name
     * @param dispatchExecutor the dispatch executor
     */
    protected AbstractSelectorDispatcher(final String name, final ExecutorService dispatchExecutor) {
        name_ = name;
        executor_ = dispatchExecutor;
    }

    /** {@inheritDoc} */
    @Override public void addOps(final SelectableChannel channel, final int ops) {
        if (!isRegisted(channel)) register(channel, ops);
        else if (!isRegisted(channel, ops)) addChangeRequest(new ChangeRequest(ChangeType.ADD_OPS, channel, ops));
    }

    /** {@inheritDoc} */
    @Override public void changeOps(final SelectableChannel channel, final int ops) {
        final SelectionKey key = channel.keyFor(selector_);
        if (!isRegisted(channel)) register(channel, ops);
        else if (key.interestOps() != ops) addChangeRequest(new ChangeRequest(ChangeType.CHANGE_OPS, channel, ops));
    }

    /** {@inheritDoc} */
    @Override public void closeChannel(final SelectableChannel channel) {
        addChangeRequest(new ChangeRequest(ChangeType.CLOSE_CHANNEL, channel, -1));
    }

    /** {@inheritDoc} */
    @Override public void deregister(final SelectableChannel channel) {
        if (isRegisted(channel)) addChangeRequest(new ChangeRequest(ChangeType.DEREGISTER, channel, -1));
    }

    /** {@inheritDoc} */
    @Override public void destroy() {
        try {
            if (selector_ != null) selector_.close();
        } catch (final IOException ex) {
            logger.logp(SEVERE, getClass().getName(), "destroy()", ex.getMessage(), ex);
        }
        if (changeQueue_ != null) {
            final Queue<ChangeRequest> queue = changeQueue_;
            changeQueue_ = null;
            queue.clear();
        }
    }

    /** {@inheritDoc} */
    @Override public SelectableChannel handleAccept(final SelectionKey key) throws IOException {
        final SocketChannel channel = accept(key);
        if (isConnected(channel)) {
            initSocket(channel.socket());
            onConnectionConnected(channel);
            handOffAfterAccept(channel);
        }
        return channel;
    }

    /** {@inheritDoc} */
    @Override public SelectableChannel handleConnect(final SelectionKey key) throws IOException {
        final SocketChannel channel = (SocketChannel) key.channel();
        if (isConnected(channel)) {
            removeOps(channel, SelectionKey.OP_CONNECT);
            initSocket(channel.socket());
            onConnectionConnected(channel);
            handOffAfterConnect(channel);
        }
        return channel;
    }

    /** {@inheritDoc} */
    @Override public SelectableChannel handleRead(final SelectionKey key) throws IOException {
        final SocketChannel channel = (SocketChannel) key.channel();
        byte[] msg = null;
        if ((msg = readMessage(channel)) != null) {
            removeOps(channel, SelectionKey.OP_READ);
            onMessageReceived(channel, msg);
            handOffAfterRead(channel);
        }
        return channel;
    }

    /** {@inheritDoc} */
    @Override public SelectableChannel handleWrite(final SelectionKey key) throws IOException {
        final SocketChannel channel = (SocketChannel) key.channel();
        if (sendMessage(channel)) {
            removeOps(channel, SelectionKey.OP_WRITE);
            handOffAfterWrite(channel);
        }
        return channel;
    }

    /** {@inheritDoc} */
    @Override public void init() {
        if (!isContinue()) throw new IllegalStateException();
        dispatcherThread_ = Thread.currentThread();
        if (changeQueue_ == null) changeQueue_ = new LinkedBlockingQueue<ChangeRequest>();
        try {
            selector_ = Selector.open();
        } catch (final IOException ex) {
            logger.logp(SEVERE, getClass().getName(), "init()", "", ex);
        }
    }

    /** {@inheritDoc} */
    @Override public boolean isRegisted(final SelectableChannel channel) {
        return channel.keyFor(selector_) == null;
    }

    /** {@inheritDoc} */
    @Override public boolean isRegisted(final SelectableChannel channel, final int ops) {
        final SelectionKey key = channel.keyFor(selector_);
        if (key != null) {
            final int interestOps = key.interestOps();
            return (interestOps & ops) == ops;
        }
        return false;
    }

    /** {@inheritDoc} */
    @Override public boolean isRunning() {
        return isRunning_;
    }

    /** {@inheritDoc} */
    @Override public void register(final SelectableChannel channel, final int ops) {
        if (!isRegisted(channel)) addChangeRequest(new ChangeRequest(ChangeType.REGISTER, channel, ops));
        else if (!isRegisted(channel, ops)) addOps(channel, ops);
    }

    /** {@inheritDoc} */
    @Override public void removeOps(final SelectableChannel channel, final int ops) {
        if (isRegisted(channel, ops)) addChangeRequest(new ChangeRequest(ChangeType.REMOVE_OPS, channel, ops));
    }

    /** {@inheritDoc} */
    @Override public void run() {
        if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "run()", "start");
        isRunning_ = true;
        try {
            init();
            dispatchLoop();
            destroy();
        } finally {
            isRunning_ = false;
        }
        if (logger.isLoggable(FINER)) logger.exiting(getClass().getName(), "run()", "end");
    }

    /** {@inheritDoc} */
    @Override public void start() {
        if (!isContinue()) {
            isContinue_ = true;
            if (executor_ != null) executor_.execute(this);
        }
    }

    /** {@inheritDoc} */
    @Override public void stop() {
        if (isContinue()) {
            isContinue_ = false;
            wakeupSelector();
        }
    }

    /** {@inheritDoc} */
    @Override public String toString() {
        return name_;
    }

    /** {@inheritDoc} */
    @Override public void wakeupSelector() {
        if (dispatcherThread_ != null && dispatcherThread_ != Thread.currentThread()) selector_.wakeup();
    }

    /**
     * Accept.
     * @param key the key
     * @return the socket channel
     * @throws IOException Signals that an I/O exception has occurred.
     */
    protected SocketChannel accept(final SelectionKey key) throws IOException {
        final ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
        final SocketChannel channel = serverChannel.accept();
        channel.configureBlocking(false);
        return channel;
    }

    /**
     * After change request.
     * @param dispatcher the dispatcher
     */
    abstract protected void afterChangeRequest();

    /**
     * After every dispatch.
     * @param channel the channel
     * @param readyOps the ready ops
     */
    abstract protected void afterEveryDispatch(final SelectableChannel channel, final int readyOps);

    /**
     * Before change request.
     * @param dipatcher the dipatcher
     */
    abstract protected void beforeChangeRequest();

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

    /**
     * Hand off after accept.
     * @param channel the channel
     */
    abstract protected void handOffAfterAccept(SocketChannel channel);

    /**
     * Hand off after connect.
     * @param channel the channel
     */
    abstract protected void handOffAfterConnect(SocketChannel channel);

    /**
     * Hand off after read.
     * @param channel the channel
     */
    abstract protected void handOffAfterRead(SocketChannel channel);

    /**
     * Hand off after write.
     * @param channel the channel
     */
    abstract protected void handOffAfterWrite(SocketChannel channel);

    /**
     * Inits the socket.
     * @param socket the socket
     */
    abstract protected void initSocket(final Socket socket);

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
     * On connection closed.
     * @param channel the channel
     */
    abstract protected void onConnectionClosed(SelectableChannel channel);

    /**
     * On connection connected.
     * @param channel the channel
     */
    abstract protected void onConnectionConnected(final SelectableChannel channel);

    /**
     * On message received.
     * @param channel the channel
     * @param msg the msg
     */
    abstract protected void onMessageReceived(final SocketChannel channel, final byte[] msg);

    /**
     * On message sent.
     * @param channel the channel
     * @param msg the msg
     */
    abstract protected void onMessageSent(final SocketChannel channel, final byte[] msg);

    /**
     * channel로 부터 데이터를 읽는다.
     * @param channel the channel
     * @return 완결된 하나의 메세지를 다 읽게 되면 true를 반환
     * @throws IOException the IO exception
     */
    abstract protected byte[] readMessage(final SocketChannel channel) throws IOException;

    /**
     * channel에 메세지들을 전송한다.
     * @param channel the channel
     * @return 더 이상 보낼 메세지가 없으면 true를 반환
     * @throws IOException the IO exception
     */
    abstract protected boolean sendMessage(final SocketChannel channel) throws IOException;

    /**
     * Adds the change request.
     * @param request the request
     */
    private final void addChangeRequest(final ChangeRequest request) {
        if (logger.isLoggable(FINER))
            logger.entering(getClass().getName(), "addChangeRequest(ChangeRequest=" + request + ")", "start");
        changeQueue_.add(request);
        wakeupSelector();
        if (logger.isLoggable(FINER))
            logger.exiting(getClass().getName(), "addChangeRequest(ChangeRequest)", "end");
    }

    /** Select one. */
    private final void dispatch() {
        if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "dispatch()", "start");
        try {
            final int nKeys = selector_.select(getSelectorTimeOut());
            if (nKeys < 1) return;
            final Iterator<SelectionKey> selectedKeys = selector_.selectedKeys().iterator();
            while (selectedKeys.hasNext()) {
                final SelectionKey key = selectedKeys.next();
                selectedKeys.remove();
                final SelectableChannel channel = processSelectedKey(key);
                if (channel != null) afterEveryDispatch(channel, key.readyOps());
            }
        } catch (final IOException ex) {
            logger.logp(SEVERE, getClass().getName(), "dispatch()", "", ex);
            selectorErrorCount_++;
        }
        if (logger.isLoggable(FINER)) logger.exiting(getClass().getName(), "dispatch()", "end");
    }

    /** Dispatch loop. */
    private final void dispatchLoop() {
        if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "dispatchLoop()", "start");
        while (isContinue())
            try {
                if (selectorErrorCount_ > getSelectorFailLimit()) {
                    try {
                        replaceSelector();
                    } catch (final IOException ex) {
                        logger.log(WARNING, "" + ex.getMessage(), ex);
                        break;
                    }
                    selectorErrorCount_ = 0;
                }
                beforeChangeRequest();
                processChangeRequest();
                afterChangeRequest();
                dispatch();
            } catch (final ClosedSelectorException e) {
                logger.logp(SEVERE, getClass().getName(), "dispatchLoop()", "", e);
                try {
                    replaceSelector();
                } catch (final IOException ex) {
                    logger.log(WARNING, "" + ex.getMessage(), ex);
                    break;
                }
                selectorErrorCount_ = 0;
            } catch (final Throwable t) {
                logger.logp(SEVERE, getClass().getName(), "dispatchLoop()", "", t);
                selectorErrorCount_++;
            }
        if (logger.isLoggable(FINER)) logger.exiting(getClass().getName(), "dispatchLoop()", "end");
    }

    /**
     * Checks if is continue.
     * @return true, if checks if is continue
     */
    private final boolean isContinue() {
        return isContinue_;
    }

    /** Process change request. */
    private final void processChangeRequest() {
        ChangeRequest request = null;
        while ((request = changeQueue_.poll()) != null) {
            final SelectionKey key = request.channel.keyFor(selector_);
            if (logger.isLoggable(FINEST)) logger.logp(FINEST, getClass().getName(), "processChangeRequest()",
                "change request(" + request + "), selection key(" + key + ")");
            switch (request.type) {
            case ADD_OPS:
                if (key != null && key.isValid()) key.interestOps(key.interestOps() | request.ops);
                break;
            case CHANGE_OPS:
                if (key != null && key.isValid()) key.interestOps(request.ops);
                break;
            case REMOVE_OPS:
                if (key != null && key.isValid())
                    key.interestOps(key.interestOps() ^ key.interestOps() & request.ops);
                break;
            case REGISTER:
                try {
                    request.channel.register(selector_, request.ops);
                } catch (final ClosedChannelException ex) {
                    logger.logp(WARNING, getClass().getName(), "processChangeRequest()",
                        "REGESTER:" + ex.getMessage(), ex);
                }
                break;
            case CLOSE_CHANNEL:
                try {
                    request.channel.close();
                    onConnectionClosed(request.channel);
                } catch (final IOException ex) {
                    logger.logp(WARNING, getClass().getName(), "processChangeRequest()",
                        "CLOSE_CHANNEL" + ex.getMessage(), ex);
                }
                //break;
            case DEREGISTER:
                if (key != null) {
                    if (key.isValid()) key.interestOps(0);
                    key.attach(null);
                    key.cancel();
                }
                break;
            }
        }
    }

    /**
     * Process selected key.
     * @param key the key
     * @return the selectable channel
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private SelectableChannel processSelectedKey(final SelectionKey key) throws IOException {
        SelectableChannel channel = key.channel();
        try {
            if (!key.isValid()) return null;
            if (key.isAcceptable()) channel = handleAccept(key);
            if (key.isConnectable()) handleConnect(key);
            if (key.isReadable()) handleRead(key);
            if (key.isWritable()) handleWrite(key);
        } catch (final CancelledKeyException cke) {
            logger.logp(WARNING, getClass().getName(), "processSelectedKey()", cke.getMessage(), cke);
            closeChannel(key.channel());
        }
        return channel;
    }

    /**
     * Replace selector.
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private void replaceSelector() throws IOException {
        if (logger.isLoggable(FINER)) logger.entering("AbstractSelectorDispatcher", "replaceSelector()", "start");
        final Selector oldSelector = selector_;
        final Selector newSelector = Selector.open();
        final Set<SelectionKey> keys = selector_.keys();
        for (final SelectionKey key : keys) {
            if (key.isValid()) key.channel().register(newSelector, key.interestOps(), key.attachment());
            key.cancel();
        }
        selector_ = newSelector;
        logger.logp(WARNING, getClass().getName(), "replaceSelector", "Selector replaced successfully.", newSelector);
        oldSelector.close();
        if (logger.isLoggable(FINER)) logger.exiting("AbstractSelectorDispatcher", "replaceSelector()", "end");
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
