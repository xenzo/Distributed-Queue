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
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import com.tmax.probus.nio.api.IMessageIoHandler;
import com.tmax.probus.nio.api.IReactor;
import com.tmax.probus.nio.api.ISelectorDispatcher;


/** The Class AbstractReactor. */
public abstract class AbstractReactor implements IReactor {
    /** Logger for this class. */
    protected final transient Logger logger = Logger.getLogger("com.tmax.probus.nio.reactor");
    /** The dispatcher executor_. */
    private ExecutorService dispatchExecutor_;
    /** The default dispatcher_. */
    private ISelectorDispatcher defaultDispatcher_;

    /** {@inheritDoc} */
    @Override public void addOps(final SelectableChannel channel, final int ops) {
        if (logger.isLoggable(FINER)) logger.entering(getClass().getName(),
            "addOps(SelectableChannel=" + channel + ", int=" + ops + ")", "start");
        if ((ops & SelectionKey.OP_ACCEPT) == SelectionKey.OP_ACCEPT && getAcceptDispatcher() != null)
            getAcceptDispatcher().addOps(channel, SelectionKey.OP_ACCEPT);
        if ((ops & SelectionKey.OP_CONNECT) == SelectionKey.OP_CONNECT && getConnectDispatcher() != null)
            getConnectDispatcher().addOps(channel, SelectionKey.OP_CONNECT);
        if ((ops & SelectionKey.OP_READ) == SelectionKey.OP_READ && getReadDispatcher() != null)
            getReadDispatcher().addOps(channel, SelectionKey.OP_READ);
        if ((ops & SelectionKey.OP_WRITE) == SelectionKey.OP_WRITE && getWriteDispatcher() != null)
            getWriteDispatcher().addOps(channel, SelectionKey.OP_WRITE);
        if (logger.isLoggable(FINER)) logger.exiting(getClass().getName(), "addOps(SelectionKey, int)", "end");
    }

    /** {@inheritDoc} */
    @Override public void changeOps(final SelectableChannel channel, final int ops) {
        if (logger.isLoggable(FINER)) logger.entering(getClass().getName(),
            "changeOps(SelectableChannel=" + channel + ", int=" + ops + ")", "start");
        //dispatcher의 changeOps를 호출하는 경우 ops가 단일 op이 아닌 경우 최종 것으로만 설정되기 때문에 add/remove한다.
        addOps(channel, channel.validOps() & ops);
        removeOps(channel, channel.validOps() ^ ops);
        if (logger.isLoggable(FINER)) logger.exiting(getClass().getName(), "changeOps(SelectableChannel, int)", "end");
    }

    /** {@inheritDoc} */
    @Override public final void closeChannel(final SelectableChannel channel) {
        if (logger.isLoggable(FINER))
            logger.entering(getClass().getName(), "closeChannel(SelectableChannel=" + channel + ")", "start");
        if ((channel.validOps() & SelectionKey.OP_ACCEPT) == SelectionKey.OP_ACCEPT && getAcceptDispatcher() != null)
            getAcceptDispatcher().closeChannel(channel);
        if ((channel.validOps() & SelectionKey.OP_CONNECT) == SelectionKey.OP_CONNECT && getConnectDispatcher() != null)
            getConnectDispatcher().closeChannel(channel);
        if ((channel.validOps() & SelectionKey.OP_READ) == SelectionKey.OP_READ && getReadDispatcher() != null)
            getReadDispatcher().closeChannel(channel);
        if ((channel.validOps() & SelectionKey.OP_WRITE) == SelectionKey.OP_WRITE && getWriteDispatcher() != null)
            getWriteDispatcher().closeChannel(channel);
        if (logger.isLoggable(FINER)) logger.exiting(getClass().getName(), "closeChannel(SelectableChannel)", "end");
    }

