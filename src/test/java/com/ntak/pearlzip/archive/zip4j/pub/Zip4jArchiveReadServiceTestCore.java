/*
 * Copyright © 2021 92AK
 */

package com.ntak.pearlzip.archive.zip4j.pub;

import com.ntak.pearlzip.archive.pub.ArchiveInfo;
import com.ntak.pearlzip.archive.pub.FileInfo;
import com.ntak.pearlzip.ui.constants.internal.InternalContextCache;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.ntak.pearlzip.archive.zip4j.constants.Zip4jConstants.*;
import static com.ntak.pearlzip.ui.constants.ZipConstants.CK_POST_PZAX_COMPLETION_CALLBACK;
import static net.lingala.zip4j.model.enums.AesKeyStrength.KEY_STRENGTH_256;
import static net.lingala.zip4j.model.enums.EncryptionMethod.AES;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public abstract class Zip4jArchiveReadServiceTestCore {

    private static Zip4jArchiveReadService service;
    private static Path tempDirectory;
    private Path unencryptedArchive = Paths.get("src","test","resources","unencryptedArchive.zip");
    private Path encryptedArchive = Paths.get("src","test","resources","encryptedArchive.zip");
    private Path faultyArchive = Paths.get("src","test","resources","faultyArchive.zip");

    /*
        Test cases:
        + List files from archive (zip)
        + List files from encrypted archive (zip)
        + List file returns empty list when opening non-existent archive
        + Extract non-existent file
        + Extract file from archive (zip)
        + Test file success (zip)
        + Test file failure (broken archive)
     */

    @BeforeAll
    public static void setUpOnce() throws IOException {
        tempDirectory = Files.createTempDirectory("pz-text");
        InternalContextCache.INTERNAL_CONFIGURATION_CACHE.setAdditionalConfig(CK_POST_PZAX_COMPLETION_CALLBACK, (Runnable)()->{});
    }

    @AfterAll
    public static void teardownLast() throws IOException {
        Files.walk(tempDirectory).filter((f)->!Files.isDirectory(f)).forEach(f-> {
            try {
                Files.deleteIfExists(f);
            } catch(IOException e) {
            }
        });
        Files.walk(tempDirectory).filter(Files::isDirectory).sorted((a,b)->b.toString().length()-a.toString().length()).forEach(f-> {
            try {
                Files.deleteIfExists(f);
            } catch(IOException e) {
            }
        });
        Files.deleteIfExists(tempDirectory);
    }

    @BeforeEach
    public void setUp() {
        service = new Zip4jArchiveReadService();
    }

    ///// LIST FILES /////

    @Test
    @DisplayName("Test: List files from an unencrypted archive")
    public void testListFiles_UnencryptedArchive_Success() {
        List<String> expectations = Arrays.asList("level2","level2/level2-file","level2/UP-MOVE");
        long sessionId = System.currentTimeMillis();
        List<FileInfo> files = service.listFiles(sessionId, unencryptedArchive.toAbsolutePath().toString());
        Assertions.assertEquals(3, files.size(), "The expected number of files was not read");
        Assertions.assertEquals(expectations.size(), files.size(), "The expected number of files was not read");
        Assertions.assertTrue(files.stream().map(FileInfo::getFileName).allMatch(expectations::contains),
                              "All filenames are accounted for in expectations");
    }

    @Test
    @DisplayName("Test: List files from an encrypted archive")
    public void testListFiles_EncryptedArchive_Success() {
        List<String> expectations = Arrays.asList("level2","level2/level2-file","level2/UP-MOVE");
        long sessionId = System.currentTimeMillis();
        ArchiveInfo archiveInfo = new ArchiveInfo();
        archiveInfo.setArchivePath(encryptedArchive.toAbsolutePath().toString());
        archiveInfo.setArchiveFormat("zip");
        archiveInfo.addProperty(KEY_ENCRYPTION_ENABLE, true);
        archiveInfo.addProperty(KEY_ENCRYPTION_METHOD, AES);
        archiveInfo.addProperty(KEY_ENCRYPTION_STRENGTH, KEY_STRENGTH_256);
        archiveInfo.addProperty(KEY_ENCRYPTION_PW, new String("Pa$$w0rD").toCharArray());

        List<FileInfo> files = service.listFiles(sessionId, archiveInfo);
        Assertions.assertEquals(expectations.size(), files.size(), "The expected number of files was not read");
        Assertions.assertTrue(files.stream().map(FileInfo::getFileName).allMatch(expectations::contains),
                              "All filenames are accounted for in expectations");
    }

    @Test
    @DisplayName("Test: List files from an encrypted archive with no password successfully")
    public void testListFiles_EncryptedArchiveNoPassword_Success() {
        List<String> expectations = Arrays.asList("level2","level2/level2-file","level2/UP-MOVE");
        long sessionId = System.currentTimeMillis();
        ArchiveInfo archiveInfo = new ArchiveInfo();
        archiveInfo.setArchivePath(encryptedArchive.toAbsolutePath().toString());
        archiveInfo.setArchiveFormat("zip");
        archiveInfo.addProperty(KEY_ENCRYPTION_ENABLE, true);
        archiveInfo.addProperty(KEY_ENCRYPTION_METHOD, AES);
        archiveInfo.addProperty(KEY_ENCRYPTION_STRENGTH, KEY_STRENGTH_256);

        List<FileInfo> files = service.listFiles(sessionId, archiveInfo);
        Assertions.assertEquals(expectations.size(), files.size(), "The expected number of files was not read");
        Assertions.assertTrue(files.stream().map(FileInfo::getFileName).allMatch(expectations::contains),
                              "All filenames are accounted for in expectations");
    }

    @Test
    @DisplayName("Test: List files for an invalid archive will return an empty list")
    public void testListFiles_InvalidFile_Empty() {
        long sessionId = System.currentTimeMillis();
        List<FileInfo> files = service.listFiles(sessionId, "non-existent-file.zip");
        Assertions.assertNotNull(files, "Null was unexpectedly returned");
        Assertions.assertEquals(0, files.size(), "Files were unexpectedly returned");
    }

    ///// EXTRACT FILES /////

    @Test
    @DisplayName("Test: Extract files for an valid archive successfully")
    public void testExtractFiles_ValidArchive_Success() {
        Path destination = Paths.get(tempDirectory.toAbsolutePath().toString(), "level2-file");
        FileInfo fileInfo = new FileInfo(1, 1, "level2/level2-file", 0L, 0L, 0L,
                                                    LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(),
                                                    "", "", 0, "", false, true, Collections.emptyMap());
        long sessionId = System.currentTimeMillis();
        ArchiveInfo archiveInfo = new ArchiveInfo();
        archiveInfo.setArchivePath(encryptedArchive.toAbsolutePath().toString());
        archiveInfo.setArchiveFormat("zip");
        archiveInfo.addProperty(KEY_ENCRYPTION_ENABLE, true);
        archiveInfo.addProperty(KEY_ENCRYPTION_METHOD, AES);
        archiveInfo.addProperty(KEY_ENCRYPTION_STRENGTH, KEY_STRENGTH_256);
        archiveInfo.addProperty(KEY_ENCRYPTION_PW, new String("Pa$$w0rD").toCharArray());

        Assertions.assertTrue(service.extractFile(sessionId, destination, archiveInfo, fileInfo),
                              "Extraction of file was not successful");
        Assertions.assertTrue(Files.exists(destination), "File was not extracted");
    }

    @Test
    @DisplayName("Test: Extract files for an invalid archive will return false")
    public void testExtractFiles_InvalidArchive_False() {
        Path destination = Paths.get(tempDirectory.toAbsolutePath().toString(), "level2-file");
        FileInfo fileInfo = new FileInfo(1, 1, "level2/level2-file", 0L, 0L, 0L,
                                         LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(),
                                         "", "", 0, "", false, true, Collections.emptyMap());
        long sessionId = System.currentTimeMillis();

        Assertions.assertFalse(service.extractFile(sessionId, destination, faultyArchive.toAbsolutePath().toString(),
                                            fileInfo),
                              "Extraction of file was unexpectedly successful");
    }

    ///// TEST FILES /////

    @Test
    @DisplayName("Test: Test Zip archive")
    public void testFiles_ValidZipFile_True() {
        long sessionId = System.currentTimeMillis();
        assertTrue(service.testArchive(sessionId, unencryptedArchive.toAbsolutePath().toString()), "Archive not valid");
    }

    @Test
    @DisplayName("Test: Test invalid archive")
    public void testFiles_InvalidArchive_False() {
        long sessionId = System.currentTimeMillis();
        assertFalse(service.testArchive(sessionId, faultyArchive.toAbsolutePath().toString()), "Archive valid unexpectedly");
    }
}
