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
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

import com.tmax.probus.nio.api.IReactor;
import com.tmax.probus.nio.api.ISelectorProcessor;
import com.tmax.probus.nio.api.ISession;


/**
 * The Class AbstractReactor.
 */
public abstract class AbstractReactor implements IReactor {
    /** The Constant MAX_RETRY_WRITE_CNT. */
    private static final int MAX_RETRY_WRITE_CNT = 2;
    /** Logger for this class. */
    private final transient Logger logger = Logger.getLogger("com.tmax.probus.nio.reactor");
    /** The time out_. */
    private long timeOut_;
    /** The is continue_. */
    private volatile boolean isContinue_ = false;
    /** The channel session map_. */
    private Map<SelectableChannel, ISession> channelSessionMap_ = new ConcurrentHashMap<SelectableChannel, ISession>();
    /** The server socket map_. */
    private Map<InetSocketAddress, ServerSocketChannel> serverSocketMap_;
    /** The processor set_. */
    private Set<ISelectorProcessor> processorSet_ = new HashSet<ISelectorProcessor>();

    /** {@inheritDoc} */
    @Override public void bind(final InetSocketAddress localAddr) {
        if (logger.isLoggable(FINER)) logger.entering("AbstractReactor", "bind(InetSocketAddress=" + localAddr + ")", "start");
        ServerSocketChannel server = null;
        try {
            server = ServerSocketChannel.open();
            server.configureBlocking(false);
            server.socket().bind(localAddr);
            getAcceptProcessor().register(server, SelectionKey.OP_ACCEPT);
            serverSocketMap_.put(localAddr, server);
        } catch (final IOException ex) {
            logger.logp(WARNING, "AbstractReactor", "bind(InetSocketAddress)", "exception ignored", ex);
        }
        if (logger.isLoggable(FINER)) logger.exiting("AbstractReactor", "bind(InetSocketAddress)", "end");
    }

