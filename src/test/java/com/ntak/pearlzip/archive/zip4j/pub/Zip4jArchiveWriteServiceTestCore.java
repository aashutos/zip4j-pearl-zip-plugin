/*
 * Copyright © 2021 92AK
 */

package com.ntak.pearlzip.archive.zip4j.pub;

import com.ntak.pearlzip.archive.pub.ArchiveInfo;
import com.ntak.pearlzip.archive.pub.FileInfo;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;

import static com.ntak.pearlzip.archive.constants.ConfigurationConstants.KEY_FILE_PATH;
import static com.ntak.pearlzip.archive.zip4j.constants.Zip4jConstants.*;
import static net.lingala.zip4j.model.enums.AesKeyStrength.KEY_STRENGTH_256;
import static net.lingala.zip4j.model.enums.EncryptionMethod.AES;
import static org.junit.jupiter.api.Assertions.fail;

public abstract class Zip4jArchiveWriteServiceTestCore {

    private static Zip4jArchiveWriteService service;
    private static Zip4jArchiveReadService readService;
    private static Path tempDirectory;
    private static Path file;
    private static FileInfo fileInfo;
    private static Path secondFile;
    private static FileInfo secondFileInfo;

    @BeforeAll
    public static void setUpOnce() throws IOException {
        tempDirectory = Files.createTempDirectory("pz-text");
        file = Paths.get(tempDirectory.toAbsolutePath().toString(), "tempFile.txt");
        fileInfo = new FileInfo(0, 0, "tempFile.txt", 0L, 0L, 0L,
                                LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(),
                                "", "", 0, "", false, true, Collections.singletonMap(KEY_FILE_PATH,
                                                                                     file.toAbsolutePath().toString())
                                );
        secondFile = Paths.get(tempDirectory.toAbsolutePath().toString(), "tempFile2.txt");
        secondFileInfo = new FileInfo(0, 0, "tempFile2.txt", 0L, 0L, 0L,
                                LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(),
                                "", "", 0, "", false, true, Collections.singletonMap(KEY_FILE_PATH,
                                                                                     file.toAbsolutePath().toString())
        );
        Files.deleteIfExists(file);
        Files.createFile(file);
        Files.deleteIfExists(secondFile);
        Files.createFile(secondFile);
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
        service = new Zip4jArchiveWriteService();
        readService = new Zip4jArchiveReadService();
    }

    /*
     *  Test cases:
     *  + Create Archive
     *  + Create encrypted archive and add a file to the archive
     *  + Create encrypted archive and delete a file to the archive
     */

    @Test
    @DisplayName("Test: Create Zip Archive Successfully")
    public void testCreateZipArchive_Success() {
        ArchiveInfo archiveInfo = new ArchiveInfo();
        archiveInfo.setArchiveFormat("zip");
        final Path archive = Paths.get(tempDirectory.toAbsolutePath()
                                                 .toString(), "tempArchive.zip");
        archiveInfo.setArchivePath(archive.toString());
        service.createArchive(System.currentTimeMillis(), archiveInfo);

        Assertions.assertTrue(Files.exists(archive), "Archive was not created");
    }

    @Test
    @DisplayName("Test: Create Encrypted Zip Archive Successfully and Add file")
    public void testCreateEncryptedZipArchiveWithAdd_Success() {
        ArchiveInfo archiveInfo = new ArchiveInfo();
        archiveInfo.setArchiveFormat("zip");
        archiveInfo.addProperty(KEY_ENCRYPTION_ENABLE, true);
        archiveInfo.addProperty(KEY_ENCRYPTION_METHOD, AES);
        archiveInfo.addProperty(KEY_ENCRYPTION_STRENGTH, KEY_STRENGTH_256);
        archiveInfo.addProperty(KEY_ENCRYPTION_PW, new String("SomePa$$W0rD").toCharArray());
        final Path archive = Paths.get(tempDirectory.toAbsolutePath()
                                                    .toString(), "tempEncryptedArchive.zip");
        archiveInfo.setArchivePath(archive.toString());

        service.createArchive(System.currentTimeMillis(), archiveInfo);
        Assertions.assertTrue(Files.exists(archive), "Archive was not created");

        service.addFile(System.currentTimeMillis(), archiveInfo, fileInfo);

        // Check Encryption flag... (Byte 7)
        try (InputStream is = Files.newInputStream(archive)) {
            byte encFlag = (byte)(is.readNBytes(7)[6] & (byte)0x1);
            Assertions.assertEquals((byte)0x1,encFlag, "Encryption flag not set");
        } catch(IOException e) {
            fail(String.format("Issue reading archive. Exception %s; Message: %s", e.getClass().getCanonicalName(),
                               e.getMessage()));
        }
    }

