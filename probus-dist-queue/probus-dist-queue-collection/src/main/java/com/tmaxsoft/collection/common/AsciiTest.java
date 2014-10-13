/*
 * AsciiTest.java Version 1.0 May 14, 2014
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
package com.tmaxsoft.collection.common;


import static java.util.logging.Level.*;

import java.util.logging.Logger;


/**
 *
 */
public class AsciiTest {
        /** Logger for this class */
        private final transient Logger logger = Logger.getLogger("com.tmaxsoft.collection.common");

        /**
         * @param args
         */
        public static void main(String[] args) {
                byte base = 48;
                {
                        byte b[] = "1230".getBytes();
                        long l = 0L;
                        for (int i = 0; i < b.length; i++) {
                                byte c = b[i];
                                System.out.println("" + (c - base));
                                l = (l << 3) + (l << 1) + (c - base);
                        }
                        System.out.println("RESULT: " + l);
                }
                //                byte fb[] = "1230".getBytes();
//                byte fbase = 48;
//                double f = 0d;
//                for (int i = 0; i < b.length; i++) {
//                        byte fc = b[i];
//                        System.out.println("" + (fc - base));
//                        f = (f << 3) + (f << 1) + (fc - base);
//                }
//                System.out.println("RESULT: " + f);
                {
                        byte b[] = "0125.30".getBytes();
                        long l = 0L;
                        int p = 0;
                        int m = 1;
                        int n = 1;
                        boolean t = false;
                        for (int i = 0; i < b.length; i++) {
                                byte c = b[i];
                                if (c != '.') l = (l << 3) + (l << 1) + (c - base);
                                else t = true;
                                if (!t) m = (m << 3) + (m << 1);
                                n = (n << 3) + (n << 1);
                        }
                        for (int i = 0; i < p - 1; i++) {
                                m = (m << 3) + (m << 1);
                        }
                        System.out.println("l=" + l);
                        System.out.println("m=" + m);
                        System.out.println("n=" + n);
                        System.out.println("" + ((double) l * m / n));
                }
                byte k[] = new byte[32];
                byte[] t = "abc".getBytes();
                System.arraycopy(t, 0, k, 0, t.length);
                System.out.println(">>>>>>" + new String(k) + "<<<");
        }
}
//end AsciiTest