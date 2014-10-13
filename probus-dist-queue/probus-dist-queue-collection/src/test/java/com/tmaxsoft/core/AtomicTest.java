/*
 * AtomicTest.java Version 1.0 May 8, 2014
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
package com.tmaxsoft.core;


import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;


/**
 *
 */
public class AtomicTest implements Runnable {
        /** Logger for this class */
        private final transient Logger logger = Logger.getLogger("com.tmaxsoft.core");
        volatile static AtomicLong a = new AtomicLong(0);
        final int seed;

        AtomicTest(int seed) {
                this.seed = seed;
        }

        /**
         * @param args
         */
        public static void main(String[] args) {
                AtomicTest t1 = new AtomicTest(1);
                AtomicTest t2 = new AtomicTest(0);
                new Thread(t1).start();
                new Thread(t2).start();
        }

        /** @InheritDoc */
        @Override public void run() {
                long max = 500000;
                long start = System.nanoTime();
                long val = read();
                while (val < max) {
                        while ((val = read()) % 2 == seed);
                        logger.info("val=" + val);
                        while (!cas(val, val + 1));
                }
                long end = System.nanoTime();
                logger.info(seed + " gap=" + ((double) (end - start)) / max);
        }

        private long read() {
                return a.get();
        }

        private boolean cas(long old, long val) {
                return a.compareAndSet(old, val);
        }
}
//end AtomicTest