/*
 * TwoPhaseDequeSupportTest.java Version 1.0 Apr 23, 2013
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


import static org.junit.Assert.*;
import static java.util.logging.Level.*;

import java.util.logging.Logger;

import org.junit.BeforeClass;
import org.junit.Test;


/**
 * The Class TwoPhaseDequeSupportTest.
 */
public class TwoPhaseDeletionDequeSupportTest {
    static TwoPhaseDeletionDequeSupport<String, Tmp> core;
    /** Logger for this class */
    private final transient Logger logger = Logger.getLogger("com.tmaxsoft.collection");

    @BeforeClass public static void setUp() throws Exception {
        core = new TwoPhaseDeletionDequeSupport<String, Tmp>(6);
    }

    @Test public void testDoOfferFirst() throws Exception {
        if (logger.isLoggable(FINER)) logger.entering(getClass().getName(), "testDoOfferFirst");
        assertTrue(core.doOfferFirst("3", new Tmp("3")));
        assertTrue(core.doOfferFirst("2", new Tmp("2")));
        assertTrue(core.doOfferFirst("1", new Tmp("1")));
        assertEquals(3, core.realCount());
        assertFalse(core.doOfferFirst("3", new Tmp("3")));//중복 불가
        assertEquals(3, core.realCount());
    }

    @Test public void testDoOfferLast() throws Exception {
        assertTrue(core.doOfferLast("4", new Tmp("4")));
        assertTrue(core.doOfferLast("5", new Tmp("5")));
        assertTrue(core.doOfferLast("6", new Tmp("6")));
        assertEquals(6, core.realCount());
        assertFalse(core.doOfferLast("6", new Tmp("6")));
        assertEquals(6, core.realCount());
        assertTrue(core.isFull());
    }

    @Test public void testDoPollFirst() throws Exception {
        assertEquals("1", core.doPollFirst().getKey());
        assertEquals("2", core.doPollFirst().getKey());
        assertEquals("3", core.doPollFirst().getKey());
        assertEquals(3, core.realCount());
        assertEquals(6, core.totalCount());
    }

    @Test public void testDoPollLast() throws Exception {
        assertEquals("6", core.doPollLast().getKey());
        assertEquals("5", core.doPollLast().getKey());
        assertEquals("4", core.doPollLast().getKey());
        assertEquals(0, core.realCount());
        assertTrue(core.isEmpty());
        assertFalse(core.isFull());
        assertEquals(6, core.totalCount());
    }

    @Test public void testDoGetSolidly() throws Exception {
        assertEquals("1", core.doGetSolidly("1").getKey());
        assertEquals("2", core.doGetSolidly("2").getKey());
        assertEquals("3", core.doGetSolidly("3").getKey());
        assertEquals("4", core.doGetSolidly("4").getKey());
        assertEquals("5", core.doGetSolidly("5").getKey());
        assertEquals("6", core.doGetSolidly("6").getKey());
    }

    @Test public void testDoRemoveSolidily() throws Exception {
        assertEquals("1", core.doRemoveSolidily("1").getKey());
        assertEquals(5, core.totalCount());
        assertNull(core.doRemoveSolidily("1"));
        assertEquals("2", core.doRemoveSolidily("2").getKey());
        assertEquals("3", core.doRemoveSolidily("3").getKey());
        assertEquals("4", core.doRemoveSolidily("4").getKey());
        assertEquals("5", core.doRemoveSolidily("5").getKey());
        assertEquals("6", core.doRemoveSolidily("6").getKey());
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
}