    /** {@inheritDoc} */
    @Override public void deregister(final SelectableChannel channel) {
        if (logger.isLoggable(FINER))
            logger.entering(getClass().getName(), "deregister(SelectableChannel=" + channel + ")", "start");
        if ((channel.validOps() & SelectionKey.OP_ACCEPT) == SelectionKey.OP_ACCEPT && getAcceptDispatcher() != null)
            getAcceptDispatcher().deregister(channel);
        if ((channel.validOps() & SelectionKey.OP_CONNECT) == SelectionKey.OP_CONNECT && getConnectDispatcher() != null)
            getConnectDispatcher().deregister(channel);
        if ((channel.validOps() & SelectionKey.OP_READ) == SelectionKey.OP_READ && getReadDispatcher() != null)
            getReadDispatcher().deregister(channel);
        if ((channel.validOps() & SelectionKey.OP_WRITE) == SelectionKey.OP_WRITE && getWriteDispatcher() != null)
            getWriteDispatcher().deregister(channel);
        if (logger.isLoggable(FINER)) logger.exiting(getClass().getName(), "deregister(SelectableChannel)", "end");
    }

    /** {@inheritDoc} */
    @Override public void destroy() {
        if (dispatchExecutor_ != null && !dispatchExecutor_.isShutdown()) {
            try {
                dispatchExecutor_.shutdown();
                if (!dispatchExecutor_.awaitTermination(1, TimeUnit.SECONDS)) dispatchExecutor_.shutdownNow();
            } catch (final InterruptedException ex) {
                logger.log(WARNING, ex.getMessage(), ex);
            } finally {
                final ExecutorService excutor = dispatchExecutor_;
                dispatchExecutor_ = null;
                if (!excutor.isShutdown()) excutor.shutdownNow();
            }
        }
    }

    /** {@inheritDoc} */
    @Override public ISelectorDispatcher getAcceptDispatcher() {
        return defaultDispatcher_;
    }

    /** {@inheritDoc} */
    @Override public ISelectorDispatcher getConnectDispatcher() {
        return defaultDispatcher_;
    }

    /** {@inheritDoc} */
    @Override public ISelectorDispatcher getReadDispatcher() {
        return defaultDispatcher_;
    }

    /** {@inheritDoc} */
    @Override public ISelectorDispatcher getWriteDispatcher() {
        return defaultDispatcher_;
    }

    /** {@inheritDoc} */
    @Override public void init() {
        final ThreadFactory tf = new ThreadFactory() {
            volatile int seq = 0;

            @Override public Thread newThread(final Runnable r) {
                final Thread t = new Thread(r, "DISPATCHER_" + seq++);
                t.setDaemon(true);
                t.setPriority(Thread.MAX_PRIORITY);
                return t;
            }
        };
        dispatchExecutor_ = new ThreadPoolExecutor(1, 3, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(),
                tf);
        defaultDispatcher_ = createSelectorDispatcher("DEFAULT");
    }

    /** {@inheritDoc} */
    @Override public void register(final SelectableChannel channel, final int ops) {
        if (logger.isLoggable(FINER)) logger.entering(getClass().getName(),
            "register(SelectableChannel=" + channel + ", int=" + ops + ")", "start");
        if ((ops & SelectionKey.OP_ACCEPT) == SelectionKey.OP_ACCEPT && getAcceptDispatcher() != null)
            getAcceptDispatcher().register(channel, SelectionKey.OP_ACCEPT);
        if ((ops & SelectionKey.OP_CONNECT) == SelectionKey.OP_CONNECT && getConnectDispatcher() != null)
            getConnectDispatcher().register(channel, SelectionKey.OP_CONNECT);
        if ((ops & SelectionKey.OP_READ) == SelectionKey.OP_READ && getReadDispatcher() != null)
            getReadDispatcher().register(channel, SelectionKey.OP_READ);
        if ((ops & SelectionKey.OP_WRITE) == SelectionKey.OP_WRITE && getWriteDispatcher() != null)
            getWriteDispatcher().register(channel, SelectionKey.OP_WRITE);
        if (logger.isLoggable(FINER)) logger.exiting(getClass().getName(), "register(SelectableChannel, int)", "end");
    }

