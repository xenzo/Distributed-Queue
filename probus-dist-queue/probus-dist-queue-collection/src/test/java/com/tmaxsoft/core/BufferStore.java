/*
 * BufferStore.java Version 1.0 Apr 23, 2014
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
package com.tmaxsoft.core;


import static java.util.logging.Level.*;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel.MapMode;
import java.util.logging.Logger;

import com.tmaxsoft.core.sample.dto.ByteBufferBackedDto;
import com.tmaxsoft.core.sample.dto.IDto;


/**
 *
 */
public class BufferStore {
        /** Logger for this class */
        private final transient Logger logger = Logger.getLogger("com.tmaxsoft.core");
        private static final int KB = 1024;
        private static final int MB = KB * KB;
        private static final int GB = MB * KB;
        MappedByteBuffer _hugeBuffer;
        private final int _segmentSize;
        private final int _count;
        private final IDto[] _store;
        private final int _storeSize;

        @SuppressWarnings("unchecked") public BufferStore(String file, int storeSize, int cacheLine) {
                int cline = cacheLine;
                _segmentSize = ((ByteBufferBackedDto.TOTAL_SIZE() - 1) / cline + 1) * cline;
                _storeSize = (storeSize / _segmentSize) * _segmentSize;
                _count = _storeSize / _segmentSize;
                logger.info("segment=" + _segmentSize + ", store=" + _storeSize + ", count=" + _count);
                try {
                        RandomAccessFile f = new RandomAccessFile(file, "rw");
                        _hugeBuffer = f.getChannel().map(MapMode.READ_WRITE, 0, _storeSize);
                } catch (IOException ex) {
                        logger.log(WARNING, "" + ex.getMessage(), ex);
                }
                _store = new IDto[_count];
                for (int i = 0, start = 0, end = _hugeBuffer.position(0).position(); i < _count; i++) {
                        end = start + _segmentSize;
                        _hugeBuffer.limit(end);
                        ByteBuffer slice = _hugeBuffer.slice();
                        _store[i] = new ByteBufferBackedDto(slice, i);
                        logger.info("index=" + i + ", start=" + start + ", end=" + end + ", direct=" + slice.isDirect());
                        start = end;
                        _hugeBuffer.position(start);
                }
        }

        public IDto get(int seq) {
                return _store[seq];
        }
}
//end BufferStore