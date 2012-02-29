package com.tmax.probus.nio.api;


import java.net.InetSocketAddress;


/**
 * @author xenzo
 */
public interface IAcceptor extends IReactor {
    /**
     * Bind.
     * @param localAddr the local addr
     */
    void openServer(String ip, int port);

    /**
     * Unbind.
     * @param localAddr the local addr
     */
    void closeServer(InetSocketAddress localAddr);
}
