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
package com.tmaxsoft.probus.dq.collection;


import static java.util.logging.Level.*;
import static org.junit.Assert.*;

import java.util.Map;
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
    /** Logger for this class */
    private final transient Logger logger = Logger.getLogger("com.tmaxsoft.probus.dq.collection");
    private static IDqDeque<String, Elem> deque;
    private static IDqCollectionOperator<String, Elem> operator;

    @BeforeClass public static void startUp() {
    }

    @Before public void setUp() {
        Map<Object, IDqElement<Object>> map = DqCollections.convertTo(DqCollections.newInstance());
        //        deque = DqCollections.convertToBlockingDeque(DqCollections.newDqCollection("SAME", null, 0, new Comparator<String>() {
        //            @Override
        //            public int compare(String o1, String o2) {
        //                if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "compare");
        //                // XXX must do something
        //                if (logger.isLoggable(FINER)) logger.exiting(getClass().getName(), "compare");
        //                return 0;
        //            }
        //        }));
        //        operator = DqCollections.convert2SolidOperator(deque);
        //        deque.add(new Elem("A"));
        //        deque.add(new Elem("B"));
        //        deque.add(new Elem("C"));
        //        deque.add(new Elem("D"));
        map.put("KKK", "");
    }

    /**
     * Test method for
     * {@link com.tmaxsoft.probus.dq.collection.DqCollections#convert2SolidOperator(java.util.Collection)}
     * .
     */
    @Test public void testConvert2SolidOperator() {
    }

    @Test public void testPoll() {
        Elem poll = deque.poll();
        assertEquals("A", poll.getIdentifier());
        assertEquals(3, deque.realCount());
        assertEquals(4, operator.sizeSolidly());
        deque.poll();
        deque.poll();
        deque.poll();
        try {
            deque.poll();
            fail();
        } catch (Throwable t) {
        }
        assertEquals(0, deque.realCount());
        try {
            assertNull(deque.poll(1, TimeUnit.SECONDS));
        } catch (InterruptedException ex) {
            fail();
        }
        assertEquals(4, operator.sizeSolidly());
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
        assertEquals(7, deque.realCount());
        assertEquals(7, operator.sizeSolidly());
    }

    @Test public void testOffer() {
        assertFalse(deque.offer(new Elem("C")));
    }

    @Test public void testRemoveSolid() {
        operator.removeSolidly("C");
        assertEquals(3, deque.realCount());
        assertEquals(3, operator.sizeSolidly());
    }

    @Test public void testPollFirstLast() {
        operator.removeSolidly("A");
        assertEquals(3, deque.realCount());
        assertEquals(3, operator.sizeSolidly());
        //        assertEquals("B", operator.putSolidly("B", new Elem("B")).getIdentifier());
        assertEquals("D", deque.doPollLast().getIdentifier());
        assertEquals("B", deque.doPollFirst().getIdentifier());
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
                        System.out.println(">> " + deque.takeFirst().getIdentifier());
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
                        System.out.println("\t<< " + deque.takeLast().getIdentifier());
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
        assertEquals(4, deque.realCount());
        assertEquals(5004, operator.sizeSolidly());
    }

    private static class Elem implements IDqElement<String> {
        private final String id_;

        /**
         *
         */
        public Elem(String id) {
            id_ = id;
        }

        // (non-Javadoc)
        // @see com.tmaxsoft.probus.dq.collection.IDqElement#getIdentifier()
        @Override public String getIdentifier() {
            return id_;
        }
    }
}
