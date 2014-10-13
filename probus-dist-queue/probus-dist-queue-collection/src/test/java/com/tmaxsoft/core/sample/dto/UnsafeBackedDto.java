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


import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import sun.misc.Unsafe;


public class UnsafeBackedDto implements IBacked {
        private static final Charset enc = Charset.forName("UTF-8");
        private static Unsafe unsafe;
        private static int arrayBaseOffset;
        static {
                Field f;
                try {
                        f = Unsafe.class.getDeclaredField("theUnsafe");
                        f.setAccessible(true);
                        unsafe = (Unsafe) f.get(null);
                        arrayBaseOffset = unsafe.arrayBaseOffset(byte[].class);
                } catch (final SecurityException ex) {
                } catch (final NoSuchFieldException ex) {
                } catch (final IllegalArgumentException ex) {
                } catch (final IllegalAccessException ex) {
                }
        }

        private static final int INNER_COUNT() {
                return 100;
        }

        public static final int SIZE() {
                return 59;
        }

        public static final int TOTAL_SIZE() {
                return SIZE() + UnsafeInnerDto.TOTAL_SIZE() * INNER_COUNT();
        }

        private long _offset;
        private ByteBuffer _data;
        private final IBackedInner _inner;
        private final int _position;

        public UnsafeBackedDto(final ByteBuffer data) {
                this(data, 0);
        }

        public UnsafeBackedDto(final ByteBuffer data, final int position) {
                _data = data;
                _inner = new UnsafeInnerDto(data);
                _inner.setData(data);
                _position = position;
                arrayBaseOffset = unsafe.arrayBaseOffset(byte[].class);
        }

        @Override public IInnerDto get(final int seq) {
                assert seq < INNER_COUNT();
                _inner.setOffset(getOffset() + SIZE() + seq * ByteBufferInnerDto.TOTAL_SIZE());
                return _inner;
        }

        /**
         * @param index
         */
        @Override public final void setIndex(final int index) {
                unsafe.putInt(getOffset(), index);
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
                return unsafe.getInt(getOffset());
        }

        /** @InheritDoc */
        @Override public final ByteBuffer getData() {
                return _data;
        }

        @Override public final void setData(final ByteBuffer data) {
                _data = data;
                if (data != null) _offset = ((sun.nio.ch.DirectBuffer) data).address() + _position;
                _inner.setData(data);
        }

        /**
         * @return the longData
         */
        @Override public final long getLongData() {
                return unsafe.getLong(getOffset() + 4);
        }

        /**
         * @param longData the longData to set
         */
        @Override public final void setLongData(final long longData) {
                unsafe.putLong(getOffset() + 4, longData);
        }

        /**
         * @return the doubleData
         */
        public final double getDoubleData() {
                return unsafe.getDouble(getOffset() + 12);
        }

        /**
         * @param doubleData the doubleData to set
         */
        @Override public final void setDoubleData(final double doubleData) {
                unsafe.putDouble(getOffset() + 12, doubleData);
        }

        /**
         * @return the intData
         */
        @Override public final int getIntData() {
                return unsafe.getInt(getOffset() + 20);
        }

        /**
         * @param intData the intData to set
         */
        @Override public final void setIntData(final int intData) {
                unsafe.putInt(getOffset() + 20, intData);
        }

        /**
         * @return the boolData
         */
        @Override public final boolean isBoolData() {
                return unsafe.getByte(getOffset() + 24) != FALSE;
        }

        /**
         * @param boolData the boolData to set
         */
        @Override public final void setBoolData(final boolean boolData) {
                unsafe.putByte(getOffset() + 24, boolData ? TRUE : FALSE);
        }

        /**
         * @return the charData
         */
        @Override public final char getCharData() {
                return unsafe.getChar(getOffset() + 25);
        }

        /**
         * @param charData the charData to set
         */
        @Override public final void setCharData(final char charData) {
                unsafe.putChar(getOffset() + 25, charData);
        }

        @Override public final String getStringData() {
                final byte[] str = new byte[32];
                unsafe.copyMemory(null, getOffset() + 27, str, arrayBaseOffset, 32);
                return new String(str, enc);
        }

        @Override public final void setStringData(final String strData) {
                final byte[] bytes = strData.getBytes(enc);
                unsafe.copyMemory(bytes, arrayBaseOffset, null, getOffset() + 27,
                        bytes.length > 32 ? 32 : bytes.length);
        }
}
//end ByteBufferBackedDto