    @Test
    @DisplayName("Test: Create Encrypted Zip Archive successfully and Delete file")
    public void testCreateEncryptedZipArchiveWithDelete_Success() {
        ArchiveInfo archiveInfo = new ArchiveInfo();
        archiveInfo.setArchiveFormat("zip");
        archiveInfo.addProperty(KEY_ENCRYPTION_ENABLE, true);
        archiveInfo.addProperty(KEY_ENCRYPTION_METHOD, AES);
        archiveInfo.addProperty(KEY_ENCRYPTION_STRENGTH, KEY_STRENGTH_256);
        archiveInfo.addProperty(KEY_ENCRYPTION_PW, new String("SomePa$$W0rD").toCharArray());
        final Path archive = Paths.get(tempDirectory.toAbsolutePath()
                                                    .toString(), "tempEncryptedArchive.zip");
        archiveInfo.setArchivePath(archive.toString());

        testCreateEncryptedZipArchiveWithAdd_Success();

        service.addFile(System.currentTimeMillis(), archiveInfo, secondFileInfo);
        Assertions.assertTrue(Files.exists(archive), "Archive was not created");

        service.deleteFile(System.currentTimeMillis(), archiveInfo, fileInfo);
        Assertions.assertTrue(Files.exists(archive), "Archive was not created");

        // Check Encryption flag... (Byte 7)
        try (InputStream is = Files.newInputStream(archive)) {
            byte encFlag = (byte)(is.readNBytes(7)[6] & (byte)0x1);
            Assertions.assertEquals((byte)0x1,encFlag, "Encryption flag not set");
        } catch(IOException e) {
            fail(String.format("Issue reading archive. Exception %s; Message: %s", e.getClass().getCanonicalName(),
                               e.getMessage()));
        }
    }

    @Test
    @DisplayName("Test: Remove Last file in directory from Zip Archive will not remove directory")
    public void testRemove_LastFileWithinDirectory_Success() throws IOException {
        ArchiveInfo archiveInfo = new ArchiveInfo();
        archiveInfo.setArchiveFormat("zip");

        // Create directory with one file
        Path file = Paths.get(tempDirectory.toAbsolutePath().toString(), "dir","file");
        Files.createDirectories(file.getParent());
        Files.createFile(file);

        FileInfo fileInfo = new FileInfo(0, 1, "dir/file", 0L, 0L, 0L,
                                     LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(),
                                     "", "", 0, "", false, true, Collections.singletonMap(KEY_FILE_PATH,
                                                                                          file.toAbsolutePath().toString())
        );

        final Path archive = Paths.get(tempDirectory.toAbsolutePath()
                                                    .toString(), "tempArchive.zip");
        archiveInfo.setArchivePath(archive.toString());

        // Create archive with directory and single file within
        service.createArchive(System.currentTimeMillis(), archiveInfo, fileInfo);
        Assertions.assertEquals(2,
                                readService.listFiles(System.currentTimeMillis(), archiveInfo).size(),
                                "Initial file counts were not as expected");

        // Delete file
        service.deleteFile(System.currentTimeMillis(), archiveInfo, fileInfo);
        Assertions.assertEquals(1,
                                readService.listFiles(System.currentTimeMillis(), archiveInfo).size(),
                                "File count after delete was not as expected");
        Assertions.assertEquals("dir",
                                readService.listFiles(System.currentTimeMillis(), archiveInfo).get(0).getFileName(),
                                "Expected folder was not present");


        Assertions.assertTrue(Files.exists(archive), "Archive was not created");
    }
}
