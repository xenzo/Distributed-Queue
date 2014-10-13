/*
 * MappedBufferTest001.java Version 1.0 May 8, 2014
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


import static java.util.logging.Level.WARNING;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel.MapMode;
import java.util.logging.Logger;

import com.tmaxsoft.core.sample.dto.UnsafeBackedDto;

import sun.misc.Unsafe;
import sun.nio.ch.DirectBuffer;


/**
 *
 */
public class MappedBufferTest001 {
        /** Logger for this class */
        private final static Logger logger = Logger.getLogger("com.tmaxsoft.core");
        MappedByteBuffer buffer = null;
        Unsafe unsafe = null;
        long address = 0L;

        MappedBufferTest001(MappedByteBuffer buffer) {
                this.buffer = buffer;
                buffer.position(0);
                buffer.limit(48);
                final ByteBuffer b = buffer.slice();
                b.order(ByteOrder.nativeOrder());
                address = ((DirectBuffer) buffer).address();
                System.out.println("ADDR ==> " + address);
                try {
                        unsafe = getUnsafe();
                } catch (NoSuchFieldException e) {
                        e.printStackTrace();
                } catch (IllegalAccessException e) {
                        e.printStackTrace();
                }
                byte[] bytes = "abcdefghijklm".getBytes();
                b.put(bytes);
                byte[] byteTo = new byte[bytes.length + 10];
                int baseOffset = unsafe.arrayBaseOffset(byte[].class);
                unsafe.copyMemory(null, address, byteTo, baseOffset, byteTo.length);
                System.out.println("RES :::" + new String(byteTo));
                System.out.println("PAGE ::: " + unsafe.pageSize());
//                int freeChunk = findFreeChunk();
//                long pos = memStart + ((long)freeChunk * (long)chunkSize);
//                unsafe.copyMemory(chunk;, Unsafe.ARRAY_BYTE_BASE_OFFSET + offset, null, pos, chunkSize);
//                return freeChunk;
//                ByteBuffer[] array = new ByteBuffer[] { b };
//                
//                int addressSize = unsafe.addressSize();
//                unsafe.arrayIndexScale(getClass());
//                if (addressSize == 4) {
//                        int objectAddress = unsafe.getInt(b, baseOffset);
//                        System.out.println(">>>>><<<<<<" + (address - objectAddress));
//                } else if (addressSize == 8) {
//                        long objectAddress = unsafe.getLong(b, baseOffset);
//                        System.out.println(">>>>>><<<<<<" + (address - objectAddress));
//                        System.out.println("ADDOFF   " + objectAddress);
//                        System.out.println("BASE     " + baseOffset);
//                }
////                buffer.putLong(0, 0L);
//                System.out.println("VAL ===> " + b.getLong(0));
//                b.putLong(0, 2L);
//                System.out.println("VAL ===> " + unsafe.getLong(address));
                write(0L);
//                System.out.println("VAL ===> " + b.getLong(0));
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
                MappedBufferTest001 t = new MappedBufferTest001(buffer);
                long start = System.nanoTime();
                long val = t.read();
                while (val < max) {
                        while ((val = t.read()) % 2 == 0);
                        logger.info("val=" + val);
                        while (!t.cas(val, val + 1));
//                        t.write(val + 1);
                }
                long end = System.nanoTime();
                logger.info("1 gap=" + ((double) (end - start)) / max + "\taddress : " + t.address + "\tvalue : " + buffer.getLong(0));
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
//end MappedBufferTest001