/*
 * Copyright © 2021 92AK
 */
package com.ntak.pearlzip.archive.szjb.pub;

import com.ntak.pearlzip.archive.pub.ArchiveReadService;
import com.ntak.pearlzip.archive.pub.FileInfo;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.ntak.pearlzip.archive.constants.ConfigurationConstants.CNS_COM_BUS_FACTORY;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public abstract class SevenZipArchiveServiceTestCore {

    private static String szFileName = SevenZipArchiveServiceTestCore.class.getClassLoader().getResource("test.7z").getFile();
    private static String cabFileName =
            SevenZipArchiveServiceTestCore.class.getClassLoader().getResource("test.cab").getFile();
    private static String isoFileName =
            SevenZipArchiveServiceTestCore.class.getClassLoader().getResource("test.iso").getFile();
    private static String rarFileName =
            SevenZipArchiveServiceTestCore.class.getClassLoader().getResource("test.rar").getFile();
    private static String tarFileName =
            SevenZipArchiveServiceTestCore.class.getClassLoader().getResource("test.tar").getFile();
    private static String bz2FileName =
            SevenZipArchiveServiceTestCore.class.getClassLoader().getResource("test.tar.bz2").getFile();
    private static String gzFileName =
            SevenZipArchiveServiceTestCore.class.getClassLoader().getResource("test.tar.gz").getFile();
    private static String xzFileName =
            SevenZipArchiveServiceTestCore.class.getClassLoader().getResource("test.tar.xz").getFile();
    private static String zipFileName = SevenZipArchiveServiceTestCore.class.getClassLoader().getResource("test.zip").getFile();

    private ArchiveReadService service;

    private static Path tempDirectory;

    @BeforeAll
    public static void setUpOnce() throws IOException {
        System.setProperty(CNS_COM_BUS_FACTORY, "com.ntak.testfx.MockCommunicationBusFactory");

        tempDirectory = Files.createTempDirectory("pz-text");
    }

    @BeforeEach
    public void setUp() {
        service = new SevenZipArchiveService();
    }

    @AfterAll
    public static void tearDown() throws IOException {
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
        Test cases:
        + List files in archive (zip, rar, iso, tar, cab, 7z)
        + List file in Compressor single file (gz, xz, bz2)
        + List file returns empty list when opening non-existent archive
        + Extract non-existent file
        + Extract file from archive (zip, rar, iso, tar, cab, 7z, gz, xz, bz2)
        + Extract file throw IOException
        + Test file success (zip, rar, iso, tar, cab, 7z, gz, xz, bz2)
        + Test file failure (broken archive)
     */

    ///// LIST FILES /////

    @Test
    @DisplayName("Test: List files for a valid 7Zip file will return recursive contents of the file")
    public void testListFiles_Valid7ZipFile_ReturnsContents() {
        List<String> expectations = new ArrayList<>(List.of("1151.txt", "da", "da/.DS_Store", "da/1204.txt"));
        expectations.sort(CharSequence::compare);
        listFilesForArchive(szFileName, expectations);
    }

    @Test
    @DisplayName("Test: List files for a valid Cab file will return recursive contents of the file")
    public void testListFiles_ValidCabFile_ReturnsContents() {
        List<String> expectations = new ArrayList<>(List.of("lala", "lala/1151.txt", "lala/da", "lala/da/1204.txt"));
        expectations.sort(CharSequence::compare);
        listFilesForArchive(cabFileName, expectations);
    }

    @Test
    @DisplayName("Test: List files for a valid RAR file will return recursive contents of the file")
    public void testListFiles_ValidRarFile_ReturnsContents() {
        List<String> expectations = new ArrayList<>(List.of("1151.txt", "da", "da/.DS_Store", "da/1204.txt"));
        expectations.sort(CharSequence::compare);
        listFilesForArchive(rarFileName, expectations);
    }

    @Test
    @DisplayName("Test: List files for a valid Tar file will return recursive contents of the file")
    public void testListFiles_ValidTarFile_ReturnsContents() {
        List<String> expectations = new ArrayList<>(List.of("1151.txt", "da", "da/.DS_Store", "da/1204.txt"));
        expectations.sort(CharSequence::compare);
        listFilesForArchive(tarFileName, expectations);
    }

        @Test
    @DisplayName("Test: List files for a valid Gzip file will return recursive contents of the file")
    public void testListFiles_ValidGzFile_ReturnsContents() {
        List<String> expectations = new ArrayList<>(List.of("test.tar"));
        expectations.sort(CharSequence::compare);
        listFilesForArchive(gzFileName, expectations);
    }

    @Test
    @DisplayName("Test: List files for a valid BZip file will return recursive contents of the file")
    public void testListFiles_ValidBz2File_ReturnsContents() {
        List<String> expectations = new ArrayList<>(List.of("test.tar"));
        expectations.sort(CharSequence::compare);
        listFilesForArchive(bz2FileName, expectations);
    }

    @Test
    @DisplayName("Test: List files for a valid XZ file will return recursive contents of the file")
    public void testListFiles_ValidXzFile_ReturnsContents() {
        List<String> expectations = new ArrayList<>(List.of("test.tar"));
        expectations.sort(CharSequence::compare);
        listFilesForArchive(xzFileName, expectations);
    }

    @Test
    @DisplayName("Test: List files for a valid Zip file will return recursive contents of the file")
    public void testListFiles_ValidZipFile_ReturnsContents() {
        List<String> expectations = new ArrayList<>(List.of("first-file", "second-file", "first-folder", "first-folder/.DS_Store",
                                             "first-folder/first-nested-file"));
        expectations.sort(CharSequence::compare);
        listFilesForArchive(zipFileName, expectations);
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
    @DisplayName("Test: Attempt to extract non-existentfiles for a valid 7Zip file is unsuccessful")
    public void testExtractFiles_InvalidSelectedFile_Skips() throws IOException {
        FileInfo fi = new FileInfo(0, 0, "non-existent-file", 0, 0, 0, null, null, null, "", "", 0, "", false, false,
                                   Collections.emptyMap());
        long sessionId = System.currentTimeMillis();
        final Path target = Path.of(tempDirectory.toString(), Paths.get(fi.getFileName()).getFileName().toString());
        Files.deleteIfExists(target);
        assertFalse(service.extractFile(sessionId, target, szFileName,
                                       fi), "Successfully extracted file");
        assertFalse(Files.exists(target), "File was not created");
    }

    @Test
    @DisplayName("Test: Extract files for a valid 7Zip file successfully")
    public void testExtractFiles_Valid7zipFile_ReturnsContents() throws IOException {
        long timestamp = System.currentTimeMillis();
        Optional<FileInfo> optFI =
                service.listFiles(timestamp, szFileName).stream().filter(f->!f.isFolder()).findFirst();
        extractFilesFromArchive(szFileName, optFI.get());
    }

    @Test
    @DisplayName("Test: Extract files for a valid Cab file successfully")
    public void testExtractFiles_ValidCabFile_ReturnsContents() throws IOException {
        long timestamp = System.currentTimeMillis();
        Optional<FileInfo> optFI =
                service.listFiles(timestamp, cabFileName).stream().filter(f->!f.isFolder()).findFirst();
        extractFilesFromArchive(cabFileName, optFI.get());
    }

    @Test
    @DisplayName("Test: Extract files for a valid Iso file successfully")
    public void testExtractFiles_ValidIsoFile_ReturnsContents() throws IOException {
        long timestamp = System.currentTimeMillis();
        Optional<FileInfo> optFI =
                service.listFiles(timestamp, isoFileName).stream().filter(f->!f.isFolder()).findFirst();
        extractFilesFromArchive(isoFileName, optFI.get());
    }

    @Test
    @DisplayName("Test: Extract files for a valid RAR file successfully")
    public void testExtractFiles_ValidRarFile_ReturnsContents() throws IOException {
        long timestamp = System.currentTimeMillis();
        Optional<FileInfo> optFI =
                service.listFiles(timestamp, rarFileName).stream().filter(f->!f.isFolder()).findFirst();
        extractFilesFromArchive(rarFileName, optFI.get());
    }

    @Test
    @DisplayName("Test: Extract files for a valid Tar file successfully")
    public void testExtractFiles_ValidTarFile_ReturnsContents() throws IOException {
        long timestamp = System.currentTimeMillis();
        Optional<FileInfo> optFI =
                service.listFiles(timestamp, tarFileName).stream().filter(f->!f.isFolder()).findFirst();
        extractFilesFromArchive(tarFileName, optFI.get());
    }

    @Test
    @DisplayName("Test: Extract files for a valid Bzip file successfully")
    public void testExtractFiles_ValidBz2File_ReturnsContents() throws IOException {
        long timestamp = System.currentTimeMillis();
        Optional<FileInfo> optFI =
                service.listFiles(timestamp, bz2FileName).stream().filter(f->!f.isFolder()).findFirst();
        extractFilesFromArchive(bz2FileName, optFI.get());
    }

    @Test
    @DisplayName("Test: Extract files for a valid Gzip file successfully")
    public void testExtractFiles_ValidGZFile_ReturnsContents() throws IOException {
        long timestamp = System.currentTimeMillis();
        Optional<FileInfo> optFI =
                service.listFiles(timestamp, gzFileName).stream().filter(f->!f.isFolder()).findFirst();
        extractFilesFromArchive(gzFileName, optFI.get());
    }

    @Test
    @DisplayName("Test: Extract files for a valid XZ file successfully")
    public void testExtractFiles_ValidXZFile_ReturnsContents() throws IOException {
        long timestamp = System.currentTimeMillis();
        Optional<FileInfo> optFI =
                service.listFiles(timestamp, xzFileName).stream().filter(f->!f.isFolder()).findFirst();
        extractFilesFromArchive(xzFileName, optFI.get());
    }

    @Test
    @DisplayName("Test: Extract files for a valid Zip file successfully")
    public void testExtractFiles_ValidZipFile_ReturnsContents() throws IOException {
        long timestamp = System.currentTimeMillis();
        Optional<FileInfo> optFI = service.listFiles(timestamp, zipFileName).stream().filter(f->!f.isFolder()).findFirst();
        extractFilesFromArchive(zipFileName, optFI.get());
    }

    ///// TEST FILES /////

    @Test
    @DisplayName("Test: Test 7Zip archive")
    public void testFiles_Valid7ZipFile_True() {
        long sessionId = System.currentTimeMillis();
        assertTrue(service.testArchive(sessionId, szFileName), "Archive not valid");
    }

    @Test
    @DisplayName("Test: Test Cab archive")
    public void testFiles_ValidCabFile_True() {
        long sessionId = System.currentTimeMillis();
        assertTrue(service.testArchive(sessionId, cabFileName), "Archive not valid");
    }

    @Test
    @DisplayName("Test: Test Iso archive")
    public void testFiles_ValidIsoFile_True() {
        long sessionId = System.currentTimeMillis();
        assertTrue(service.testArchive(sessionId, isoFileName), "Archive not valid");
    }

    @Test
    @DisplayName("Test: Test Rar archive")
    public void testFiles_ValidRarFile_True() {
        long sessionId = System.currentTimeMillis();
        assertTrue(service.testArchive(sessionId, rarFileName), "Archive not valid");
    }

    @Test
    @DisplayName("Test: Test Tar archive")
    public void testFiles_ValidTarFile_True() {
        long sessionId = System.currentTimeMillis();
        assertTrue(service.testArchive(sessionId, tarFileName), "Archive not valid");
    }

    @Test
    @DisplayName("Test: Test GZip archive")
    public void testFiles_ValidGZipFile_True() {
        long sessionId = System.currentTimeMillis();
        assertTrue(service.testArchive(sessionId, gzFileName), "Archive not valid");
    }

    @Test
    @DisplayName("Test: Test XZ archive")
    public void testFiles_ValidXZFile_True() {
        long sessionId = System.currentTimeMillis();
        assertTrue(service.testArchive(sessionId, xzFileName), "Archive not valid");
    }

    @Test
    @DisplayName("Test: Test Bzip archive")
    public void testFiles_ValidBz2File_True() {
        long sessionId = System.currentTimeMillis();
        assertTrue(service.testArchive(sessionId, bz2FileName), "Archive not valid");
    }

    @Test
    @DisplayName("Test: Test Zip archive")
    public void testFiles_ValidZipFile_True() {
        long sessionId = System.currentTimeMillis();
        assertTrue(service.testArchive(sessionId, zipFileName), "Archive not valid");
    }

    @Test
    @DisplayName("Test: Test invalid archive")
    public void testFiles_InvalidArchive_False() {
        long sessionId = System.currentTimeMillis();
        assertFalse(service.testArchive(sessionId, SevenZipArchiveServiceTestCore.class.getClassLoader().getResource(
                "broken.tar.xz").getFile()), "Archive valid unexpectedly");
    }

    ///// UTILITY METHODS /////

    public void extractFilesFromArchive(String archiveFile, FileInfo file) throws IOException {
        long sessionId = System.currentTimeMillis();
        final Path target = Path.of(tempDirectory.toString(), Paths.get(file.getFileName()).getFileName().toString());
        Files.deleteIfExists(target);
        assertTrue(service.extractFile(sessionId, target, archiveFile,
                                       file), "Successfully extracted file");
        assertTrue(Files.exists(target), "File was not created");
    }

    public void listFilesForArchive(String archiveFile, List<String> expectations) {
        long sessionId = System.currentTimeMillis();
        List<FileInfo> files = service.listFiles(sessionId, archiveFile);
        Assertions.assertNotNull(files, "Files should not be null");
        Assertions.assertEquals(expectations.size(), files.size(), String.format("Files should contain %d files/folders",
         expectations.size()));
        int i = 0;
        List<String> fileNames = files.stream()
                                      .map(FileInfo::getFileName)
                                      .sorted(CharSequence::compare)
                                      .collect(Collectors.toList());

        for (String file : fileNames) {
            Assertions.assertEquals(expectations.get(i), file, String.format("File: %s was not found as " +
                                                                                     "expected",
                                                                             expectations.get(i)));
            i++;
        }
    }
}
