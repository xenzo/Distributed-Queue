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


import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import sun.misc.Unsafe;


public class UnsafeInnerDto implements IBackedInner {
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

        public static final int SIZE() {
                return 18;
        }

        public static final int TOTAL_SIZE() {
                return 18;
        }

        private long _offset;
        private ByteBuffer _data;
        private final int _position;

        public UnsafeInnerDto(final ByteBuffer data) {
                this(data, 0);
        }

        public UnsafeInnerDto(final ByteBuffer data, final int position) {
                _data = data;
                _position = position;
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
                return unsafe.getInt(getOffset());
        }

        @Override public final void setIndex(final int index) {
                unsafe.putInt(getOffset(), index);
        }

        /** @InheritDoc */
        @Override public ByteBuffer getData() {
                return _data;
        }

        @Override public void setData(final ByteBuffer data) {
                _data = data;
                if (data != null) _offset = ((sun.nio.ch.DirectBuffer) data).address() + _position;
        }

        /**
         * @return the longData
         */
        @Override public long getLongData() {
                return unsafe.getLong(getOffset() + 4);
        }

        /**
         * @param longData the longData to set
         */
        @Override public void setLongData(final long longData) {
                unsafe.putLong(getOffset() + 4, longData);
        }

        /**
         * @return the intData
         */
        @Override public int getIntData() {
                return unsafe.getInt(getOffset() + 12);
        }

        /**
         * @param intData the intData to set
         */
        @Override public void setIntData(final int intData) {
                unsafe.putInt(getOffset() + 12, intData);
        }

        /**
         * @return the boolData
         */
        @Override public boolean isBoolData() {
                return unsafe.getByte(getOffset() + 16) != FALSE;
        }

        /**
         * @param boolData the boolData to set
         */
        @Override public void setBoolData(final boolean boolData) {
                unsafe.putByte(getOffset() + 16, boolData ? TRUE : FALSE);
        }

        /**
         * @return the charData
         */
        @Override public char getCharData() {
                return unsafe.getChar(getOffset() + 17);
        }

        /**
         * @param charData the charData to set
         */
        @Override public void setCharData(final char charData) {
                unsafe.putChar(getOffset() + 17, charData);
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
}//end ByteBufferInnerDto
