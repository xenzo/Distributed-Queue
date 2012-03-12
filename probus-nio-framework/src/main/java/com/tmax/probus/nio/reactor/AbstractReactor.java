/*
 * AbstractReactor.java Version 1.0 Mar 8, 2012
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


/*
 * AbstractReactor.java Version 1.0 Feb 24, 2012
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
import static java.util.logging.Level.*;

import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import com.tmax.probus.nio.api.IConnectionEventListener;
import com.tmax.probus.nio.api.IMessageEventListener;
import com.tmax.probus.nio.api.IMessageHandler;
import com.tmax.probus.nio.api.IReactor;
import com.tmax.probus.nio.api.ISelectorDispatcher;


/**
 * The Class AbstractReactor.
 */
public abstract class AbstractReactor implements IReactor {
    /** Logger for this class. */
    protected final transient Logger logger = Logger.getLogger("com.tmax.probus.nio.reactor");
    /** The dispatcher executor_. */
    private ExecutorService dispatchExecutor_;

    /** {@inheritDoc} */
    @Override public void changeOpts(final SelectableChannel channel, final int opts) {
        if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "changeOpts(SelectableChannel=" + channel + ", int=" + opts + ")", "start");
        switch (opts) {
        case SelectionKey.OP_ACCEPT:
            getAcceptDispatcher().changeOpts(channel, opts);
            break;
        case SelectionKey.OP_CONNECT:
            getConnectDispatcher().changeOpts(channel, opts);
            break;
        case SelectionKey.OP_READ:
        case SelectionKey.OP_WRITE:
            getReadWriteDispatcher().changeOpts(channel, opts);
            break;
        }
        if (logger.isLoggable(FINER)) logger.exiting(getClass().getName(), "changeOpts(SelectableChannel, int)", "end");
    }

    /** {@inheritDoc} */
    @Override public void deregister(final SelectableChannel channel) {
        if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "deregister(SelectableChannel=" + channel + ")", "start");
        if (channel instanceof ServerSocketChannel) getAcceptDispatcher().deregister(channel);
        else {
            getReadWriteDispatcher().deregister(channel);
            getConnectDispatcher().deregister(channel);
        }
        if (logger.isLoggable(FINER)) logger.exiting(getClass().getName(), "deregister(SelectableChannel)", "end");
    }

    /** {@inheritDoc} */
    @Override public void destroy() {
        if (!dispatchExecutor_.isShutdown()) {
            dispatchExecutor_.shutdown();
            try {
                dispatchExecutor_.awaitTermination(1, TimeUnit.SECONDS);
            } catch (final InterruptedException ex) {
                logger.log(WARNING, "" + ex.getMessage(), ex);
            } finally {
                if (!dispatchExecutor_.isShutdown()) dispatchExecutor_.shutdownNow();
                dispatchExecutor_ = null;
            }
        }
    }

    /** {@inheritDoc} */
    @Override public void init() {
        final ThreadFactory tf = new ThreadFactory() {
            volatile int seq = 0;

            @Override public Thread newThread(final Runnable r) {
                final Thread t = new Thread(r, "DISPATCHER_" + seq++);
                t.setDaemon(true);
                return t;
            }
        };
        dispatchExecutor_ = new ThreadPoolExecutor(1, 3, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(), tf);
    }

    /** {@inheritDoc} */
    @Override public void register(final SelectableChannel channel, final int opts) {
        if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "register(SelectableChannel=" + channel + ", int=" + opts + ")", "start");
        switch (opts) {
        case SelectionKey.OP_ACCEPT:
            getAcceptDispatcher().register(channel, opts);
            break;
        case SelectionKey.OP_CONNECT:
            getConnectDispatcher().register(channel, opts);
            break;
        case SelectionKey.OP_READ:
        case SelectionKey.OP_WRITE:
            getReadWriteDispatcher().register(channel, opts);
            break;
        }
        if (logger.isLoggable(FINER)) logger.exiting(getClass().getName(), "register(SelectableChannel, int)", "end");
    }

    /** {@inheritDoc} */
    @Override public void removeOpts(final SelectableChannel channel, final int opts) {
        if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "removeOpts(SelectableChannel=" + channel + ", int=" + opts + ")", "start");
        switch (opts) {
        case SelectionKey.OP_ACCEPT:
            getAcceptDispatcher().removeOpts(channel, opts);
            break;
        case SelectionKey.OP_CONNECT:
            getConnectDispatcher().changeOpts(channel, opts);
            break;
        case SelectionKey.OP_READ:
        case SelectionKey.OP_WRITE:
            getReadWriteDispatcher().changeOpts(channel, opts);
            break;
        }
        if (logger.isLoggable(FINER)) logger.exiting(getClass().getName(), "removeOpts(SelectableChannel, int)", "end");
    }

    /** {@inheritDoc} */
    @Override public void start() {
        if (getReadWriteDispatcher() != null) getReadWriteDispatcher().startUp();
        if (getConnectDispatcher() != null) getConnectDispatcher().startUp();
        if (getAcceptDispatcher() != null) getAcceptDispatcher().startUp();
        final Set<Runnable> s = new LinkedHashSet<Runnable>();
        s.add(getReadWriteDispatcher());
        s.add(getConnectDispatcher());
        s.add(getAcceptDispatcher());
        for (final Runnable runnable : s)
            dispatchExecutor_.execute(runnable);
    }

    /** {@inheritDoc} */
    @Override public void stop() {
        if (getAcceptDispatcher() != null) getAcceptDispatcher().shutDown();
        if (getConnectDispatcher() != null) getConnectDispatcher().shutDown();
        if (getReadWriteDispatcher() != null) getReadWriteDispatcher().shutDown();
    }

    /**
     * OP_ACCEPT를 처리.
     * @param key the key
     * @return the socket channel
     * @throws IOException Signals that an I/O exception has occurred.
     */
    protected SocketChannel accept(final SelectionKey key) throws IOException {
        throw new UnsupportedOperationException();
    }

    /**
     * Close channel.
     * @param channel the channel
     * @throws IOException Signals that an I/O exception has occurred.
     */
    protected final void closeChannel(final SelectableChannel channel) throws IOException {
        if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "closeChannel(SelectableChannel=" + channel + ")", "start");
        if (channel instanceof ServerSocketChannel) {
            final ServerSocketChannel c = (ServerSocketChannel) channel;
            c.socket().close();
            c.close();
        } else if (channel instanceof SocketChannel) {
            final SocketChannel c = (SocketChannel) channel;
            c.socket().close();
            c.close();
        }
        if (logger.isLoggable(FINER)) logger.exiting(getClass().getName(), "closeChannel(SelectableChannel)", "end");
    }

    /**
     * Creates the selector processor.
     * @param name the name
     * @return the i selector processor
     */
    protected ISelectorDispatcher createSelectorDispatcher(final String name) {
        return new SelectorDispatcher(name);
    }

    /**
     * Gets the connection event listener.
     * @param channel the channel
     * @return the connection event listener
     */
    abstract protected IConnectionEventListener getConnectionEventListener(SocketChannel channel);

    /**
     * Gets the message event listener.
     * @param channel the channel
     * @return the message event listener
     */
    abstract protected IMessageEventListener getMessageEventListener(SocketChannel channel);

    /**
     * Gets the message handler.
     * @param channel the channel
     * @return the message handler
     */
    abstract protected IMessageHandler getMessageHandler(SocketChannel channel);

    /**
     * Gets the selector time out.
     * @return the selector time out
     */
    protected long getSelectorTimeOut() {
        return 3000L;
    }

    /**
     * Gets the write retry count.
     * @return the write retry count
     */
    protected int getWriteRetryCount() {
        return 2;
    }

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
    protected void initSocket(final Socket socket) {
    }

    /**
     * channel로 부터 데이터를 읽는다.
     * @param channel the channel
     * @return 완결된 하나의 메세지를 다 읽게 되면 true를 반환
     * @throws IOException the IO exception
     */
    protected byte[] readMessage(final SocketChannel channel) throws IOException {
        if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "readMessage(SocketChannel=" + channel + ")", "start");
        final IMessageHandler handler = getMessageHandler(channel);
        if (handler == null) throw new NullPointerException();
        final ByteBuffer readBuffer = handler.acquireReadBuffer();
        int nRead = 0, nLastRead = 0;
        byte[] msg = null;
        try {
            while (readBuffer.hasRemaining() && (nLastRead = channel.read(readBuffer)) > 0)
                nRead += nLastRead;
            if (logger.isLoggable(FINE)) logger.logp(FINE, getClass().getName(), "readMessage(SocketChannel)", "nRead=" + nRead + ", nLastRead=" + nLastRead);
            readBuffer.flip();
            final boolean isEof = nLastRead < 0;
            msg = handler.onMessageRead(isEof);
            readBuffer.compact();
            if (isEof) {
                final IConnectionEventListener connectionEventListener = getConnectionEventListener(channel);
                if (connectionEventListener != null) connectionEventListener.eventConnectionClosed(this, channel);
            }
        } finally {
            handler.releaseReadBuffer();
        }
        if (logger.isLoggable(FINER)) logger.exiting(getClass().getName(), "readMessage(SocketChannel)", "end - return value=" + msg);
        return msg;
    }

    /**
     * channel에 메세지들을 전송한다.
     * @param channel the channel
     * @return 더 이상 보낼 메세지가 없으면 true를 반환
     * @throws IOException the IO exception
     */
    protected boolean writeMessage(final SocketChannel channel) throws IOException {
        if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "writeMessage(SocketChannel=" + channel + ")", "start");
        final IMessageHandler handler = getMessageHandler(channel);
        if (handler == null) throw new NullPointerException();
        final Queue<ByteBuffer> queue = handler.acquireWriteQueue();
        try {
            if (queue == null || queue.isEmpty()) return true;
            while (!queue.isEmpty()) {
                final ByteBuffer msg = queue.peek();
                int cnt = 0;
                while (cnt++ < getWriteRetryCount() && msg.hasRemaining())
                    channel.write(msg);
                if (msg.hasRemaining()) return false;
                //                IMessageEventListener listener = getMessageEventListener(channel);
                //                if (listener != null) listener.eventMessageSent(msg.array());
                queue.remove();
            }
        } finally {
            handler.releaseWriteQueue();
        }
        if (logger.isLoggable(FINER)) logger.exiting(getClass().getName(), "writeMessage(SocketChannel)", "end - return value=" + true);
        return true;
    }

    /**
     * Checks if is connected.
     * @param channel the channel
     * @return true, if is connected
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private final boolean isConnected(final SocketChannel channel) throws IOException {
        return channel != null && channel.finishConnect();
    }

    /**
     * Process accept.
     * @param key the key
     * @throws IOException the IO exception
     */
    private final void processAccept(final SelectionKey key) throws IOException {
        final SocketChannel channel = accept(key);
        if (isConnected(channel)) {
            initSocket(channel.socket());
            final IConnectionEventListener listener = getConnectionEventListener(channel);
            if (listener != null) listener.eventConnectionConnected(this, channel);
            handOffAfterAccept(channel);
        }
    }

    /**
     * Process connect.
     * @param key the key
     * @throws IOException the IO exception
     */
    private final void processConnect(final SelectionKey key) throws IOException {
        final SocketChannel channel = (SocketChannel) key.channel();
        if (isConnected(channel)) {
            initSocket(channel.socket());
            key.interestOps(0);
            final IConnectionEventListener listener = getConnectionEventListener(channel);
            if (listener != null) listener.eventConnectionConnected(this, channel);
            handOffAfterConnect(channel);
        }
    }

    /**
     * Process read.
     * @param key the key
     * @throws IOException the IO exception
     */
    private final void processRead(final SelectionKey key) throws IOException {
        final SocketChannel channel = (SocketChannel) key.channel();
        final byte[] msg = readMessage(channel);
        final boolean inputIsComplete = msg != null;
        if (inputIsComplete) {
            final IMessageEventListener listener = getMessageEventListener(channel);
            if (listener != null) listener.eventMessageReceived(msg);
            handOffAfterRead(channel);
        }
    }

    /**
     * Process write.
     * @param key the key
     * @throws IOException the IO exception
     */
    private final void processWrite(final SelectionKey key) throws IOException {
        final SocketChannel channel = (SocketChannel) key.channel();
        if (writeMessage(channel)) handOffAfterWrite(channel);
    }

    /**
     * The Class ChangeRequest.
     */
    private class ChangeRequest {
        /** The type_. */
        private final ChangeType type;
        /** The channel_. */
        private final SelectableChannel channel;
        /** The opts_. */
        private final int opts;

        /**
         * The Constructor.
         * @param type the type
         * @param channel the channel
         * @param opts the opts
         */
        private ChangeRequest(final ChangeType type, final SelectableChannel channel, final int opts) {
            this.type = type;
            this.channel = channel;
            this.opts = opts;
        }

        /** {@inheritDoc} */
        @Override public String toString() {
            final StringBuilder sb = new StringBuilder();
            sb.append("type=" + type);
            if (opts > 0) sb.append(", opts=").append(opts);
            if (channel != null) sb.append(", channel=").append(channel);
            return sb.toString();
        }
    }

    /**
     * The Enum ChangeType.
     */
    private enum ChangeType {
        /** The REGISTER. */
        REGISTER,
        /** The DEREGISTER. */
        DEREGISTER,
        /** The CHANG e_ opts. */
        CHANGE_OPTS,
        /** The REMOV e_ opts. */
        REMOVE_OPTS;
    }

    /**
     * The Class SelectorProcessor.
     *
     * <pre>
     * run
     * |-setUp
     * |-dispatchLoop(반복)
     * | |-processBeforeJob
     * | |-dispatch
     * | |-processAfterJob
     * |-tearDown
     * </pre>
     */
    private class SelectorDispatcher implements ISelectorDispatcher {
        /** The selector_. */
        private Selector selector_;
        /** The change queue_. */
        private Queue<ChangeRequest> changeQueue_ = null;
        /** The is continue_. */
        private volatile boolean isContinue_ = false;
        /** The name_. */
        private final String name_;

        /**
         * Instantiates a new selector dispatcher.
         * @param name the name
         */
        public SelectorDispatcher(final String name) {
            name_ = name;
        }

        /** {@inheritDoc} */
        @Override public void changeOpts(final SelectableChannel channel, final int opts) {
            addChangeRequest(new ChangeRequest(ChangeType.CHANGE_OPTS, channel, opts));
        }

        /** {@inheritDoc} */
        @Override public void deregister(final SelectableChannel channel) {
            addChangeRequest(new ChangeRequest(ChangeType.DEREGISTER, channel, -1));
        }

        /** {@inheritDoc} */
        @Override public void handleAccept(final SelectionKey key) throws IOException {
            processAccept(key);
        }

        /** {@inheritDoc} */
        @Override public void handleConnect(final SelectionKey key) throws IOException {
            processConnect(key);
        }

        /** {@inheritDoc} */
        @Override public void handleRead(final SelectionKey key) throws IOException {
            processRead(key);
        }

        /** {@inheritDoc} */
        @Override public void handleWrite(final SelectionKey key) throws IOException {
            processWrite(key);
        }

        /** {@inheritDoc} */
        @Override public void register(final SelectableChannel channel, final int opts) {
            addChangeRequest(new ChangeRequest(ChangeType.REGISTER, channel, opts));
        }

        /** {@inheritDoc} */
        @Override public void removeOpts(final SelectableChannel channel, final int opts) {
            addChangeRequest(new ChangeRequest(ChangeType.REMOVE_OPTS, channel, opts));
        }

        /** {@inheritDoc} */
        @Override public void run() {
            if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "run()", "start");
            setUp();
            dispatchLoop();
            tearDown();
            if (logger.isLoggable(FINER)) logger.exiting(getClass().getName(), "run()", "end");
        }

        /** {@inheritDoc} */
        @Override public void shutDown() {
            if (isContinue()) {
                isContinue_ = false;
                wakeupSelector();
            }
        }

        /** {@inheritDoc} */
        @Override public void startUp() {
            if (!isContinue()) isContinue_ = true;
        }

        /** {@inheritDoc} */
        @Override public String toString() {
            return name_;
        }

        /** {@inheritDoc} */
        @Override public void wakeupSelector() {
            selector_.wakeup();
        }

        /**
         * Process after jobs.
         */
        protected void postDispatch() {
        }

        /**
         * Process pending jobs.
         */
        protected void preDispatch() {
            processChangeRequest();
        }

        /**
         * Sets the up.
         */
        protected void setUp() {
            if (changeQueue_ == null) changeQueue_ = new LinkedBlockingQueue<ChangeRequest>();
            try {
                selector_ = Selector.open();
            } catch (final IOException ex) {
                logger.logp(SEVERE, getClass().getName(), "setUp()", "", ex);
            }
        }

        /**
         * Tear down.
         */
        protected void tearDown() {
            try {
                if (selector_ != null) selector_.close();
            } catch (final IOException ex) {
                logger.logp(SEVERE, getClass().getName(), "tearDown()", "", ex);
            }
            if (changeQueue_ != null) {
                final Queue<ChangeRequest> queue = changeQueue_;
                changeQueue_ = null;
                queue.clear();
            }
        }

        /**
         * Adds the change request.
         * @param request the request
         */
        private final void addChangeRequest(final ChangeRequest request) {
            if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "addChangeRequest(ChangeRequest=" + request + ")", "start");
            changeQueue_.add(request);
            wakeupSelector();
            if (logger.isLoggable(FINER)) logger.exiting(getClass().getName(), "addChangeRequest(ChangeRequest)", "end");
        }

        /**
         * Select one.
         */
        private final void dispatch() {
            if (logger.isLoggable(FINEST)) logger.entering(getClass().getName(), "dispatch()", "start");
            try {
                final int nKeys = selector_.select(getSelectorTimeOut());
                if (nKeys <= 0) return;
                final Iterator<SelectionKey> selectedKeys = selector_.selectedKeys().iterator();
                while (selectedKeys.hasNext()) {
                    final SelectionKey key = selectedKeys.next();
                    selectedKeys.remove();
                    if (!key.isValid()) continue;
                    if (key.isAcceptable()) handleAccept(key);
                    if (key.isConnectable()) handleConnect(key);
                    if (key.isReadable()) handleRead(key);
                    if (key.isWritable()) handleWrite(key);
                }
            } catch (final IOException ex) {
                logger.logp(SEVERE, getClass().getName(), "dispatch()", "", ex);
            }
            if (logger.isLoggable(FINEST)) logger.exiting(getClass().getName(), "dispatch()", "end");
        }

        /**
         * Select all.
         */
        private final void dispatchLoop() {
            if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "dispatchLoop()", "start");
            while (isContinue())
                try {
                    preDispatch();
                    dispatch();
                    postDispatch();
                } catch (final Throwable t) {
                    logger.logp(SEVERE, getClass().getName(), "dispatchLoop()", "", t);
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

        /**
         * Process change request.
         */
        private final void processChangeRequest() {
            ChangeRequest request = null;
            while ((request = changeQueue_.poll()) != null) {
                final SelectionKey key = request.channel.keyFor(selector_);
                if (logger.isLoggable(FINEST)) logger.logp(FINEST, getClass().getName(), "processChangeRequest()", "change request(" + request + "), selection key(" + key + ")");
                switch (request.type) {
                case CHANGE_OPTS:
                    if (key != null) key.interestOps(request.opts);
                    break;
                case REMOVE_OPTS:
                    if (key != null) key.interestOps(key.interestOps() ^ request.opts);
                    break;
                case DEREGISTER:
                    if (key != null) {
                        key.interestOps(0);
                        key.attach(null);
                        key.cancel();
                    }
                    break;
                case REGISTER:
                    try {
                        request.channel.register(selector_, request.opts);
                    } catch (final ClosedChannelException ex) {
                        logger.throwing(getClass().getName(), "processChangeRequest()", ex);
                    }
                    break;
                }
            }
        }
    }
}
