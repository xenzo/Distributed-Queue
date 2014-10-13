/*
 * TestMMB.java Version 1.0 May 8, 2014
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


import static java.util.logging.Level.*;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel.MapMode;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;


/**
 *
 */
public class TestMMB {
        /** Logger for this class */
        private final static Logger logger = Logger.getLogger("com.tmaxsoft.core");

        public static void main(String... args) {
                int cline = 64;
                String file = "/Users/OnlyUno/Desktop/tmp.dat";
                MappedByteBuffer buffer = null;
                try {
                        RandomAccessFile f = new RandomAccessFile(file, "rw");
                        buffer = f.getChannel().map(MapMode.READ_WRITE, 0, cline);
                } catch (IOException ex) {
                        logger.log(WARNING, "" + ex.getMessage(), ex);
                }
                for (int i = 0; i < 10; i++) {
                        System.out.println(">>\t" + Integer.bitCount(i));
                }
                logger.info("" + buffer.getLong(0));
                info(buffer);
                buffer.putLong(0, 4L);
                buffer.position(6);
                buffer.limit(33);
                info(buffer);
                buffer.clear();
                info(buffer);
                logger.info("" + buffer.getLong(0));
                info(buffer);
                ExecutorService pool = Executors.newCachedThreadPool();
                AtomicBoolean b = new AtomicBoolean(true);
                pool.execute(new T("1", b));
                pool.execute(new T("2", b));
                pool.execute(new T("3", b));
                pool.execute(new T("4", b));
                pool.execute(new T("5", b));
                pool.execute(new T("6", b));
                pool.execute(new T("7", b));
                pool.execute(new T("8", b));
                pool.execute(new T("9", b));
                pool.execute(new T("10", b));
                pool.execute(new T("11", b));
                pool.execute(new T("13", b));
                pool.execute(new T("14", b));
                pool.execute(new T("15", b));
                pool.execute(new T("16", b));
                pool.execute(new T("17", b));
                pool.execute(new T("18", b));
                pool.execute(new T("19", b));
                pool.execute(new T("20", b));
                pool.execute(new T("21", b));
                pool.execute(new T("22", b));
                pool.execute(new T("23", b));
                pool.execute(new T("24", b));
                pool.execute(new T("25", b));
                pool.execute(new T("26", b));
                pool.execute(new T("27", b));
                pool.execute(new T("28", b));
                pool.execute(new T("29", b));
                pool.execute(new T("30", b));
                pool.execute(new T("31", b));
                pool.execute(new T("32", b));
                pool.execute(new T("33", b));
                pool.execute(new T("34", b));
                pool.execute(new T("35", b));
                pool.execute(new T("36", b));
                pool.execute(new T("37", b));
                pool.execute(new T("38", b));
                try {
                        Thread.sleep(5000);
                } catch (InterruptedException ex) {
                        logger.log(WARNING, "" + ex.getMessage(), ex);
                }
                b.set(false);
                try {
                        Thread.sleep(5000);
                } catch (InterruptedException ex) {
                        logger.log(WARNING, "" + ex.getMessage(), ex);
                }
        }

        private static void info(ByteBuffer buffer) {
                StringBuffer b = new StringBuffer();
                b.append("position: " + buffer.position()).append(", limit: " + buffer.limit() + ", capa: " + buffer.capacity());
                logger.info(b.toString());
        }

        static class T implements Runnable {
                private String _id;
                private AtomicBoolean _b;

                /**
                 *
                 */
                public T(String id, AtomicBoolean b) {
                        _id = id;
                        _b = b;
                }

                /** @InheritDoc */
                @Override public void run() {
                        long sum = 0L;
                        long base = System.nanoTime();
                        while (_b.get()) {
                                sum += System.nanoTime() - base;
                        }
                        System.out.println("" + _id + " : " + sum);
                }
        }
}
//end TestMMB