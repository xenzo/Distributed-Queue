/*
 * IDqReactor.java Version 1.0 Jan 27, 2012
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
package com.tmax.probus.dq.api;


import java.io.IOException;
import java.net.Socket;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;


/**
 * The Interface IDqReactor.
 */
public interface IDqReactor extends Runnable {
    /**
     * Adds the pending job.
     * @param job the job
     */
    void addPendingJob(final Runnable job);

    /**
     * Creates the selection handler.
     * @return the i dq reactor handler
     */
    IDqReactorHandler createSelectionHandler();

    /**
     * Inits the socket.
     * @param socket the socket
     */
    void initSocket(final Socket socket);

    /**
     * Checks if is live.
     * @return true, if is live
     */
    boolean isLive();

    /**
     * Process pending jobs.
     */
    void processPendingJobs();

    /**
     * Register.
     * @param aChannel the a channel
     * @param aOp the a op
     * @throws IOException Signals that an I/O exception has occurred.
     */
    void register(final SelectableChannel aChannel, final int aOp) throws IOException;

    /**
     * Register.
     * @param aChannel the a channel
     * @param aOp the a op
     * @param aAttachment the a attachment
     * @throws IOException Signals that an I/O exception has occurred.
     */
    void register(final SelectableChannel aChannel, final int aOp, final Object aAttachment)
            throws IOException;

    /**
     * Sets the live.
     * @param live the new live
     */
    void setLive(final boolean live);

    /**
     * Shutdown.
     */
    void shutdown();

    /**
     * Wakeup selector.
     */
    void wakeupSelector();

    /**
     * The Interface IDqReactorHandler.
     */
    public static interface IDqReactorHandler {
        /**
         * Handle accept.
         * @param key the key
         */
        void handleAccept(SelectionKey key);

        /**
         * Handle connect.
         * @param key the key
         */
        void handleConnect(SelectionKey key);

        /**
         * Handle read.
         * @param key the key
         */
        void handleRead(SelectionKey key);

        /**
         * Handle write.
         * @param key the key
         */
        void handleWrite(SelectionKey key);
    }
}
