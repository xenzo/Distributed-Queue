/*
 * ByteBufferStore.java Version 1.0 Apr 28, 2014
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
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel.MapMode;
import java.util.logging.Logger;

import com.tmaxsoft.core.sample.dto.ByteBufferBackedDto;
import com.tmaxsoft.core.sample.dto.IBuffered;


/**
 *
 */
public class ByteBufferStore {
        /** Logger for this class */
        private final transient Logger logger = Logger.getLogger("com.tmaxsoft.core");
        private static final int KB = 1024;
        private static final int MB = KB * KB;
        private static final int GB = MB * KB;
        private static final int PAGE = 4 * KB;
        MappedByteBuffer _hugeBuffer;
        private int _segmentSize;
        private final int _count;
        private final ByteBuffer[] _store;
        private final int _storeSize;

        @SuppressWarnings("unchecked") public ByteBufferStore(String file, int storeSize, int cacheLine) {
                int cline = cacheLine;
                _segmentSize = ((ByteBufferBackedDto.TOTAL_SIZE() - 1) / cline + 1) * cline;
                if (_segmentSize <= PAGE) {
                        final int clinePerPage = PAGE / cline;
                        int ps = PAGE / _segmentSize;
                        while (clinePerPage % ps != 0)
                                ps--;
                        _segmentSize = (clinePerPage / ps) * cline;
                }
                _storeSize = ((storeSize / _segmentSize) * _segmentSize / PAGE) * PAGE;
                _count = _storeSize / _segmentSize;
                logger.fine("segment=" + _segmentSize + ", store=" + _storeSize + ", count=" + _count);
                try {
                        RandomAccessFile f = new RandomAccessFile(file, "rw");
                        _hugeBuffer = f.getChannel().map(MapMode.READ_WRITE, 0, _storeSize);
                } catch (IOException ex) {
                        logger.log(WARNING, "" + ex.getMessage(), ex);
                }
                _store = new ByteBuffer[_count];
                for (int i = 0, start = 0, end = _hugeBuffer.position(0).position(); i < _count; i++) {
                        end = start + _segmentSize;
                        _hugeBuffer.limit(end);
                        _store[i] = _hugeBuffer.slice();
                        logger.fine("index=" + i + ", start=" + start + ", end=" + end + ", direct=" + _store[i].isDirect());
                        start = end;
                        _hugeBuffer.position(start);
                }
        }

        public ByteBuffer get(int seq) {
                return _store[seq];
        }

        public IBuffered get(int seq, IBuffered dto) {
                dto.setData(_store[seq]);
                dto.setIndex(seq);
                return dto;
        }
}
//end ByteBufferStore