    /** {@inheritDoc} */
    @Override public ISession connect(final InetSocketAddress remoteAddr, final InetAddress localAddr) {
        if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "connect");
        SocketChannel channel = null;
        ISession session = null;
        try {
            channel = SocketChannel.open();
            channel.configureBlocking(false);
            if (localAddr != null) channel.socket().bind(new InetSocketAddress(localAddr, 0));
            channel.connect(remoteAddr);
            session = createSession(null, channel);
            putSession(channel, session);
            getConnectProcessor().register(channel, SelectionKey.OP_CONNECT);
        } catch (final IOException ex) {
            logger.log(WARNING, "" + ex.getMessage(), ex);
        }
        return session;
    }

    /** {@inheritDoc} */
    @Override public ISession getSession(final SocketChannel channel) {
        return channelSessionMap_.get(channel);
    }

    /** {@inheritDoc} */
    @Override public void putSession(final SocketChannel channel, final ISession session) {
        channelSessionMap_.put(channel, session);
    }

    /** {@inheritDoc} */
    @Override public ISession removeSession(final SocketChannel channel) {
        return channelSessionMap_.remove(channel);
    }

    /** {@inheritDoc} */
    @Override public void start() {
        isContinue_ = true;
        for (final ISelectorProcessor processor : processorSet_) {
            if (!processor.isRunning()) processor.start();
        }
    }

    /** {@inheritDoc} */
    @Override public void stop() {
        isContinue_ = false;
        final Map<SelectableChannel, ISession> channelSessionMap = channelSessionMap_;
        channelSessionMap_ = null;
        channelSessionMap.clear();
        final Map<InetSocketAddress, ServerSocketChannel> serverSocketMap = serverSocketMap_;
        serverSocketMap_ = null;
        serverSocketMap.clear();
        final Set<ISelectorProcessor> processorSet = processorSet_;
        processorSet_ = null;
        processorSet.clear();
    }

    /** {@inheritDoc} */
    @Override public void unbind(final InetSocketAddress localAddr) {
        final ServerSocketChannel serverChannel = serverSocketMap_.get(localAddr);
        try {
            serverChannel.socket().close();
            serverChannel.close();
            getAcceptProcessor().deregister(serverChannel);
        } catch (final IOException ex) {
        }
    }

    /**
     * After accept.
     * @param channel the channel
     */
    protected void afterAccept(final SocketChannel channel) {
        ISession session = getSession(channel);
        session.afterAccept(this);
    }

    /**
     * After connect.
     * @param channel the channel
     */
    protected void afterConnect(final SocketChannel channel) {
        ISession session = getSession(channel);
        session.afterConnect(this);
    }

    /**
     * After read.
     * @param channel the channel
     */
    protected void afterRead(final SocketChannel channel) {
        ISession session = getSession(channel);
        session.afterRead(this);
    }

    /**
     * After write.
     * @param channel the channel
     */
    protected void afterWrite(final SocketChannel channel) {
        ISession session = getSession(channel);
        session.afterWrite(this);
    }

    /**
     * Creates the selector processor.
     * @param name the name
     * @return the i selector processor
     */
    protected ISelectorProcessor createSelectorProcessor(final String name) {
        final ISelectorProcessor processor = new SelectorProcessor(name);
        processorSet_.add(processor);
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
        final ISession session = getSession(channel);
        final ByteBuffer readBuffer = session.acquireReadBuffer();
        try {
            int nRead = 0, nLastRead = 0;
            System.out.println(readBuffer.hasRemaining());
            while (readBuffer.hasRemaining() && (nLastRead = channel.read(readBuffer)) > 0)
                nRead += nLastRead;
            if (logger.isLoggable(FINE)) logger.logp(FINE, getClass().getName(), "processRead(SocketChannel)", "nRead=" + nRead);
            if (nLastRead < 0) { return false; }
        } finally {
            session.releaseReadBuffer();
        }
        return session.onMessageRead();
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
        if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "writeMessage");
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
        if (logger.isLoggable(FINER)) logger.exiting(getClass().getName(), "writeMessage");
        return true;
    }

    /**
     * Process accept.
     * @param key the key
     * @throws IOException the IO exception
     */
    private final void handleAccept(final SelectionKey key) throws IOException {
        if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "processAccept");
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
        if (logger.isLoggable(FINER)) logger.exiting(getClass().getName(), "processAccept");
    }

    /**
     * Process connect.
     * @param key the key
     * @throws IOException the IO exception
     */
    private final void handleConnect(final SelectionKey key) throws IOException {
        if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "processConnect");
        final SocketChannel channel = (SocketChannel) key.channel();
        if (channel != null) {
            if (channel.finishConnect()) {
                key.interestOps(0);
                initSocket(channel.socket());
                afterConnect(channel);
            }
        }
        if (logger.isLoggable(FINER)) logger.exiting(getClass().getName(), "processConnect");
    }

    /**
     * Process read.
     * @param key the key
     * @throws IOException the IO exception
     */
    private final void handleRead(final SelectionKey key) throws IOException {
        if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "processRead");
        final SocketChannel channel = (SocketChannel) key.channel();
        if (readMessage(channel)) afterRead(channel);
        if (logger.isLoggable(FINER)) logger.exiting(getClass().getName(), "processRead");
    }

    /**
     * Process write.
     * @param key the key
     * @throws IOException the IO exception
     */
    private final void handleWrite(final SelectionKey key) throws IOException {
        if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "processWrite");
        final SocketChannel channel = (SocketChannel) key.channel();
        if (writeMessage(channel)) afterWrite(channel);
        if (logger.isLoggable(FINER)) logger.exiting(getClass().getName(), "processWrite");
    }

    /**
     * Checks if is continue.
     * @return true, if checks if is continue
     */
    private final boolean isContinue() {
        return isContinue_;
    }

    @Override public void changeOpts(SelectableChannel channel, int opts) {
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
    }

    @Override public void register(SelectableChannel channel, int opts) {
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
    }

    @Override public void deregister(SelectableChannel channel) {
        if (channel instanceof ServerSocketChannel) getAcceptProcessor().deregister(channel);
        else {
            getReadWriteProcessor().deregister(channel);
            getConnectProcessor().deregister(channel);
        }
    }

    /**
     * The Class SelectorProcessor.
     */
    protected class SelectorProcessor extends Thread implements ISelectorProcessor {
        /** The selector_. */
        private Selector selector_;
        /** The change queue_. */
        Queue<ChangeRequest> changeQueue_ = new LinkedBlockingQueue<ChangeRequest>();
        /** The status_. */
        Status status_ = Status.STOPPED;

        /**
         * Instantiates a new selector processor.
         * @param name the name
         */
        public SelectorProcessor(final String name) {
            super(name);
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
        @Override public boolean isRunning() {
            return status_ == Status.STARTED;
        }

        /** {@inheritDoc} */
        @Override public void register(final SelectableChannel channel, final int opts) {
            addChangeRequest(new ChangeRequest(ChangeType.REGISTER, channel, opts));
        }

        /** {@inheritDoc} */
        @Override public void run() {
            setUp();
            selectAll();
            tearDown();
        }

        /** {@inheritDoc} */
        @Override public void wakeup() {
            selector_.wakeup();
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
            try {
                selector_ = Selector.open();
            } catch (final IOException ex) {
                logger.log(WARNING, "" + ex.getMessage(), ex);
            }
            status_ = Status.STARTED;
        }

        /**
         * Tear down.
         */
        protected void tearDown() {
            status_ = Status.STOPPED;
            final Queue<ChangeRequest> queue = changeQueue_;
            changeQueue_ = null;
            queue.clear();
        }

        /**
         * Adds the change request.
         * @param request the request
         */
        private final void addChangeRequest(final ChangeRequest request) {
            changeQueue_.add(request);
            wakeup();
        }

        /**
         * Process change request.
         */
        private void processChangeRequest() {
            ChangeRequest request = null;
            while ((request = changeQueue_.poll()) != null) {
                final SelectionKey key = request.channel.keyFor(selector_);
                switch (request.type) {
                case CHANGE_OPTS:
                    if (key != null) key.interestOps(request.opts);
                    break;
                case DEREGISTER:
                    if (key != null) {
                        key.attach(null);
                        key.cancel();
                    }
                    break;
                case REGISTER:
                    try {
                        request.channel.register(selector_, request.opts);
                    } catch (final ClosedChannelException ex) {
                        logger.log(WARNING, "" + ex.getMessage(), ex);
                    }
                    break;
                }
            }
        }

        /**
         * Select all.
         */
        private final void selectAll() {
            if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "selectAll");
            while (isContinue()) {
                processBeforeJobs();
                selectOne();
                processAfterJobs();
            }
            if (logger.isLoggable(FINER)) logger.exiting(getClass().getName(), "selectAll");
        }

        /**
         * Select one.
         */
        private final void selectOne() {
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
                logger.log(WARNING, "" + ex.getMessage(), ex);
            }
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
         * @param attachment the attachment
         */
        private ChangeRequest(final ChangeType type, final SelectableChannel channel, final int opts) {
            this.type = type;
            this.channel = channel;
            this.opts = opts;
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
        CHANGE_OPTS;
    }

    /**
     * The Enum Status.
     */
    private enum Status {
        /** The STARTED. */
        STARTED,
        /** The STOPPED. */
        STOPPED;
    }
}
