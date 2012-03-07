package com.tmax.probus.nio.api;

/**
 * The Interface IAcceptor.
 * @author xenzo
 */
public interface IAcceptor extends IReactor {
    /**
     * Close server.
     * @param port the port
     */
    void closeServer(int port);

    /**
     * Unbind.
     * @param localAddr the local addr
     */
    void closeServer(String ip, int port);

    /**
     * Open server.
     * @param port the port
     */
    void openServer(int port);

    /**
     * Bind.
     * @param ip the ip
     * @param port the port
     */
    void openServer(String ip, int port);
}