    /** {@inheritDoc} */
    @Override public void removeOps(final SelectableChannel channel, final int ops) {
        if (logger.isLoggable(FINER)) logger.entering(getClass().getName(),
            "removeOps(SelectableChannel=" + channel + ", int=" + ops + ")", "start");
        if ((ops & SelectionKey.OP_ACCEPT) == SelectionKey.OP_ACCEPT && getAcceptDispatcher() != null)
            getAcceptDispatcher().removeOps(channel, SelectionKey.OP_ACCEPT);
        if ((ops & SelectionKey.OP_CONNECT) == SelectionKey.OP_CONNECT && getConnectDispatcher() != null)
            getConnectDispatcher().removeOps(channel, SelectionKey.OP_CONNECT);
        if ((ops & SelectionKey.OP_READ) == SelectionKey.OP_READ && getReadDispatcher() != null)
            getReadDispatcher().removeOps(channel, SelectionKey.OP_READ);
        if ((ops & SelectionKey.OP_WRITE) == SelectionKey.OP_WRITE && getWriteDispatcher() != null)
            getWriteDispatcher().removeOps(channel, SelectionKey.OP_WRITE);
        if (logger.isLoggable(FINER)) logger.exiting(getClass().getName(), "removeOps(SelectableChannel, int)", "end");
    }

    /** {@inheritDoc} */
    @Override public void start() {
        if (getWriteDispatcher() != null) getWriteDispatcher().start();
        if (getReadDispatcher() != null) getReadDispatcher().start();
        if (getConnectDispatcher() != null) getConnectDispatcher().start();
        if (getAcceptDispatcher() != null) getAcceptDispatcher().start();
    }

