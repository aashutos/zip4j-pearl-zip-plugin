/*
 * Copyright © 2021 92AK
 */

package com.ntak.pearlzip.archive.zip4j.util;

import com.ntak.pearlzip.archive.pub.ArchiveInfo;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.model.enums.AesKeyStrength;
import net.lingala.zip4j.model.enums.CompressionLevel;
import net.lingala.zip4j.model.enums.CompressionMethod;
import net.lingala.zip4j.model.enums.EncryptionMethod;

import java.util.Arrays;

import static com.ntak.pearlzip.archive.zip4j.constants.Zip4jConstants.*;

/**
 *  Utility methods used in the Zip4j write and read archive processes.
 *  @author Aashutos Kakshepati
 */
public class Zip4jUtil {
    public static void initializeZipParameters(ZipParameters parameters, ArchiveInfo archiveInfo) {
        if (archiveInfo.<Boolean>getProperty(KEY_ENCRYPTION_ENABLE).orElse(false)) {
            parameters.setEncryptFiles(true);
            parameters.setEncryptionMethod(EncryptionMethod.valueOf(archiveInfo.<String>getProperty(
                    KEY_ENCRYPTION_METHOD).orElse("AES")));
            parameters.setAesKeyStrength(AesKeyStrength.valueOf(archiveInfo.<String>getProperty(KEY_ENCRYPTION_STRENGTH).orElse("KEY_STRENGTH_256")));
        }

        parameters.setCompressionMethod(CompressionMethod.valueOf(archiveInfo.<String>getProperty(KEY_COMPRESSION_METHOD).orElse("DEFLATE")));
        parameters.setCompressionLevel(Arrays.stream(CompressionLevel.values())
                                             .filter(c-> c.getLevel() ==
                                               archiveInfo.getCompressionLevel())
                                             .findFirst()
                                             .orElse(CompressionLevel.MAXIMUM)
        );
    }
}
