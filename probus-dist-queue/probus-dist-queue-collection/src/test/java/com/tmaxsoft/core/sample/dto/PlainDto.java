/*
 * PlainDto.java Version 1.0 Apr 24, 2014
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
public class PlainDto implements IDto {
        double doubleData;
        long longData;
        int intData;
        boolean boolData;
        char charData;
        PlainInnerDto[] _inner = new PlainInnerDto[100];
        private String stringData;

        /**
         *
         */
        public PlainDto() {
                for (int i = 0; i < 100; i++) {
                        _inner[i] = new PlainInnerDto();
                }
        }

        @Override public IInnerDto get(final int seq) {
                return _inner[seq];
        }

        /**
         * @return the doubleData
         */
        @Override public double getDoubleData() {
                return doubleData;
        }

        /**
         * @param doubleData the doubleData to set
         */
        @Override public void setDoubleData(final double doubleData) {
                this.doubleData = doubleData;
        }

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

        /**
         * @param string
         */
        @Override public void setStringData(final String string) {
                stringData = string;
        }

        /**
         * @return
         */
        @Override public String getStringData() {
                return stringData;
        }
}
//end PlainDto