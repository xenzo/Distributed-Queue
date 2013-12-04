package com.tmax.api;

/** @author xenzo */
public interface ILifeCycle {
    /** Reactor 종료. */
    void destroy();

    /** Reactor 시동. */
    void init();

    /** Start. */
    void start();

    /** Stop. */
    void stop();

    /** Checks if is running. */
    boolean isRunning();
}
