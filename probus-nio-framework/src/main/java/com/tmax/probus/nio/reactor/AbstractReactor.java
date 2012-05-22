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


import static java.util.logging.Level.*;

import java.io.IOException;
import java.net.Socket;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
    /** The accept dispatchers. */
    private final List<ISelectorDispatcher> acceptDispatchers_ = Collections.synchronizedList(new ArrayList<ISelectorDispatcher>());
    /** The connect dispatchers. */
    private final List<ISelectorDispatcher> connectDispatchers_ = Collections.synchronizedList(new ArrayList<ISelectorDispatcher>());
    /** The read dispatchers. */
    private final List<ISelectorDispatcher> readDispatchers_ = Collections.synchronizedList(new ArrayList<ISelectorDispatcher>());
    /** The write dispatchers. */
    private final List<ISelectorDispatcher> writeDispatchers_ = Collections.synchronizedList(new ArrayList<ISelectorDispatcher>());

    /** {@inheritDoc} */
    @Override public void addOps(final SelectableChannel channel, final int ops) {
        if (logger.isLoggable(FINER)) logger.entering(getClass().getName(),
            "addOps(SelectableChannel=" + channel + ", int=" + ops + ")", "start");
        if ((channel.validOps() & ops & SelectionKey.OP_ACCEPT) == SelectionKey.OP_ACCEPT && getAcceptDispatcher(channel) != null)
            getAcceptDispatcher(channel).addOps(channel, SelectionKey.OP_ACCEPT);
        if ((channel.validOps() & ops & SelectionKey.OP_CONNECT) == SelectionKey.OP_CONNECT && getConnectDispatcher(channel) != null)
            getConnectDispatcher(channel).addOps(channel, SelectionKey.OP_CONNECT);
        if ((channel.validOps() & ops & SelectionKey.OP_READ) == SelectionKey.OP_READ && getReadDispatcher(channel) != null)
            getReadDispatcher(channel).addOps(channel, SelectionKey.OP_READ);
        if ((channel.validOps() & ops & SelectionKey.OP_WRITE) == SelectionKey.OP_WRITE && getWriteDispatcher(channel) != null)
            getWriteDispatcher(channel).addOps(channel, SelectionKey.OP_WRITE);
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
        if ((channel.validOps() & SelectionKey.OP_ACCEPT) == SelectionKey.OP_ACCEPT && getAcceptDispatcher(channel) != null)
            getAcceptDispatcher(channel).closeChannel(channel);
        if ((channel.validOps() & SelectionKey.OP_CONNECT) == SelectionKey.OP_CONNECT && getConnectDispatcher(channel) != null)
            getConnectDispatcher(channel).closeChannel(channel);
        if ((channel.validOps() & SelectionKey.OP_READ) == SelectionKey.OP_READ && getReadDispatcher(channel) != null)
            getReadDispatcher(channel).closeChannel(channel);
        if ((channel.validOps() & SelectionKey.OP_WRITE) == SelectionKey.OP_WRITE && getWriteDispatcher(channel) != null)
            getWriteDispatcher(channel).closeChannel(channel);
        if (logger.isLoggable(FINER)) logger.exiting(getClass().getName(), "closeChannel(SelectableChannel)", "end");
    }

    /** {@inheritDoc} */
    @Override public void deregister(final SelectableChannel channel) {
        if (logger.isLoggable(FINER))
            logger.entering(getClass().getName(), "deregister(SelectableChannel=" + channel + ")", "start");
        if ((channel.validOps() & SelectionKey.OP_ACCEPT) == SelectionKey.OP_ACCEPT && getAcceptDispatcher(channel) != null)
            getAcceptDispatcher(channel).deregister(channel);
        if ((channel.validOps() & SelectionKey.OP_CONNECT) == SelectionKey.OP_CONNECT && getConnectDispatcher(channel) != null)
            getConnectDispatcher(channel).deregister(channel);
        if ((channel.validOps() & SelectionKey.OP_READ) == SelectionKey.OP_READ && getReadDispatcher(channel) != null)
            getReadDispatcher(channel).deregister(channel);
        if ((channel.validOps() & SelectionKey.OP_WRITE) == SelectionKey.OP_WRITE && getWriteDispatcher(channel) != null)
            getWriteDispatcher(channel).deregister(channel);
        if (logger.isLoggable(FINER)) logger.exiting(getClass().getName(), "deregister(SelectableChannel)", "end");
    }

    /** {@inheritDoc} */
    @Override public void destroy() {
        if (dispatchExecutor_ != null && !dispatchExecutor_.isShutdown()) try {
            dispatchExecutor_.shutdown();
            if (!dispatchExecutor_.awaitTermination(1, TimeUnit.SECONDS)) dispatchExecutor_.shutdownNow();
        } catch (final InterruptedException ex) {
            logger.log(WARNING, ex.getMessage(), ex);
        } finally {
            final ExecutorService excutor = dispatchExecutor_;
            dispatchExecutor_ = null;
            if (!excutor.isShutdown()) excutor.shutdownNow();
        }
        getAcceptDispatchers().clear();
        getConnectDispatchers().clear();
        getReadDispatchers().clear();
        getWriteDispatchers().clear();
    }

    /** {@inheritDoc} */
    @Override public ISelectorDispatcher getAcceptDispatcher(final SelectableChannel channel) {
        return chooseDispatcher(getAcceptDispatchers(), channel);
    }

    /** {@inheritDoc} */
    @Override public ISelectorDispatcher getConnectDispatcher(final SelectableChannel channel) {
        return chooseDispatcher(getConnectDispatchers(), channel);
    }

    /** {@inheritDoc} */
    @Override public ISelectorDispatcher getReadDispatcher(final SelectableChannel channel) {
        return chooseDispatcher(getReadDispatchers(), channel);
    }

    /** {@inheritDoc} */
    @Override public ISelectorDispatcher getWriteDispatcher(final SelectableChannel channel) {
        return chooseDispatcher(getWriteDispatchers(), channel);
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
        dispatchExecutor_ = new ThreadPoolExecutor(1, getMaxSelectorThreadCount(), 0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(), tf);
    }

    /** {@inheritDoc} */
    @Override public void register(final SelectableChannel channel, final int ops) {
        if (logger.isLoggable(FINER)) logger.entering(getClass().getName(),
            "register(SelectableChannel=" + channel + ", int=" + ops + ")", "start");
        if ((channel.validOps() & ops & SelectionKey.OP_ACCEPT) == SelectionKey.OP_ACCEPT && getAcceptDispatcher(channel) != null)
            getAcceptDispatcher(channel).register(channel, SelectionKey.OP_ACCEPT);
        if ((channel.validOps() & ops & SelectionKey.OP_CONNECT) == SelectionKey.OP_CONNECT && getConnectDispatcher(channel) != null)
            getConnectDispatcher(channel).register(channel, SelectionKey.OP_CONNECT);
        if ((channel.validOps() & ops & SelectionKey.OP_READ) == SelectionKey.OP_READ && getReadDispatcher(channel) != null)
            getReadDispatcher(channel).register(channel, SelectionKey.OP_READ);
        if ((channel.validOps() & ops & SelectionKey.OP_WRITE) == SelectionKey.OP_WRITE && getWriteDispatcher(channel) != null)
            getWriteDispatcher(channel).register(channel, SelectionKey.OP_WRITE);
        if (logger.isLoggable(FINER)) logger.exiting(getClass().getName(), "register(SelectableChannel, int)", "end");
    }

    /** {@inheritDoc} */
    @Override public void removeOps(final SelectableChannel channel, final int ops) {
        if (logger.isLoggable(FINER)) logger.entering(getClass().getName(),
            "removeOps(SelectableChannel=" + channel + ", int=" + ops + ")", "start");
        if ((ops & SelectionKey.OP_ACCEPT) == SelectionKey.OP_ACCEPT && getAcceptDispatcher(channel) != null)
            getAcceptDispatcher(channel).removeOps(channel, SelectionKey.OP_ACCEPT);
        if ((ops & SelectionKey.OP_CONNECT) == SelectionKey.OP_CONNECT && getConnectDispatcher(channel) != null)
            getConnectDispatcher(channel).removeOps(channel, SelectionKey.OP_CONNECT);
        if ((ops & SelectionKey.OP_READ) == SelectionKey.OP_READ && getReadDispatcher(channel) != null)
            getReadDispatcher(channel).removeOps(channel, SelectionKey.OP_READ);
        if ((ops & SelectionKey.OP_WRITE) == SelectionKey.OP_WRITE && getWriteDispatcher(channel) != null)
            getWriteDispatcher(channel).removeOps(channel, SelectionKey.OP_WRITE);
        if (logger.isLoggable(FINER)) logger.exiting(getClass().getName(), "removeOps(SelectableChannel, int)", "end");
    }

    /** {@inheritDoc} */
    @Override public void start() {
        startDispatchers(getWriteDispatchers());
        startDispatchers(getReadDispatchers());
        startDispatchers(getConnectDispatchers());
        startDispatchers(getAcceptDispatchers());
    }

    /** {@inheritDoc} */
    @Override public void stop() {
        stopDispatchers(getAcceptDispatchers());
        stopDispatchers(getConnectDispatchers());
        stopDispatchers(getReadDispatchers());
        stopDispatchers(getWriteDispatchers());
    }

    /**
     * Process after jobs.
     * @param dispatcher the dispatcher
     */
    protected void afterChangeRequest(final ISelectorDispatcher dispatcher) {
    }

    /**
     * After every dispatch.
     * @param selectorDispatcher the selector dispatcher
     * @param channel the channel
     * @param readyOps the ready ops
     */
    protected void afterEveryDispatch(final ISelectorDispatcher selectorDispatcher, final SelectableChannel channel, final int readyOps) {
    }

    /**
     * Process pending jobs.
     * @param dipatcher the dipatcher
     */
    protected void beforeChangeRequest(final ISelectorDispatcher dipatcher) {
    }

    /**
     * Choose dispatcher.
     * @param dispatchers the dispatchers
     * @param channel the channel
     * @return the i selector dispatcher
     */
    protected ISelectorDispatcher chooseDispatcher(final List<ISelectorDispatcher> dispatchers, final SelectableChannel channel) {
        return dispatchers == null || dispatchers.isEmpty() ? null : dispatchers.get(channel.hashCode() % dispatchers.size());
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
     * Gets the accept dispatchers.
     * @return the accept dispatchers
     */
    protected List<ISelectorDispatcher> getAcceptDispatchers() {
        return acceptDispatchers_;
    }

    /**
     * Gets the connect dispatchers.
     * @return the connect dispatchers
     */
    protected List<ISelectorDispatcher> getConnectDispatchers() {
        return connectDispatchers_;
    }

    /**
     * Gets the max selector count.
     * @return the max selector count
     */
    abstract protected int getMaxSelectorThreadCount();

    /**
     * Gets the message handler.
     * @param channel the channel
     * @return the message handler
     */
    abstract protected IMessageIoHandler getMessageHandler(SocketChannel channel);

    /**
     * Gets the read dispatchers.
     * @return the read dispatchers
     */
    protected List<ISelectorDispatcher> getReadDispatchers() {
        return readDispatchers_;
    }

    /**
     * Gets the selector fail limit.
     * @param dispatcher the dispatcher
     * @return the selector fail limit
     */
    abstract protected int getSelectorFailLimit(ISelectorDispatcher dispatcher);

    /**
     * Gets the selector time out.
     * @param dispatcher the dispatcher
     * @return the selector time out
     */
    protected long getSelectorTimeOut(final ISelectorDispatcher dispatcher) {
        return 3000L;
    }

    /**
     * Gets the write dispatchers.
     * @return the write dispatchers
     */
    protected List<ISelectorDispatcher> getWriteDispatchers() {
        return writeDispatchers_;
    }

    /**
     * Hand off after accept.
     * @param dispatcher the dispatcher
     * @param channel the channel
     */
    abstract protected void handOffAfterAccept(ISelectorDispatcher dispatcher, SocketChannel channel);

    /**
     * Hand off after connect.
     * @param dispatcher the dispatcher
     * @param channel the channel
     */
    abstract protected void handOffAfterConnect(ISelectorDispatcher dispatcher, SocketChannel channel);

    /**
     * Hand off after read.
     * @param dispatcher the dispatcher
     * @param channel the channel
     */
    abstract protected void handOffAfterRead(ISelectorDispatcher dispatcher, SocketChannel channel);

    /**
     * Hand off after write.
     * @param dispatcher the dispatcher
     * @param channel the channel
     */
    abstract protected void handOffAfterWrite(ISelectorDispatcher dispatcher, SocketChannel channel);

    /**
     * Inits the socket.
     * @param dispatcher the dispatcher
     * @param socket the socket
     */
    abstract protected void initSocket(ISelectorDispatcher dispatcher, final Socket socket);

    /**
     * On connection closed.
     * @param dispatcher the dispatcher
     * @param channel the channel
     */
    abstract protected void onConnectionClosed(ISelectorDispatcher dispatcher, final SelectableChannel channel);

    /**
     * On connection connected.
     * @param dispatcher the dispatcher
     * @param channel the channel
     */
    abstract protected void onConnectionConnected(ISelectorDispatcher dispatcher, final SelectableChannel channel);

    /**
     * On message received.
     * @param dispatcher the dispatcher
     * @param channel the channel
     * @param msg the msg
     */
    abstract protected void onMessageReceived(ISelectorDispatcher dispatcher, final SocketChannel channel, final byte[] msg);

    /**
     * On message sent.
     * @param dispatcher the dispatcher
     * @param channel the channel
     * @param msg the msg
     */
    abstract protected void onMessageSent(ISelectorDispatcher dispatcher, final SocketChannel channel, final byte[] msg);

    /**
     * channel로 부터 데이터를 읽는다.
     * @param dispatcher the dispatcher
     * @param channel the channel
     * @return 완결된 하나의 메세지를 다 읽게 되면 true를 반환
     * @throws IOException the IO exception
     */
    protected byte[] readMessage(final ISelectorDispatcher dispatcher, final SocketChannel channel)
            throws IOException {
        if (logger.isLoggable(FINER))
            logger.entering(getClass().getName(), "readMessage(SocketChannel=" + channel + ")", "start");
        final IMessageIoHandler handler = getMessageHandler(channel);
        if (handler == null) throw new NullPointerException();
        final byte[] msg = handler.read();
        if (logger.isLoggable(FINER))
            logger.exiting(getClass().getName(), "readMessage(SocketChannel)", "end - return value=" + msg);
        return msg;
    }

    /**
     * channel에 메세지들을 전송한다.
     * @param dispatcher the dispatcher
     * @param channel the channel
     * @return 더 이상 보낼 메세지가 없으면 true를 반환
     * @throws IOException the IO exception
     */
    protected boolean sendMessage(final ISelectorDispatcher dispatcher, final SocketChannel channel)
            throws IOException {
        if (logger.isLoggable(FINER))
            logger.entering(getClass().getName(), "writeMessage(SocketChannel=" + channel + ")", "start");
        final IMessageIoHandler handler = getMessageHandler(channel);
        if (handler == null) throw new NullPointerException();
        final boolean ret = handler.send();
        if (logger.isLoggable(FINER))
            logger.exiting(getClass().getName(), "writeMessage(SocketChannel)", "end - return value=" + ret);
        return ret;
    }

    /**
     * Start dispatchers.
     * @param dispatchers the dispatchers
     */
    private void startDispatchers(final List<ISelectorDispatcher> dispatchers) {
        if (dispatchers != null && !dispatchers.isEmpty()) for (final ISelectorDispatcher dispatcher : dispatchers)
            dispatcher.start();
    }

    /**
     * Stop dispatchers.
     * @param dispatchers the dispatchers
     */
    private void stopDispatchers(final List<ISelectorDispatcher> dispatchers) {
        if (dispatchers != null && !dispatchers.isEmpty()) for (final ISelectorDispatcher dispatcher : dispatchers)
            dispatcher.stop();
    }

    /**
     * The Class DefaultSelectorDispatcher.
     */
    private final class DefaultSelectorDispatcher extends AbstractSelectorDispatcher {
        /**
         * Instantiates a new selector dispatcher.
         * @param name the name
         * @param dispatchExecutor the dispatch executor
         */
        protected DefaultSelectorDispatcher(final String name, final ExecutorService dispatchExecutor) {
            super(name, dispatchExecutor);
        }

        /** {@inheritDoc} */
        @Override protected void afterChangeRequest() {
            AbstractReactor.this.afterChangeRequest(this);
        }

        /** {@inheritDoc} */
        @Override protected void afterEveryDispatch(final SelectableChannel channel, final int readyOps) {
            AbstractReactor.this.afterEveryDispatch(this, channel, readyOps);
        }

        /** {@inheritDoc} */
        @Override protected void beforeChangeRequest() {
            AbstractReactor.this.beforeChangeRequest(this);
        }

        /** {@inheritDoc} */
        @Override protected int getSelectorFailLimit() {
            return AbstractReactor.this.getSelectorFailLimit(this);
        }

        /** {@inheritDoc} */
        @Override protected long getSelectorTimeOut() {
            return AbstractReactor.this.getSelectorTimeOut(this);
        }

        /** {@inheritDoc} */
        @Override protected void handOffAfterAccept(final SocketChannel channel) {
            AbstractReactor.this.handOffAfterAccept(this, channel);
        }

        /** {@inheritDoc} */
        @Override protected void handOffAfterConnect(final SocketChannel channel) {
            AbstractReactor.this.handOffAfterConnect(this, channel);
        }

        /** {@inheritDoc} */
        @Override protected void handOffAfterRead(final SocketChannel channel) {
            AbstractReactor.this.handOffAfterRead(this, channel);
        }

        /** {@inheritDoc} */
        @Override protected void handOffAfterWrite(final SocketChannel channel) {
            AbstractReactor.this.handOffAfterWrite(this, channel);
        }

        /** {@inheritDoc} */
        @Override protected void initSocket(final Socket socket) {
            AbstractReactor.this.initSocket(this, socket);
        }

        /** {@inheritDoc} */
        @Override protected void onConnectionClosed(final SelectableChannel channel) {
            AbstractReactor.this.onConnectionClosed(this, channel);
        }

        /** {@inheritDoc} */
        @Override protected void onConnectionConnected(final SelectableChannel channel) {
            AbstractReactor.this.onConnectionConnected(this, channel);
        }

        /** {@inheritDoc} */
        @Override protected void onMessageReceived(final SocketChannel channel, final byte[] msg) {
            AbstractReactor.this.onMessageReceived(this, channel, msg);
        }

        /** {@inheritDoc} */
        @Override protected void onMessageSent(final SocketChannel channel, final byte[] msg) {
            AbstractReactor.this.onMessageSent(this, channel, msg);
        }

        /** {@inheritDoc} */
        @Override protected byte[] readMessage(final SocketChannel channel) throws IOException {
            return AbstractReactor.this.readMessage(this, channel);
        }

        /** {@inheritDoc} */
        @Override protected boolean sendMessage(final SocketChannel channel) throws IOException {
            return AbstractReactor.this.sendMessage(this, channel);
        }
    }//end of Dispatcher
}