    /** {@inheritDoc} */
    @Override public void stop() {
        if (getAcceptDispatcher() != null) getAcceptDispatcher().stop();
        if (getConnectDispatcher() != null) getConnectDispatcher().stop();
        if (getReadDispatcher() != null) getReadDispatcher().stop();
        if (getWriteDispatcher() != null) getWriteDispatcher().stop();
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
     * Creates the selector processor.
     * @param name the name
     * @return the i selector processor
     */
    protected ISelectorDispatcher createSelectorDispatcher(final String name) {
        return new DefaultSelectorDispatcher(name, dispatchExecutor_);
    }

    /**
     * Gets the message handler.
     * @param channel the channel
     * @return the message handler
     */
    abstract protected IMessageIoHandler getMessageReader(SocketChannel channel);

    /**
     * Gets the selector time out.
     * @return the selector time out
     */
    protected long getSelectorTimeOut() {
        return 3000L;
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
    abstract protected void onConnectionClosed(final SelectableChannel channel);

    /**
     * On connection connected.
     * @param channel the channel
     */
    abstract protected void onConnectionConnected(final SelectableChannel channel);

    /**
     * @param channel
     * @param readyOps
     */
    protected void afterEveryDispatch(final SelectableChannel channel, final int readyOps) {
    }

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

    /** Process after jobs. */
    protected void postChangeRequest() {
    }

    /** Process pending jobs. */
    protected void preChangeRequest() {
    }

    /**
     * channel로 부터 데이터를 읽는다.
     * @param channel the channel
     * @return 완결된 하나의 메세지를 다 읽게 되면 true를 반환
     * @throws IOException the IO exception
     */
    protected byte[] readMessage(final SocketChannel channel) throws IOException {
        if (logger.isLoggable(FINER))
            logger.entering(getClass().getName(), "readMessage(SocketChannel=" + channel + ")", "start");
        final IMessageIoHandler handler = getMessageReader(channel);
        if (handler == null) throw new NullPointerException();
        final byte[] msg = handler.read();
        if (logger.isLoggable(FINER))
            logger.exiting(getClass().getName(), "readMessage(SocketChannel)", "end - return value=" + msg);
        return msg;
    }

    /**
     * channel에 메세지들을 전송한다.
     * @param channel the channel
     * @return 더 이상 보낼 메세지가 없으면 true를 반환
     * @throws IOException the IO exception
     */
    protected boolean sendMessage(final SocketChannel channel) throws IOException {
        if (logger.isLoggable(FINER))
            logger.entering(getClass().getName(), "writeMessage(SocketChannel=" + channel + ")", "start");
        final IMessageIoHandler handler = getMessageReader(channel);
        if (handler == null) throw new NullPointerException();
        final boolean ret = handler.send();
        if (logger.isLoggable(FINER))
            logger.exiting(getClass().getName(), "writeMessage(SocketChannel)", "end - return value=" + ret);
        return ret;
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

    /**
     * The Class SelectorProcessor.
     * <p/>
     *
     * <pre>
     * run
     * |-init
     * |-dispatchLoop(반복)
     * | |-processBeforeJob
     * | |-dispatch
     * | |-processAfterJob
     * |-destroy
     * </pre>
     */
    protected final class DefaultSelectorDispatcher implements ISelectorDispatcher {
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
        private Thread dispatcherThread_;

        /**
         * Instantiates a new selector dispatcher.
         * @param name the name
         * @param dispatchExecutor the dispatch executor
         */
        protected DefaultSelectorDispatcher(final String name, final ExecutorService dispatchExecutor) {
            name_ = name;
            executor_ = dispatchExecutor;
        }

        /** {@inheritDoc} */
        @Override public void addOps(final SelectableChannel channel, final int ops) {
            final SelectionKey key = channel.keyFor(selector_);
            if (key == null) register(channel, ops);
            else if ((ops ^ (key.interestOps() & ops)) != 0)
                addChangeRequest(new ChangeRequest(ChangeType.ADD_OPS, channel, ops));
        }

        /** {@inheritDoc} */
        @Override public void changeOps(final SelectableChannel channel, final int ops) {
            throw new UnsupportedOperationException();
            //            final SelectionKey key = channel.keyFor(selector_);
            //            if (key == null) register(channel, ops);
            //            else if (key.interestOps() != ops)
            //                addChangeRequest(new ChangeRequest(ChangeType.CHANGE_OPS, channel, ops));
        }

        /** {@inheritDoc} */
        @Override public void closeChannel(final SelectableChannel channel) {
            addChangeRequest(new ChangeRequest(ChangeType.CLOSE_CHANNEL, channel, -1));
        }

        /** {@inheritDoc} */
        @Override public void deregister(final SelectableChannel channel) {
            if (channel.keyFor(selector_) != null)
                addChangeRequest(new ChangeRequest(ChangeType.DEREGISTER, channel, -1));
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

        /** {@inheritDoc} */
        @Override public void handleConnect(final SelectionKey key) throws IOException {
            final SocketChannel channel = (SocketChannel) key.channel();
            if (isConnected(channel)) {
                removeOps(channel, SelectionKey.OP_CONNECT);
                initSocket(channel.socket());
                onConnectionConnected(channel);
                handOffAfterConnect(channel);
            }
        }

        /** {@inheritDoc} */
        @Override public void handleRead(final SelectionKey key) throws IOException {
            final SocketChannel channel = (SocketChannel) key.channel();
            byte[] msg = null;
            if ((msg = readMessage(channel)) != null) {
                //                removeOps(channel, SelectionKey.OP_READ);
                onMessageReceived(channel, msg);
                handOffAfterRead(channel);
            }
        }

        /** {@inheritDoc} */
        @Override public void handleWrite(final SelectionKey key) throws IOException {
            final SocketChannel channel = (SocketChannel) key.channel();
            if (sendMessage(channel)) {
                removeOps(channel, SelectionKey.OP_WRITE);
                handOffAfterWrite(channel);
            }
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
        @Override public void register(final SelectableChannel channel, final int ops) {
            if (channel.keyFor(selector_) != null) addOps(channel, ops);
            else addChangeRequest(new ChangeRequest(ChangeType.REGISTER, channel, ops));
        }

        /** {@inheritDoc} */
        @Override public void removeOps(final SelectableChannel channel, final int ops) {
            final SelectionKey key = channel.keyFor(selector_);
            if (key != null && (key.interestOps() & ops) != 0)
                addChangeRequest(new ChangeRequest(ChangeType.REMOVE_OPS, channel, ops));
        }

        /** {@inheritDoc} */
        @Override public void run() {
            if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "run()", "start");
            init();
            dispatchLoop();
            destroy();
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
            }
            if (logger.isLoggable(FINER)) logger.exiting(getClass().getName(), "dispatch()", "end");
        }

        /** Select all. */
        private final void dispatchLoop() {
            if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "dispatchLoop()", "start");
            while (isContinue()) {
                try {
                    preChangeRequest();
                    processChangeRequest();
                    postChangeRequest();
                    dispatch();
                } catch (final ClosedSelectorException e) {
                    logger.logp(SEVERE, getClass().getName(), "dispatchLoop()", "", e);
                    break;
                } catch (final Throwable t) {
                    logger.logp(SEVERE, getClass().getName(), "dispatchLoop()", "", t);
                }
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
                        key.interestOps(key.interestOps() ^ (key.interestOps() & request.ops));
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
    }//end of Dispatcher
}
