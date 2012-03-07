package com.tmax.probus.nio.api;

/**
 * The Interface IConnector.
 */
public interface IConnector extends IReactor {
    /**
     * Connect to server.
     * @param remoteIp the remote ip
     * @param remotePort the remote port
     * @param localIp the local ip
     * @return the i session
     */
    ISession connectToServer(String remoteIp, int remotePort, String localIp, int localPort);

    /**
     * Connect to server.
     * @param remoteIp the remote ip
     * @param remotePort the remote port
     * @return the i session
     */
    ISession connectToServer(String remoteIp, int remotePort, String localIp);

    /**
     * Connect to server.
     * @param remoteIp the remote ip
     * @param remotePort the remote port
     * @return the i session
     */
    ISession connectToServer(String remoteIp, int remotePort);
}
