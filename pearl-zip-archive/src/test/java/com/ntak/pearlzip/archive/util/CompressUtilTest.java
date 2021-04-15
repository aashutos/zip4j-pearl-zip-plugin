/*
 * Copyright © 2021 92AK
 */
package com.ntak.pearlzip.archive.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class CompressUtilTest {

    /*
        Test cases:
        + Hash given file and ensure generated CRC Hash matches precompiled expectation
        + Hash attempted on non-existent file and an IOException is raised. Ensure default value is returned.
     */

    @Test
    @DisplayName("Test: Generate CRC Hash with the expected value from a valid file")
    public void generateCRCHash_ValidFile_MatchExpectations() throws IOException {
        Path tempFile = Files.createTempFile("tmp", "");
        try {
            Files.writeString(tempFile, "A temporary file to generate a CRC hash of...");
            long hash = CompressUtil.crcHashFile(tempFile.toFile());

            Assertions.assertEquals(2102450593, hash, "Expected hash value was not returned");
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    @Test
    @DisplayName("Test: Generate CRC Hash with the default value from an invalid file")
    public void generateCRCHash_InvalidFile_DefaultValue() {
        long hash = CompressUtil.crcHashFile(new File("non-existent-file"));
        Assertions.assertEquals(0, hash, "Default hash value was not returned");
    }
}
