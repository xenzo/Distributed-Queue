/*
 * Main.java Version 1.0 Apr 24, 2014
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
import static java.util.logging.Level.*;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.tmaxsoft.core.ByteBufferStore;
import com.tmaxsoft.core.sample.dto.ByteBufferBackedDto;
import com.tmaxsoft.core.sample.dto.IBacked;
import com.tmaxsoft.core.sample.dto.IDto;
import com.tmaxsoft.core.sample.dto.IInnerDto;
import com.tmaxsoft.core.sample.dto.PlainDto;
import com.tmaxsoft.core.sample.dto.UnsafeBackedDto;


/**
 *
 */
public class Main {
        /** Logger for this class */
        private static final transient Logger logger = Logger.getLogger("");

        public Main() {
        }

        public void perfTest() throws InterruptedException {
                ByteBufferStore store = new ByteBufferStore("/Users/OnlyUno/Desktop/Test.dat", 500 * 1024 * 1024, 64);
                final int max = 10;
                int imax = 0;
                final int sleep_time = 5 * 1000;
                logger.setLevel(Level.INFO);
                Handler[] handlers = logger.getHandlers();
                for (int i = 0; i < handlers.length; i++) {
                        Handler handler = handlers[i];
                        handler.setLevel(INFO);
                }
                IBacked dto = null;
                IDto dto1 = null;
                PlainDto[] list = new PlainDto[250000];
                for (int b = 0; b < 5; b++) {
                        imax = (b + 1) * 50000;
                        Thread.sleep(sleep_time);
                        System.gc();
                        logger.info("GC");
                        Thread.sleep(sleep_time);
                        logger.info("\t\t\t\t\t\t\t\t\t>>>>>>>>>>>>>>>>>>>>>> DTO count " + (imax /* max*/) + " / Loop count " + max + " / Total count " + imax * max + " <<<<<<<<<<<<<<<<<<<<<<<");
                        dto = new ByteBufferBackedDto(null);
                        long start = System.currentTimeMillis();
                        for (int a = 0; a < max; a++) {
                                for (int i = 0; i < imax; i++) {
                                        dto.setData(store.get(i));
                                        runSetLoop(dto, i);
                                }
                                for (int i = 0; i < imax; i++) {
                                        dto.setData(store.get(i));
                                        runGetLoop(dto);
                                }
                        }
                        long end = System.currentTimeMillis();
                        printTimeInfo("DirectBuffer", max, imax, start, end);
                        Thread.sleep(sleep_time);
                        System.gc();
                        logger.info("GC");
                        Thread.sleep(sleep_time);
                        dto = new UnsafeBackedDto(null);
                        start = System.currentTimeMillis();
                        for (int a = 0; a < max; a++) {
                                for (int i = 0; i < imax; i++) {
                                        dto.setData(store.get(i));
                                        runSetLoop(dto, i);
                                }
                                for (int i = 0; i < imax; i++) {
                                        dto.setData(store.get(i));
                                        runGetLoop(dto);
                                }
                        }
                        end = System.currentTimeMillis();
                        printTimeInfo("Unsafe", max, imax, start, end);
                        Thread.sleep(sleep_time);
                        System.gc();
                        logger.info("GC");
                        Thread.sleep(sleep_time);
                        start = System.currentTimeMillis();
                        for (int a = 0; a < max; a++) {
                                for (int i = 0; i < imax; i++) {
                                        dto1 = (list[i] = new PlainDto());
                                        runSetLoop(dto1, i);
                                }
                                for (int i = 0; i < imax; i++) {
                                        dto1 = list[i];
                                        runGetLoop(dto1);
                                }
                        }
                        end = System.currentTimeMillis();
                        printTimeInfo("new Instance", max, imax, start, end);
                        Thread.sleep(sleep_time);
                        System.gc();
                        logger.info("GC");
                        Thread.sleep(sleep_time);
                        start = System.currentTimeMillis();
                        for (int a = 0; a < max; a++) {
                                for (int i = 0; i < imax; i++) {
                                        dto1 = list[i];
                                        runSetLoop(dto1, i);
                                }
                                for (int i = 0; i < imax; i++) {
                                        dto1 = list[i];
                                        runGetLoop(dto1);
                                }
                        }
                        end = System.currentTimeMillis();
                        printTimeInfo("reuse Instance", max, imax, start, end);
                }
        }

        /**
         * @param args
         */
        public static void main(String[] args) {
                Main main = new Main();
                try {
                        Thread.sleep(5000);
                        main.perfTest();
                } catch (InterruptedException e) {
                        e.printStackTrace();
                }
        }

        /**
         * @param max
         * @param imax
         * @param start
         * @param end
         */
        private void printTimeInfo(String name, final int max, int imax, long start, long end) {
                long gap = end - start;
                long tps = imax * max / gap * 1000;
                logger.info("\t\t\t\t\t\t\t\t\t\t\t\t" + (name.length() > 8 ? name : name + "\t")
                                + "\t >>>>>>> " + gap + "ms\t ::: " + tps + " tps");
        }

        /**
         * @param dto
         * @param totLong
         * @param totDouble
         * @param totInt
         * @param totBool
         */
        private void runGetLoop(IDto dto) {
                long totLong = 0L;
                int totInt = 0;
                boolean totBool = false;
                double totDouble = 0d;
                double d = dto.getDoubleData();
                long l = dto.getLongData();
                int k = dto.getIntData();
                boolean bd = dto.isBoolData();
                char c = dto.getCharData();
                String s = dto.getStringData();
                for (int j = 0; j < 100; j++) {
                        IInnerDto innerDto = dto.get(j);
                        totDouble += d;
                        totLong += l * innerDto.getLongData();
                        totInt += k * innerDto.getIntData();
                        totBool ^= (bd & innerDto.isBoolData());
                }
                logger.fine(s + "\t : totLong=" + totLong + ", totDouble" + totDouble + ", totInt=" + totInt + ", totBool=" + totBool + " , char=" + c);
        }

        /**
         * @param dto
         * @param i
         */
        private void runSetLoop(IDto dto, int i) {
                dto.setDoubleData(i);
                dto.setLongData(i);
                dto.setIntData(i);
                dto.setBoolData(i % 2 == 0);
                dto.setCharData((char) i);
                dto.setStringData("abcedfghijk");
                for (int j = 0; j < 100; j++) {
                        IInnerDto innerDto = dto.get(j);
                        innerDto.setLongData(j);
                        innerDto.setIntData(j);
                        innerDto.setBoolData(j % 2 == 0);
                        innerDto.setCharData(Character.forDigit(j, 10));
                }
        }
}
//end Main
