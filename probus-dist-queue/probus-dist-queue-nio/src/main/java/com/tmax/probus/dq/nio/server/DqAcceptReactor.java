/*
 * DqAcceptor.java Version 1.0 Jan 26, 2012
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
package com.tmax.probus.dq.nio.server;


import static java.util.logging.Level.*;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import com.tmax.probus.dq.DqNode;
import com.tmax.probus.dq.api.IDqReactor;
import com.tmax.probus.dq.api.IDqSession;
import com.tmax.probus.dq.nio.AbstractDqReactor;


/**
 * The Class DqAcceptor.
 */
public class DqAcceptReactor extends AbstractDqReactor {
    /** Logger for this class. */
    private final transient Logger logger = Logger.getLogger("com.tmax.probus.dq.nio");
    /** The node_. */
    private final DqNode node_;
    /** The server channel_. */
    private final Map<SocketAddress, ServerSocketChannel> serverChannels_;

    /**
     * Instantiates a new dq acceptor.
     * @param node the node
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public DqAcceptReactor(final DqNode node) throws IOException {
        node_ = node;
        serverChannels_ = new HashMap<SocketAddress, ServerSocketChannel>();
        //        bindAddress(new InetSocketAddress(node_.serverInfo().getHostAddress(), node_.serverInfo().getPort()));
    }

    /**
     * Bind address.
     * @param socketAddress the socket address
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public void bindAddress(final SocketAddress socketAddress) throws IOException {
        final ServerSocketChannel serverChannel = ServerSocketChannel.open();
        serverChannel.configureBlocking(false);
        addPendingJob(new Runnable() {
            @Override public void run() {
                try {
                    serverChannel.socket().bind(socketAddress);
                    register(serverChannel, SelectionKey.OP_ACCEPT);
                } catch (final IOException ex) {
                    logger.log(WARNING, "" + ex.getMessage(), ex);
                }
            }
        });
        serverChannels_.put(socketAddress, serverChannel);
        wakeupSelector();
    }

    /**
     * Gets the node.
     * @return the node
     */
    public DqNode getNode() {
        return node_;
    }

    /**
     * Inits the socket.
     * @param socket the socket
     */
    @Override public void initSocket(final Socket socket) {
    }

    /**
     * Unbind address.
     * @param address the address
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public void unbindAddress(final SocketAddress address) throws IOException {
        final ServerSocketChannel serverSocketChannel = serverChannels_.remove(address);
        if (serverSocketChannel != null) {
            serverSocketChannel.socket().close();
            serverSocketChannel.close();
        }
        addPendingJob(new Runnable() {
            @Override public void run() {
                final SelectionKey key = serverSocketChannel.keyFor(getSelector());
                if (key != null) key.cancel();
            }
        });
        wakeupSelector();
    }

    /**
     * Creates the session.
     * @return the i dq session
     */
    @Override protected IDqSession createSession() {
        return null;
    }

    // (non-Javadoc)
    // @see com.tmax.probus.dq.nio.AbstractDqReactor#getIoReactor()
    @Override protected IDqReactor getIoReactor() {
        return getNode().serverInfo().getIoReactor();
    }

    // (non-Javadoc)
    // @see com.tmax.probus.dq.nio.DqReactorBase#getSelectTimeout()
    @Override protected long getSelectTimeout() {
        return getNode().serverInfo().getSelectTimeout();
    }

    // (non-Javadoc)
    // @see com.tmax.probus.dq.api.IDqReactor#createSelectionHandler()
    @Override public IDqReactorHandler createSelectionHandler() {
        return new AcceptHandler();
    }

    /**
     * The Class AcceptHandler.
     */
    public class AcceptHandler implements IDqReactorHandler {
        // (non-Javadoc)
        // @see com.tmax.probus.dq.api.IDqReactor.IDqReactorHandler#handleAccept(java.nio.channels.SelectionKey)
        @Override public void handleAccept(SelectionKey key) {
            try {
                if (!key.isValid() || !key.isAcceptable()) return;
                final SocketChannel channel = ((ServerSocketChannel) key.channel()).accept();
                final boolean success = channel.finishConnect();
                if (success) {
                    IDqSession session = null;
                    if (key.attachment() == null) {
                        session = createSession();
                        key.attach(session);
                    }
                    session = (IDqSession) key.attachment();
                    final IDqSession fSession = session;
                    initSocket(channel.socket());
                    final IDqReactor reactor = getIoReactor();
                    reactor.addPendingJob(new Runnable() {
                        @Override public void run() {
                            try {
                                reactor.register(channel, SelectionKey.OP_READ, fSession);
                            } catch (final IOException ex) {
                                logger.log(WARNING, "" + ex.getMessage(), ex);
                            }
                        }
                    });
                    reactor.wakeupSelector();
                }
            } catch (final IOException ex) {
                logger.log(WARNING, "" + ex.getMessage(), ex);
            }
        }

        // (non-Javadoc)
        // @see com.tmax.probus.dq.api.IDqReactor.IDqReactorHandler#handleWrite(java.nio.channels.SelectionKey)
        @Override public void handleWrite(SelectionKey key) {
            if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "handleWrite");
            // XXX must do something
            if (logger.isLoggable(FINER)) logger.exiting(getClass().getName(), "handleWrite");
        }

        // (non-Javadoc)
        // @see com.tmax.probus.dq.api.IDqReactor.IDqReactorHandler#handleRead(java.nio.channels.SelectionKey)
        @Override public void handleRead(SelectionKey key) {
            if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "handleRead");
            // XXX must do something
            if (logger.isLoggable(FINER)) logger.exiting(getClass().getName(), "handleRead");
        }

        // (non-Javadoc)
        // @see com.tmax.probus.dq.api.IDqReactor.IDqReactorHandler#handleConnect(java.nio.channels.SelectionKey)
        @Override public void handleConnect(SelectionKey key) {
            if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "handleConnect");
            // XXX must do something
            if (logger.isLoggable(FINER)) logger.exiting(getClass().getName(), "handleConnect");
        }
    }
}
