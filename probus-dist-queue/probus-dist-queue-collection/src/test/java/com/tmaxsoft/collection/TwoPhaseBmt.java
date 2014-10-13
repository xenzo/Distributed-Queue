/*
 * TwoPhaseBmt.java Version 1.0 Apr 23, 2013
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
package com.tmaxsoft.collection;


import static java.util.logging.Level.*;

import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;


/**
 * The Class TwoPhaseBmt.
 */
public class TwoPhaseBmt {
        /** Logger for this class */
        private final transient Logger logger = Logger.getLogger("com.tmaxsoft.collection");
        TwoPhaseDeletionDequeSupport<String, Tmp> core = new TwoPhaseDeletionDequeSupport<String, Tmp>();
        ConcurrentSkipListMap<String, Tmp> bias = new ConcurrentSkipListMap<String, Tmp>();
        AtomicInteger c = new AtomicInteger(0);

        public TwoPhaseBmt() {
        }

        private final static int CNT = 300;
        CountDownLatch latch = new CountDownLatch(1);
        CountDownLatch end = new CountDownLatch(CNT);
        ExecutorService pool = Executors.newCachedThreadPool();

        public static void main(String[] args) {
                for (int i = 0; i < 5; i++) {
                        new TwoPhaseBmt().go2();
                }
                for (int i = 0; i < 5; i++) {
                        new TwoPhaseBmt().go();
                }
        }

        public void go() {
                for (int i = 0; i < CNT; i++) {
                        pool.execute(new Adder());
                }
                long start = System.currentTimeMillis();
                latch.countDown();
                try {
                        end.await();
                } catch (InterruptedException ex) {
                        logger.log(WARNING, "" + ex.getMessage(), ex);
                }
                long end = System.currentTimeMillis();
                System.out.println("go time : " + (end - start));
                System.out.println("go count : " + c.get());
                pool.shutdown();
        }

        public void go2() {
                for (int i = 0; i < CNT; i++) {
                        pool.execute(new Adder2());
                }
                long start = System.currentTimeMillis();
                latch.countDown();
                try {
                        end.await();
                } catch (InterruptedException ex) {
                        logger.log(WARNING, "" + ex.getMessage(), ex);
                }
                long end = System.currentTimeMillis();
                System.out.println("go2 time : " + (end - start));
                System.out.println("go2 count : " + c.get());
                pool.shutdown();
        }

        class Tmp implements IEntry<String> {
                String key;

                Tmp(String key) {
                        this.key = key;
                }

                /** @InheritDoc */
                @Override public String getKey() {
                        return key;
                }
        }

        class Adder implements Runnable {
                /** @InheritDoc */
                @Override public void run() {
                        try {
                                latch.await();
                                core.doOfferFirst(String.valueOf(c.incrementAndGet()), new Tmp(String.valueOf(c.get())));
                                end.countDown();
                        } catch (InterruptedException ex) {
                                logger.log(WARNING, "" + ex.getMessage(), ex);
                        }
                }
        }

        class Adder2 implements Runnable {
                /** @InheritDoc */
                @Override public void run() {
                        try {
                                latch.await();
                                String v = String.valueOf(c.incrementAndGet());
                                bias.putIfAbsent(v, new Tmp(v));
                                end.countDown();
                        } catch (InterruptedException ex) {
                                logger.log(WARNING, "" + ex.getMessage(), ex);
                        }
                }
        }
}
