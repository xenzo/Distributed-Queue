/*
 * TestMappedBuffer.java Version 1.0 Feb 7, 2012
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
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel.MapMode;
import java.nio.channels.ReadableByteChannel;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;


/**
 *
 */
public class TestMappedBuffer {
    @BeforeClass public static void setUp() {
    }

    @Test public void testMappedBuffer() throws IOException {
        RandomAccessFile file = null;
        try {
            File f = new File("src/test/resources/TempPool.map");
            file = new RandomAccessFile(f, "rw");
            MappedByteBuffer map = file.getChannel().map(MapMode.READ_WRITE, 0, 300000);
            //            file.close();
            if (file != null) file.close();
            map.clear();
            map.limit(10);
            ByteBuffer slice1 = map.slice();
            map.position(10);
            map.limit(20);
            ByteBuffer slice2 = map.slice();
            map.position(20);
            map.limit(30);
            ByteBuffer slice3 = map.slice();
            ByteArrayInputStream is = new ByteArrayInputStream("12345678  abcde     ABCDEF    ".getBytes());
            ReadableByteChannel newChannel = Channels.newChannel(is);
            newChannel.read(slice1);
            slice1.flip();
            byte[] b = new byte[10];
            slice1.get(b, 0, 10);
            System.out.println(new String(b));
            newChannel.read(slice2);
            slice2.flip();
            slice2.get(b, 0, 10);
            System.out.println(new String(b));
            newChannel.read(slice3);
            slice3.flip();
            slice3.get(b, 0, 10);
            System.out.println(new String(b));
            System.out.println(slice3.isDirect());
            System.out.println(slice3 instanceof MappedByteBuffer);
        } catch (Exception ex) {
            ex.printStackTrace();
            fail();
        } finally {
        }
    }

    @AfterClass public static void tearDown() {
    }
}
