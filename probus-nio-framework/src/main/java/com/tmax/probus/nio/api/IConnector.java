package com.tmax.probus.nio.api;


import java.net.InetAddress;
import java.net.InetSocketAddress;


/**
 * @author xenzo
 */
public interface IConnector extends IReactor {
    /**
     * Connect.
     * @param remoteAddr the remote addr
     * @param localAddr the local addr
     * @return the i session
     */
    ISession connectToServer(InetSocketAddress remoteAddr, InetAddress localAddr);
}
