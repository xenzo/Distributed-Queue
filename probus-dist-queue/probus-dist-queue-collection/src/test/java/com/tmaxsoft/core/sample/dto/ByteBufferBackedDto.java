/*
 * ByteBufferBackedDto.java Version 1.0 Apr 23, 2014
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
import java.nio.charset.Charset;


public class ByteBufferBackedDto implements IBacked {
        Charset enc = Charset.forName("UTF-8");

        private static final int INNER_COUNT() {
                return 100;
        }

        public static final int SIZE() {
                return 59;
        }

        public static final int TOTAL_SIZE() {
                return SIZE() + ByteBufferInnerDto.TOTAL_SIZE() * INNER_COUNT();
        }

        private long _offset;
        private ByteBuffer _data;
        private final IBackedInner _inner;

        public ByteBufferBackedDto(final ByteBuffer data) {
                this(data, 0);
        }

        public ByteBufferBackedDto(final ByteBuffer data, final int position) {
                _offset = position;
                _data = data;
                _inner = new ByteBufferInnerDto(data);
                _inner.setData(data);
        }

        /** @InheritDoc */
        @Override public IInnerDto get(final int seq) {
                assert seq < INNER_COUNT();
                _inner.setOffset(getOffset() + SIZE() + seq * ByteBufferInnerDto.TOTAL_SIZE());
                return _inner;
        }

        /**
         * @param index
         */
        @Override public final void setIndex(final int index) {
                getData().putInt((int) getOffset(), index);
        }

        /** @InheritDoc */
        @Override public final long getOffset() {
                return _offset;
        }

        @Override public void setOffset(final long offset) {
                _offset = offset;
        }

        /** @InheritDoc */
        @Override public final int getIndex() {
                return getData().getInt((int) getOffset());
        }

        /** @InheritDoc */
        @Override public final ByteBuffer getData() {
                return _data;
        }

        @Override public final void setData(final ByteBuffer data) {
                _data = data;
                _inner.setData(data);
        }

        /** @InheritDoc */
        @Override public final long getLongData() {
                return getData().getLong((int) getOffset() + 4);
        }

        /** @InheritDoc */
        @Override public final void setLongData(final long longData) {
                getData().putLong((int) getOffset() + 4, longData);
        }

        /**
         * @return the doubleData
         */
        @Override public final double getDoubleData() {
                return getData().getDouble((int) getOffset() + 12);
        }

        /** @InheritDoc */
        @Override public final void setDoubleData(final double doubleData) {
                getData().putDouble((int) getOffset() + 12, doubleData);
        }

        /** @InheritDoc */
        @Override public final int getIntData() {
                return getData().getInt((int) getOffset() + 20);
        }

        /** @InheritDoc */
        @Override public final void setIntData(final int intData) {
                getData().putInt((int) getOffset() + 20, intData);
        }

        /** @InheritDoc */
        @Override public final boolean isBoolData() {
                return getData().get((int) getOffset() + 24) != FALSE;
        }

        /** @InheritDoc */
        @Override public final void setBoolData(final boolean boolData) {
                getData().put((int) getOffset() + 24, boolData ? TRUE : FALSE);
        }

        /** @InheritDoc */
        @Override public final char getCharData() {
                return getData().getChar((int) getOffset() + 25);
        }

        /** @InheritDoc */
        @Override public final void setCharData(final char charData) {
                getData().putChar((int) getOffset() + 25, charData);
        }

        /** @InheritDoc */
        @Override public final String getStringData() {
                final byte[] str = new byte[32];
                getData().get(str, 0, 32);
                return new String(str, enc);
        }

        /** @InheritDoc */
        @Override public final void setStringData(final String strData) {
                final byte[] b = strData.getBytes(enc);
                getData().position((int) getOffset() + 27);
                getData().put(b, 0, b.length > 32 ? 32 : b.length);
        }
}
//end ByteBufferBackedDto