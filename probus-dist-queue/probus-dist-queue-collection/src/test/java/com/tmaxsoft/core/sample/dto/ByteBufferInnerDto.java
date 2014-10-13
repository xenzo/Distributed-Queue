/*
 * InnerDto.java Version 1.0 Apr 24, 2014
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


import java.nio.ByteBuffer;


public class ByteBufferInnerDto implements IBackedInner {
        public static final int SIZE() {
                return 18;
        }

        public static final int TOTAL_SIZE() {
                return 18;
        }

        private long _offset;
        private ByteBuffer _data;

        public ByteBufferInnerDto(final ByteBuffer data) {
                this(data, 0);
        }

        public ByteBufferInnerDto(final ByteBuffer data, final int position) {
                _data = data;
                _offset = position;
        }

        /** @InheritDoc */
        @Override public long getOffset() {
                return _offset;
        }

        @Override public void setOffset(final long offset) {
                _offset = offset;
        }

        /** @InheritDoc */
        @Override public int getIndex() {
                return getData().getInt((int) getOffset());
        }

        @Override public final void setIndex(final int index) {
                getData().putInt((int) getOffset(), index);
        }

        /** @InheritDoc */
        @Override public ByteBuffer getData() {
                return _data;
        }

        @Override public void setData(final ByteBuffer data) {
                _data = data;
        }

        /** @InheritDoc */
        @Override public long getLongData() {
                return getData().getLong((int) getOffset() + 4);
        }

        /** @InheritDoc */
        @Override public void setLongData(final long longData) {
                getData().putLong((int) getOffset() + 4, longData);
        }

        /** @InheritDoc */
        @Override public int getIntData() {
                return getData().getInt((int) getOffset() + 12);
        }

        /** @InheritDoc */
        @Override public void setIntData(final int intData) {
                getData().putInt((int) getOffset() + 12, intData);
        }

        /** @InheritDoc */
        @Override public boolean isBoolData() {
                return getData().get((int) getOffset() + 16) != FALSE;
        }

        /** @InheritDoc */
        @Override public void setBoolData(final boolean boolData) {
                getData().put((int) getOffset() + 16, boolData ? TRUE : FALSE);
        }

        /** @InheritDoc */
        @Override public char getCharData() {
                return getData().getChar((int) getOffset() + 17);
        }

        /** @InheritDoc */
        @Override public void setCharData(final char charData) {
                getData().putChar((int) getOffset() + 17, charData);
        }

        /** @InheritDoc */
        @Override public String getStringData() {
                return null;
        }

        /** @InheritDoc */
        @Override public void setStringData(String stringData) {
        }
}//end ByteBufferInnerDto
