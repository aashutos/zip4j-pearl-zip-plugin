/*
 * Copyright © 2021 92AK
 */
package com.ntak.pearlzip.archive.acc.util;

import static org.apache.commons.compress.compressors.CompressorStreamFactory.BZIP2;

/**
 *  Utility methods used by the Apache Commons Compress implementation of the Archive Service interfaces.
 */
public class CommonsCompressUtil {
    public static String getArchiveFormat(String archivePath) {

        String format =  archivePath.substring(archivePath.lastIndexOf(".") + 1)
                                    .toUpperCase();

        return switch(format) {
            case "BZ2" -> BZIP2.toUpperCase();
            default -> format;
        };
    }
}
