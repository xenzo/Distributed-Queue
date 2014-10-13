/*
 * TestInheitence.java Version 1.0 May 21, 2014
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
import java.util.logging.Logger;


/**
 *
 */
public class TestInheitence {
        /** Logger for this class */
        private final transient Logger logger = Logger.getLogger("");

        @SuppressWarnings("null") public static void main(String... args) {
                TestChild c = null;
                B l = c.getL();
        }
}


interface Test {
        A getL();
}


interface TestChild extends Test {
        B getL();
}


interface A {
}


interface B extends A {
}
//end TestInheitence