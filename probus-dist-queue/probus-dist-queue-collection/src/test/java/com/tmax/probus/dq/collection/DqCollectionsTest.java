/*
 * DqCollectionsTest.java Version 1.0 Feb 3, 2012
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
package com.tmax.probus.dq.collection;


import static java.util.logging.Level.*;
import static org.junit.Assert.*;

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;


/**
 *
 */
public class DqCollectionsTest {
    /**
     * Logger for this class
     */
    private final transient Logger logger = Logger.getLogger("com.tmax.probus.dq.collection");
    static BlockingDeque<Elem> deque;
    static IDqSolidOperator<String, Elem> operator;

    @BeforeClass public static void startUp() {
    }

    @Before public void setUp() {
        deque = DqCollections.newBlockingDeque("SAME");
        operator = DqCollections.convert2SolidOperator(deque);
        deque.add(new Elem("A"));
        deque.add(new Elem("B"));
        deque.add(new Elem("C"));
        deque.add(new Elem("D"));
    }

    /**
     * Test method for
     * {@link com.tmax.probus.dq.collection.DqCollections#convert2SolidOperator(java.util.Collection)}
     * .
     */
    @Test public void testConvert2SolidOperator() {
    }

    @Test public void testPoll() {
        Elem poll = deque.poll();
        assertEquals("A", poll.getId());
        assertEquals(3, deque.size());
        assertEquals(4, operator.fullSize());
        deque.poll();
        deque.poll();
        deque.poll();
        try {
            deque.poll();
            fail();
        } catch (Throwable t) {
        }
        assertEquals(0, deque.size());
        try {
            assertNull(deque.poll(1, TimeUnit.SECONDS));
        } catch (InterruptedException ex) {
            fail();
        }
        assertEquals(4, operator.fullSize());
    }

    @Test public void testAdd() {
        try {
            deque.add(new Elem("C"));
            fail();
        } catch (Throwable t) {
        }
        deque.add(new Elem("E"));
        deque.add(new Elem("F"));
        deque.add(new Elem("G"));
        assertEquals(7, deque.size());
        assertEquals(7, operator.fullSize());
    }

    @Test public void testOffer() {
        assertFalse(deque.offer(new Elem("C")));
    }

    @Test public void testRemoveSolid() {
        operator.removeSolidly("C");
        assertEquals(3, deque.size());
        assertEquals(3, operator.fullSize());
    }

    @Test public void testPollFirstLast() {
        operator.removeSolidly("A");
        assertEquals(3, deque.size());
        assertEquals(3, operator.fullSize());
        assertEquals("B", operator.putSolidly("B", new Elem("B")).getId());
        assertEquals("D", deque.pollLast().getId());
        assertEquals("B", deque.pollFirst().getId());
    }

    @Test public void testConcurrency() {
        final CountDownLatch l = new CountDownLatch(1);
        final AtomicInteger a = new AtomicInteger(0);
        ExecutorService ec = Executors.newFixedThreadPool(5);
        ExecutorService ed = Executors.newFixedThreadPool(5);
        for (int i = 0; i < 5000; i++) {
            ec.submit(new Runnable() {
                @Override public void run() {
                    try {
                        l.await();
                    } catch (InterruptedException ex) {
                        fail();
                    }
                    deque.offer(new Elem(Integer.toString(a.getAndIncrement())));
                }
            });
        }
        for (int i = 0; i < 2500; i++) {
            ed.submit(new Runnable() {
                @Override public void run() {
                    try {
                        l.await();
                        System.out.println(">> " + deque.takeFirst().getId());
                    } catch (InterruptedException ex) {
                        fail();
                    }
                }
            });
        }
        for (int i = 0; i < 2500; i++) {
            ed.submit(new Runnable() {
                @Override public void run() {
                    try {
                        l.await();
                        System.out.println("\t<< " + deque.takeLast().getId());
                    } catch (InterruptedException ex) {
                        fail();
                    }
                }
            });
        }
        l.countDown();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            logger.log(WARNING, "" + ex.getMessage(), ex);
        }
        assertEquals(5000, a.intValue());
        assertEquals(4, deque.size());
        assertEquals(5004, operator.fullSize());
    }

    static class Elem implements IDqElement<String> {
        private String id_;

        /**
         *
         */
        public Elem(String id) {
            id_ = id;
        }

        // (non-Javadoc)
        // @see com.tmax.probus.dq.collection.IDqElement#getId()
        @Override public String getId() {
            return id_;
        }
    }
}
