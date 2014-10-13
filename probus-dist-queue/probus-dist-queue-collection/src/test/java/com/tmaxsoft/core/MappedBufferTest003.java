/*
 * MappedBufferTest002.java Version 1.0 May 8, 2014
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
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel.MapMode;
import java.util.logging.Logger;

import sun.misc.Unsafe;
import sun.nio.ch.DirectBuffer;


/**
 *
 */
public class MappedBufferTest003 {
        /** Logger for this class */
        private final static Logger logger = Logger.getLogger("com.tmaxsoft.core");
        volatile MappedByteBuffer buffer = null;
        Unsafe unsafe = null;
        long address = 0L;

        MappedBufferTest003(MappedByteBuffer buffer) {
                this.buffer = buffer;
                address = ((DirectBuffer) buffer).address();
                try {
                        unsafe = getUnsafe();
                } catch (NoSuchFieldException e) {
                        e.printStackTrace();
                } catch (IllegalAccessException e) {
                        e.printStackTrace();
                }
        }

//        volatile static long val = 0L;
        public static void main(String[] args) {
                int cline = 64;
                String file = "/Users/OnlyUno/Desktop/tmp.dat";
                MappedByteBuffer buffer = null;
                try {
                        RandomAccessFile f = new RandomAccessFile(file, "rw");
                        buffer = f.getChannel().map(MapMode.READ_WRITE, 0, cline);
                } catch (IOException ex) {
                        logger.log(WARNING, "" + ex.getMessage(), ex);
                }
                final long max = 500000;
                buffer.order(ByteOrder.nativeOrder());
                MappedBufferTest003 t = new MappedBufferTest003(buffer);
                try {
                        Thread.sleep(100);
                } catch (InterruptedException ex) {
                        logger.log(WARNING, "" + ex.getMessage(), ex);
                }
                long start = System.nanoTime();
                long val = t.read();
                while (val < max) {
                        while ((val = t.read()) % 2 == 1);
                        logger.info("val=" + val);
                        t.write(val + 1);
//                        t.write(val + 1);
                }
                long end = System.nanoTime();
                logger.info("2 gap=" + ((double) (end - start)) / max + "\taddress : " + t.address + "\tvalue : " + buffer.getLong(0));
        }

        /**
         * @param l
         */
        private void write(long l) {
                unsafe.putLong(address, l);
        }

        private boolean cas(long old, long val) {
                return unsafe.compareAndSwapLong(null, address, old, val);
        }

        /**
         * @return
         */
        private long read() {
                return unsafe.getLong(address);
        }

        public static Unsafe getUnsafe() throws NoSuchFieldException, IllegalAccessException {
                Field f = Unsafe.class.getDeclaredField("theUnsafe"); //Internal reference
                f.setAccessible(true);
                return (Unsafe) f.get(null);
        }
}
//end MappedBufferTest002