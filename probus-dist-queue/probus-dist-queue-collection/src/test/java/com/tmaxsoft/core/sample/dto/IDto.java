/*
 * IDto.java Version 1.0 May 26, 2014
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
public interface IDto {
        /**
         * Gets the.
         * @param seq the seq
         * @return the i inner dto
         */
        public IInnerDto get(int seq);

        /**
         * @return the longData
         */
        long getLongData();

        /**
         * @param longData the longData to set
         */
        void setLongData(long longData);

        /**
         * @param doubleData the doubleData to set
         */
        void setDoubleData(double doubleData);

        /**
         * @return the intData
         */
        int getIntData();

        /**
         * @param intData the intData to set
         */
        void setIntData(int intData);

        /**
         * @return the boolData
         */
        boolean isBoolData();

        /**
         * @param boolData the boolData to set
         */
        void setBoolData(boolean boolData);

        /**
         * @return the charData
         */
        char getCharData();

        /**
         * @param charData the charData to set
         */
        void setCharData(char charData);

        /**
         * Gets the string data.
         * @return the string data
         */
        String getStringData();

        /**
         * Sets the string data.
         * @param strData the new string data
         */
        void setStringData(String strData);

        /**
         * Gets the double data.
         * @return the double data
         */
        double getDoubleData();
}//end IDto
