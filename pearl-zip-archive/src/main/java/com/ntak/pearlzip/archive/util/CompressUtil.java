/*
 * Copyright © 2021 92AK
 */
package com.ntak.pearlzip.archive.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.CRC32;

/**
 *  Helper class, which performs tasks that aid with compression mechanisms.
 *  @author Aashutos Kakshepati
 */
public class CompressUtil {

    public static long crcHashFile(File file) {
        final CRC32 CRC_HASH = new CRC32();

        try (InputStream ioStream = new FileInputStream(file)) {
            byte[] buffer = new byte[2048];
            int bytesRead;
            while((bytesRead = ioStream.read(buffer)) != -1){
                CRC_HASH.update(buffer, 0, bytesRead);
            }
        } catch(IOException e) {
        }

        return CRC_HASH.getValue();
    }
}
