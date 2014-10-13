/*
 * PlainInnerDto.java Version 1.0 Apr 24, 2014
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
package com.tmaxsoft.core.sample.dto;

/**
 *
 */
public class PlainInnerDto implements IInnerDto {
        long longData;
        int intData;
        boolean boolData;
        char charData;
        String stringData;

        /**
         * @return the longData
         */
        @Override public long getLongData() {
                return longData;
        }

        /**
         * @param longData the longData to set
         */
        @Override public void setLongData(final long longData) {
                this.longData = longData;
        }

        /**
         * @return the intData
         */
        @Override public int getIntData() {
                return intData;
        }

        /**
         * @param intData the intData to set
         */
        @Override public void setIntData(final int intData) {
                this.intData = intData;
        }

        /**
         * @return the boolData
         */
        @Override public boolean isBoolData() {
                return boolData;
        }

        /**
         * @param boolData the boolData to set
         */
        @Override public void setBoolData(final boolean boolData) {
                this.boolData = boolData;
        }

        /**
         * @return the charData
         */
        @Override public char getCharData() {
                return charData;
        }

        /**
         * @param charData the charData to set
         */
        @Override public void setCharData(final char charData) {
                this.charData = charData;
        }

        /** @InheritDoc */
        @Override public String getStringData() {
                return stringData;
        }

        /** @InheritDoc */
        @Override public void setStringData(String stringData) {
                this.stringData = stringData;
        }
}
//end PlainInnerDto