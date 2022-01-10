/*
 * Copyright © 2021 92AK
 */

package com.ntak.pearlzip.archive.zip4j.pub;

import com.ntak.pearlzip.archive.pub.ArchiveInfo;
import com.ntak.pearlzip.archive.pub.FileInfo;
import com.ntak.pearlzip.ui.constants.ZipConstants;
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
        ZipConstants.POST_PZAX_COMPLETION_CALLBACK = ()->{};
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

    @BeforeEach
    public void setUp() throws IOException {
        service = new Zip4jArchiveWriteService();
        readService = new Zip4jArchiveReadService();
        Files.createDirectories(tempDirectory);
        Files.deleteIfExists(file);
        Files.createFile(file);
        Files.deleteIfExists(secondFile);
        Files.createFile(secondFile);
    }

    @AfterEach
    public void tearDown() throws IOException {
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

    @Test
    @DisplayName("Test: Add nested empty directory to archive")
    public void testAdd_NestedEmptyDirectory_Success() throws IOException {
        ArchiveInfo archiveInfo = new ArchiveInfo();
        archiveInfo.setArchiveFormat("zip");

        // Create directory with one file
        Path file = Paths.get(tempDirectory.toAbsolutePath().toString(),"empty");
        Files.createDirectories(file);

        // Create archive
        final Path archive = Paths.get(tempDirectory.toAbsolutePath()
                                                    .toString(), "tempArchive.zip");
        archiveInfo.setArchivePath(archive.toString());
        service.createArchive(System.currentTimeMillis(), archiveInfo);

        // Add first directory...
        FileInfo fileInfo = new FileInfo(0, 0, "empty", 0L, 0L, 0L,
                                         LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(),
                                         "", "", 0, "", true, false, Collections.singletonMap(KEY_FILE_PATH,
                                                                                              file.toAbsolutePath().toString())
        );
        service.addFile(System.currentTimeMillis(), archiveInfo, fileInfo);
        Assertions.assertEquals(1,
                                readService.listFiles(System.currentTimeMillis(), archiveInfo).size(),
                                "File count was not as expected");
        Assertions.assertTrue(
                readService.listFiles(System.currentTimeMillis(), archiveInfo).stream().map(FileInfo::getFileName).anyMatch(f->f.equals("empty")),
                "File empty was not found");

        fileInfo = new FileInfo(1, 1, "empty/empty", 0L, 0L, 0L,
                                         LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(),
                                         "", "", 0, "", true, false, Collections.singletonMap(KEY_FILE_PATH,
                                                                                              file.toAbsolutePath().toString())
        );
        service.addFile(System.currentTimeMillis(), archiveInfo, fileInfo);
        Assertions.assertEquals(2,
                                readService.listFiles(System.currentTimeMillis(), archiveInfo).size(),
                                "File count was not as expected");
        Assertions.assertTrue(
                readService.listFiles(System.currentTimeMillis(), archiveInfo).stream().map(FileInfo::getFileName).anyMatch(f->f.equals("empty")),
                "File empty was not found");
        Assertions.assertTrue(
                readService.listFiles(System.currentTimeMillis(), archiveInfo).stream().map(FileInfo::getFileName).anyMatch(f->f.equals("empty/empty")),
                "File empty/empty was not found");

        fileInfo = new FileInfo(2, 2, "empty/empty/empty", 0L, 0L, 0L,
                                         LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(),
                                         "", "", 0, "", true, false, Collections.singletonMap(KEY_FILE_PATH,
                                                                                              file.toAbsolutePath().toString())
        );
        service.addFile(System.currentTimeMillis(), archiveInfo, fileInfo);
        Assertions.assertEquals(3,
                                readService.listFiles(System.currentTimeMillis(), archiveInfo).size(),
                                "File count was not as expected");
        Assertions.assertTrue(
                readService.listFiles(System.currentTimeMillis(), archiveInfo).stream().map(FileInfo::getFileName).anyMatch(f->f.equals("empty")),
                "File empty was not found");
        Assertions.assertTrue(
                readService.listFiles(System.currentTimeMillis(), archiveInfo).stream().map(FileInfo::getFileName).anyMatch(f->f.equals("empty/empty")),
                "File empty/empty was not found");
        Assertions.assertTrue(
                readService.listFiles(System.currentTimeMillis(), archiveInfo).stream().map(FileInfo::getFileName).anyMatch(f->f.equals("empty/empty/empty")),
                "File empty/empty/empty was not found");

        Assertions.assertTrue(Files.exists(archive), "Archive was not created");
    }

    @Test
    @DisplayName("Test: Delete folder or file which is a prefix of another file/folder on the same level. Should not " +
            "delete other file")
    public void testDelete_FolderOrFileSameLevelSharedPrefix_Success() throws IOException {
        ArchiveInfo archiveInfo = new ArchiveInfo();
        archiveInfo.setArchiveFormat("zip");

        // Create directory with one file
        Path folder = Paths.get(tempDirectory.toAbsolutePath().toString(),"zip");
        Files.createDirectories(folder);

        Path anotherFolder = Paths.get(tempDirectory.toAbsolutePath().toString(),"zipster");
        Files.createDirectories(anotherFolder);

        Path file = Paths.get(tempDirectory.toAbsolutePath().toString(),"zippy-file");
        Files.createFile(file);

        // Create archive
        final Path archive = Paths.get(tempDirectory.toAbsolutePath()
                                                    .toString(), "tempArchive.zip");
        archiveInfo.setArchivePath(archive.toString());
        service.createArchive(System.currentTimeMillis(), archiveInfo);

        // Add files and folders
        FileInfo fiFolder = new FileInfo(0, 0, "zip", 0L, 0L, 0L,
                                         LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(),
                                         "", "", 0, "", true, false, Collections.singletonMap(KEY_FILE_PATH,
                                                                                              folder.toAbsolutePath().toString())
        );
        FileInfo fiAnotherFolder = new FileInfo(1, 0, "zipster", 0L, 0L, 0L,
                                         LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(),
                                         "", "", 0, "", true, false, Collections.singletonMap(KEY_FILE_PATH,
                                                                                              anotherFolder.toAbsolutePath().toString())
        );
        FileInfo fiFile = new FileInfo(2, 0, "zippy-file", 0L, 0L, 0L,
                                         LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(),
                                         "", "", 0, "", false, false, Collections.singletonMap(KEY_FILE_PATH,
                                                                                              file.toAbsolutePath().toString())
        );

        service.addFile(System.currentTimeMillis(), archiveInfo, fiFolder, fiAnotherFolder, fiFile);
        Assertions.assertEquals(3,
                                readService.listFiles(System.currentTimeMillis(), archiveInfo).size(),
                                "File count was not as expected");
        Assertions.assertTrue(
                readService.listFiles(System.currentTimeMillis(), archiveInfo).stream().map(FileInfo::getFileName).anyMatch(f->f.equals("zip")),
                "File zip was not found");
        Assertions.assertTrue(
                readService.listFiles(System.currentTimeMillis(), archiveInfo).stream().map(FileInfo::getFileName).anyMatch(f->f.equals("zipster")),
                "File zipster was not found");
        Assertions.assertTrue(
                readService.listFiles(System.currentTimeMillis(), archiveInfo).stream().map(FileInfo::getFileName).anyMatch(f->f.equals("zippy-file")),
                "File zippy-file was not found");

        // Delete folder zip
        service.deleteFile(System.currentTimeMillis(), archiveInfo, fiFolder);

        Assertions.assertEquals(2,
                                readService.listFiles(System.currentTimeMillis(), archiveInfo).size(),
                                "File count was not as expected");
        Assertions.assertFalse(
                readService.listFiles(System.currentTimeMillis(), archiveInfo).stream().map(FileInfo::getFileName).anyMatch(f->f.equals("zip")),
                "File zip was found unexpectedly");
        Assertions.assertTrue(
                readService.listFiles(System.currentTimeMillis(), archiveInfo).stream().map(FileInfo::getFileName).anyMatch(f->f.equals("zipster")),
                "File zipster was not found");
        Assertions.assertTrue(
                readService.listFiles(System.currentTimeMillis(), archiveInfo).stream().map(FileInfo::getFileName).anyMatch(f->f.equals("zippy-file")),
                "File zippy-file was not found");
    }
}
