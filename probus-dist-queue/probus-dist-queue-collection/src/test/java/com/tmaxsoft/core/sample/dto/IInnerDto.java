/*
 * IInnerDto.java Version 1.0 May 26, 2014
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
public interface IInnerDto {
        /**
         * @return the longData
         */
        long getLongData();

        /**
         * @param longData the longData to set
         */
        void setLongData(long longData);

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

        String getStringData();

        void setStringData(String stringData);
}//end IInnerDto
