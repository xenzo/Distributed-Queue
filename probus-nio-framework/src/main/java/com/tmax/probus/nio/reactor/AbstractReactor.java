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
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

import com.tmax.probus.nio.api.IReactor;
import com.tmax.probus.nio.api.ISelectorProcessor;
import com.tmax.probus.nio.api.ISession;
import com.tmax.probus.nio.api.ISessionManager;


/**
 * The Class AbstractReactor.
 */
public abstract class AbstractReactor implements IReactor, ISessionManager {
    /** The Constant MAX_RETRY_WRITE_CNT. */
    private static final int MAX_RETRY_WRITE_CNT = 2;
    /** Logger for this class. */
    private final transient Logger logger = Logger.getLogger("com.tmax.probus.nio.reactor");
    /** The time out_. */
    private long timeOut_;
    /** The channel session map_. */
    private Map<SelectableChannel, ISession> channelSessionMap_;

    /** {@inheritDoc} */
    @Override public void changeOpts(final SelectableChannel channel, final int opts) {
        if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "changeOpts(SelectableChannel=" + channel + ", int=" + opts + ")", "start");
        switch (opts) {
        case SelectionKey.OP_ACCEPT:
            getAcceptProcessor().changeOpts(channel, opts);
            break;
        case SelectionKey.OP_CONNECT:
            getConnectProcessor().changeOpts(channel, opts);
            break;
        case SelectionKey.OP_READ:
        case SelectionKey.OP_WRITE:
            getReadWriteProcessor().changeOpts(channel, opts);
            break;
        }
        if (logger.isLoggable(FINER)) logger.exiting(getClass().getName(), "changeOpts(SelectableChannel, int)", "end");
    }

    /** {@inheritDoc} */
    @Override public void deregister(final SelectableChannel channel) {
        if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "deregister(SelectableChannel=" + channel + ")", "start");
        if (channel instanceof ServerSocketChannel) getAcceptProcessor().deregister(channel);
        else {
            getReadWriteProcessor().deregister(channel);
            getConnectProcessor().deregister(channel);
        }
        if (logger.isLoggable(FINER)) logger.exiting(getClass().getName(), "deregister(SelectableChannel)", "end");
    }

    /** {@inheritDoc} */
    @Override public void destroy() {
        if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "destroy()", "start");
        if (getAcceptProcessor() != null) getAcceptProcessor().shutDown();
        if (getConnectProcessor() != null) getConnectProcessor().shutDown();
        if (getReadWriteProcessor() != null) getReadWriteProcessor().shutDown();
        final Map<SelectableChannel, ISession> channelSessionMap = channelSessionMap_;
        channelSessionMap_ = null;
        channelSessionMap.clear();
        if (logger.isLoggable(FINER)) logger.exiting(getClass().getName(), "destroy()", "end");
    }

    /** {@inheritDoc} */
    @Override public ISession getSession(final SocketChannel channel) {
        if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "getSession(SocketChannel=" + channel + ")", "start");
        final ISession returnISession = channelSessionMap_.get(channel);
        if (logger.isLoggable(FINER)) logger.exiting(getClass().getName(), "getSession(SocketChannel)", "end - return value=" + returnISession);
        return returnISession;
    }

    /** {@inheritDoc} */
    @Override public void init() {
        if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "init()", "start");
        channelSessionMap_ = new ConcurrentHashMap<SelectableChannel, ISession>();
        if (getReadWriteProcessor() != null) getReadWriteProcessor().startUp();
        if (getConnectProcessor() != null) getConnectProcessor().startUp();
        if (getAcceptProcessor() != null) getAcceptProcessor().startUp();
        if (logger.isLoggable(FINER)) logger.exiting(getClass().getName(), "init()", "end");
    }

    /** {@inheritDoc} */
    @Override public void putSession(final SocketChannel channel, final ISession session) {
        if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "putSession(SocketChannel=" + channel + ", ISession=" + session + ")", "start");
        channelSessionMap_.put(channel, session);
        if (logger.isLoggable(FINER)) logger.exiting(getClass().getName(), "putSession(SocketChannel, ISession)", "end");
    }

    /** {@inheritDoc} */
    @Override public void register(final SelectableChannel channel, final int opts) {
        if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "register(SelectableChannel=" + channel + ", int=" + opts + ")", "start");
        switch (opts) {
        case SelectionKey.OP_ACCEPT:
            getAcceptProcessor().register(channel, opts);
            break;
        case SelectionKey.OP_CONNECT:
            getConnectProcessor().register(channel, opts);
            break;
        case SelectionKey.OP_READ:
        case SelectionKey.OP_WRITE:
            getReadWriteProcessor().register(channel, opts);
            break;
        }
        if (logger.isLoggable(FINER)) logger.exiting(getClass().getName(), "register(SelectableChannel, int)", "end");
    }

    /** {@inheritDoc} */
    @Override public void removeOpts(final SelectableChannel channel, final int opts) {
        if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "removeOpts(SelectableChannel=" + channel + ", int=" + opts + ")", "start");
        switch (opts) {
        case SelectionKey.OP_ACCEPT:
            getAcceptProcessor().removeOpts(channel, opts);
            break;
        case SelectionKey.OP_CONNECT:
            getConnectProcessor().changeOpts(channel, opts);
            break;
        case SelectionKey.OP_READ:
        case SelectionKey.OP_WRITE:
            getReadWriteProcessor().changeOpts(channel, opts);
            break;
        }
        if (logger.isLoggable(FINER)) logger.exiting(getClass().getName(), "removeOpts(SelectableChannel, int)", "end");
    }

    /** {@inheritDoc} */
    @Override public ISession removeSession(final SocketChannel channel) {
        if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "removeSession(SocketChannel=" + channel + ")", "start");
        final ISession returnISession = channelSessionMap_.remove(channel);
        if (logger.isLoggable(FINER)) logger.exiting(getClass().getName(), "removeSession(SocketChannel)", "end - return value=" + returnISession);
        return returnISession;
    }

    /**
     * After accept.
     * @param channel the channel
     */
    protected void afterAccept(final SocketChannel channel) {
        if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "afterAccept(SocketChannel=" + channel + ")", "start");
        final ISession session = getSession(channel);
        session.afterAccept(this);
        if (logger.isLoggable(FINER)) logger.exiting(getClass().getName(), "afterAccept(SocketChannel)", "end");
    }

    /**
     * After connect.
     * @param channel the channel
     */
    protected void afterConnect(final SocketChannel channel) {
        if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "afterConnect(SocketChannel=" + channel + ")", "start");
        final ISession session = getSession(channel);
        session.afterConnect(this);
        if (logger.isLoggable(FINER)) logger.exiting(getClass().getName(), "afterConnect(SocketChannel)", "end");
    }

    /**
     * After read.
     * @param channel the channel
     */
    protected void afterRead(final SocketChannel channel) {
        if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "afterRead(SocketChannel=" + channel + ")", "start");
        final ISession session = getSession(channel);
        session.afterRead(this);
        if (logger.isLoggable(FINER)) logger.exiting(getClass().getName(), "afterRead(SocketChannel)", "end");
    }

    /**
     * After write.
     * @param channel the channel
     */
    protected void afterWrite(final SocketChannel channel) {
        if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "afterWrite(SocketChannel=" + channel + ")", "start");
        final ISession session = getSession(channel);
        session.afterWrite(this);
        if (logger.isLoggable(FINER)) logger.exiting(getClass().getName(), "afterWrite(SocketChannel)", "end");
    }

    /**
     * Bind.
     * @param localAddr the local addr
     * @return the server socket channel
     * @throws IOException Signals that an I/O exception has occurred.
     */
    protected ServerSocketChannel bind(final InetSocketAddress localAddr) throws IOException {
        if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "bind(InetSocketAddress=" + localAddr + ")", "start");
        ServerSocketChannel server = null;
        server = ServerSocketChannel.open();
        server.configureBlocking(false);
        server.socket().bind(localAddr);
        getAcceptProcessor().register(server, SelectionKey.OP_ACCEPT);
        if (logger.isLoggable(FINER)) logger.exiting(getClass().getName(), "bind(InetSocketAddress)", "end - return value=" + server);
        return server;
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
     * Connect.
     * @param remoteAddr the remote addr
     * @param localAddr the local addr
     * @return the i session
     * @throws IOException Signals that an I/O exception has occurred.
     */
    protected ISession connect(final InetSocketAddress remoteAddr, final InetSocketAddress localAddr)
            throws IOException {
        if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "connect(InetSocketAddress=" + remoteAddr + ", InetSocketAddress=" + localAddr + ")", "start");
        SocketChannel channel = null;
        ISession session = null;
        channel = SocketChannel.open();
        channel.configureBlocking(false);
        if (localAddr != null) channel.socket().bind(localAddr);
        channel.connect(remoteAddr);
        session = createSession(null, channel);
        putSession(channel, session);
        getConnectProcessor().register(channel, SelectionKey.OP_CONNECT);
        if (logger.isLoggable(FINER)) logger.exiting(getClass().getName(), "connect(InetSocketAddress, InetSocketAddress)", "end - return value=" + session);
        return session;
    }

    /**
     * Creates the selector processor.
     * @param name the name
     * @return the i selector processor
     */
    protected ISelectorProcessor createSelectorProcessor(final String name) {
        if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "createSelectorProcessor(String=" + name + ")", "start");
        final ISelectorProcessor processor = new SelectorProcessor(name);
        if (logger.isLoggable(FINER)) logger.exiting(getClass().getName(), "createSelectorProcessor(String)", "end - return value=" + processor);
        return processor;
    }

    /**
     * Creates the session.
     * @param serverChannel the server channel
     * @param channel the channel
     * @return the i session
     */
    abstract protected ISession createSession(final SelectableChannel serverChannel, final SocketChannel channel);

    /**
     * Inits the socket.
     * @param socket the socket
     */
    abstract protected void initSocket(final Socket socket);

    /**
     * Process read.
     * @param channel the channel
     * @return true, if process read
     * @throws IOException the IO exception
     */
    protected boolean readMessage(final SocketChannel channel) throws IOException {
        if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "readMessage(SocketChannel=" + channel + ")", "start");
        final ISession session = getSession(channel);
        final ByteBuffer readBuffer = session.acquireReadBuffer();
        int nRead = 0, nLastRead = 0;
        try {
            while (readBuffer.hasRemaining() && (nLastRead = channel.read(readBuffer)) > 0)
                nRead += nLastRead;
            if (logger.isLoggable(FINE)) logger.logp(FINE, getClass().getName(), "readMessage(SocketChannel)", "nRead=" + nRead);
            if (nLastRead < 0) {
                if (logger.isLoggable(FINER)) logger.exiting(getClass().getName(), "readMessage(SocketChannel)", "end - return value=" + false);
                return false;
            }
        } finally {
            session.releaseReadBuffer();
        }
        final boolean result = session.onMessageRead(nLastRead < 0);
        if (logger.isLoggable(FINER)) logger.exiting(getClass().getName(), "readMessage(SocketChannel)", "end - return value=" + result);
        return result;
    }

    /**
     * Sets the selector timeout.
     * @param timeout the new selector timeout
     */
    protected final void setSelectorTimeout(final long timeout) {
        timeOut_ = timeout;
    }

    /**
     * Write message.
     * @param channel the channel
     * @return true, if write message
     * @throws IOException the IO exception
     */
    protected boolean writeMessage(final SocketChannel channel) throws IOException {
        if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "writeMessage(SocketChannel=" + channel + ")", "start");
        final ISession session = getSession(channel);
        final Queue<ByteBuffer> queue = session.acquireWriteQueue();
        try {
            if (queue == null || queue.isEmpty()) return true;
            while (!queue.isEmpty()) {
                final ByteBuffer msg = queue.peek();
                int cnt = 0;
                while (cnt++ < MAX_RETRY_WRITE_CNT && msg.hasRemaining())
                    channel.write(msg);
                if (msg.hasRemaining()) return false;
                queue.remove();
            }
        } finally {
            session.releaseWriteQueue();
        }
        if (logger.isLoggable(FINER)) logger.exiting(getClass().getName(), "writeMessage(SocketChannel)", "end - return value=" + true);
        return true;
    }

    /**
     * Process accept.
     * @param key the key
     * @throws IOException the IO exception
     */
    private final void handleAccept(final SelectionKey key) throws IOException {
        if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "handleAccept(SelectionKey=" + key + ")", "start");
        final ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
        final SocketChannel channel = serverChannel.accept();
        if (channel != null) {
            final ISession session = createSession(serverChannel, channel);
            putSession(channel, session);
            if (channel.finishConnect()) {
                key.interestOps(0);
                initSocket(channel.socket());
                afterAccept(channel);
            }
        }
        if (logger.isLoggable(FINER)) logger.exiting(getClass().getName(), "handleAccept(SelectionKey)", "end");
    }

    /**
     * Process connect.
     * @param key the key
     * @throws IOException the IO exception
     */
    private final void handleConnect(final SelectionKey key) throws IOException {
        if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "handleConnect(SelectionKey=" + key + ")", "start");
        final SocketChannel channel = (SocketChannel) key.channel();
        if (channel != null && channel.finishConnect()) {
            key.interestOps(0);
            initSocket(channel.socket());
            afterConnect(channel);
        }
        if (logger.isLoggable(FINER)) logger.exiting(getClass().getName(), "handleConnect(SelectionKey)", "end");
    }

    /**
     * Process read.
     * @param key the key
     * @throws IOException the IO exception
     */
    private final void handleRead(final SelectionKey key) throws IOException {
        if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "handleRead(SelectionKey=" + key + ")", "start");
        final SocketChannel channel = (SocketChannel) key.channel();
        if (readMessage(channel)) afterRead(channel);
        if (logger.isLoggable(FINER)) logger.exiting(getClass().getName(), "handleRead(SelectionKey)", "end");
    }

    /**
     * Process write.
     * @param key the key
     * @throws IOException the IO exception
     */
    private final void handleWrite(final SelectionKey key) throws IOException {
        if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "handleWrite(SelectionKey=" + key + ")", "start");
        final SocketChannel channel = (SocketChannel) key.channel();
        if (writeMessage(channel)) afterWrite(channel);
        if (logger.isLoggable(FINER)) logger.exiting(getClass().getName(), "handleWrite(SelectionKey)", "end");
    }

    /**
     * The Class SelectorProcessor.
     */
    protected class SelectorProcessor extends Thread implements ISelectorProcessor {
        /** The selector_. */
        private Selector selector_;
        /** The change queue_. */
        Queue<ChangeRequest> changeQueue_ = new LinkedBlockingQueue<ChangeRequest>();
        /** The is continue_. */
        private volatile boolean isContinue_ = false;

        /**
         * Instantiates a new selector processor.
         * @param name the name
         */
        public SelectorProcessor(final String name) {
            super(name);
            //setDaemon(true);
        }

        /** {@inheritDoc} */
        @Override public void changeOpts(final SelectableChannel channel, final int opts) {
            if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "changeOpts(SelectableChannel=" + channel + ", int=" + opts + ")", "start");
            addChangeRequest(new ChangeRequest(ChangeType.CHANGE_OPTS, channel, opts));
            if (logger.isLoggable(FINER)) logger.exiting(getClass().getName(), "changeOpts(SelectableChannel, int)", "end");
        }

        /** {@inheritDoc} */
        @Override public void deregister(final SelectableChannel channel) {
            if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "deregister(SelectableChannel=" + channel + ")", "start");
            addChangeRequest(new ChangeRequest(ChangeType.DEREGISTER, channel, -1));
            if (logger.isLoggable(FINER)) logger.exiting(getClass().getName(), "deregister(SelectableChannel)", "end");
        }

        /** {@inheritDoc} */
        @Override public void register(final SelectableChannel channel, final int opts) {
            if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "register(SelectableChannel=" + channel + ", int=" + opts + ")", "start");
            addChangeRequest(new ChangeRequest(ChangeType.REGISTER, channel, opts));
            if (logger.isLoggable(FINER)) logger.exiting(getClass().getName(), "register(SelectableChannel, int)", "end");
        }

        /** {@inheritDoc} */
        @Override public void removeOpts(final SelectableChannel channel, final int opts) {
            if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "removeOpts(SelectableChannel=" + channel + ", int=" + opts + ")", "start");
            addChangeRequest(new ChangeRequest(ChangeType.REMOVE_OPTS, channel, opts));
            if (logger.isLoggable(FINER)) logger.exiting(getClass().getName(), "removeOpts(SelectableChannel, int)", "end");
        }

        /** {@inheritDoc} */
        @Override public void run() {
            if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "run()", "start");
            setUp();
            selectAll();
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
            if (!isContinue()) {
                isContinue_ = true;
                super.start();
            }
        }

        /** {@inheritDoc} */
        @Override public void wakeupSelector() {
            if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "wakeup()", "start");
            selector_.wakeup();
            if (logger.isLoggable(FINER)) logger.exiting(getClass().getName(), "wakeup()", "end");
        }

        /**
         * Process after jobs.
         */
        protected void processAfterJobs() {
        }

        /**
         * Process pending jobs.
         */
        protected void processBeforeJobs() {
            processChangeRequest();
        }

        /**
         * Sets the up.
         */
        protected void setUp() {
            if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "setUp()", "start");
            try {
                selector_ = Selector.open();
            } catch (final IOException ex) {
                logger.logp(SEVERE, getClass().getName(), "setUp()", "", ex);
            }
            if (logger.isLoggable(FINER)) logger.exiting(getClass().getName(), "setUp()", "end");
        }

        /**
         * Tear down.
         */
        protected void tearDown() {
            if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "tearDown()", "start");
            try {
                selector_.close();
            } catch (IOException ex) {
                logger.logp(SEVERE, getClass().getName(), "tearDown()", "", ex);
            }
            final Queue<ChangeRequest> queue = changeQueue_;
            changeQueue_ = null;
            queue.clear();
            if (logger.isLoggable(FINER)) logger.exiting(getClass().getName(), "tearDown()", "end");
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
         * Checks if is continue.
         * @return true, if checks if is continue
         */
        private final boolean isContinue() {
            return isContinue_;
        }

        /**
         * Process change request.
         */
        private void processChangeRequest() {
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

        /**
         * Select all.
         */
        private final void selectAll() {
            if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "selectAll()", "start");
            while (isContinue()) {
                processBeforeJobs();
                selectOne();
                processAfterJobs();
            }
            if (logger.isLoggable(FINER)) logger.exiting(getClass().getName(), "selectAll()", "end");
        }

        /**
         * Select one.
         */
        private final void selectOne() {
            if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "selectOne()", "start");
            try {
                final int nKeys = selector_.select(timeOut_);
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
                logger.logp(SEVERE, getClass().getName(), "selectOne()", "", ex);
            }
            if (logger.isLoggable(FINER)) logger.exiting(getClass().getName(), "selectOne()", "end");
        }
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
            sb.append(type);
            if (opts > 0) sb.append(" : ").append(opts);
            if (channel != null) sb.append(" : ").append(channel);
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
}
