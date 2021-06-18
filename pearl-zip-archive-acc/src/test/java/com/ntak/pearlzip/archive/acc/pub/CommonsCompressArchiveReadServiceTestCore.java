/*
 * Copyright © 2021 92AK
 */
package com.ntak.pearlzip.archive.acc.pub;

import com.ntak.pearlzip.archive.pub.FileInfo;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public abstract class CommonsCompressArchiveReadServiceTestCore {

    private CommonsCompressArchiveReadService service;
    private static Path testArchive = Paths.get(".", "src", "test", "resources", "test.tar");
    private static Path faultyArchive = Paths.get(".", "src", "test", "resources", "faulty-archive.tar");
    private static Path emptyArchive = Paths.get(".", "src", "test", "resources", "empty-archive.tar");
    private static Path tempDirectory;

    @BeforeAll
    public static void setUpOnce() throws IOException {
        tempDirectory = Files.createTempDirectory("pz-text");
    }

    @BeforeEach
    public void setUp() {
        service = new CommonsCompressArchiveReadService();
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
     *  Test cases:
     *  + List contents of tar file
     *  + Extract file from tar archive
     *  + Test archive (empty tar file) - success
     *  + Test archive (non-empty tar file) - success
     *  + Test archive - failure
     */

    @Test
    @DisplayName("Test: List contents of a tarball successfully")
    public void testListContents_ValidArchive_Success() {
        List<String> expectations = new ArrayList<>(List.of("1151.txt", "da/.DS_Store", "da/1204.txt", "da"));
        expectations.sort(CharSequence::compare);
        listFilesForArchive(testArchive.toAbsolutePath().toString(), expectations);
    }

    @Test
    @DisplayName("Test: Extract a single file from a tarball successfully")
    public void testExtractFile_ValidArchive_Success() throws IOException {
        Optional<FileInfo> optFI =
                service.listFiles(0L, testArchive.toAbsolutePath().toString()).stream().filter(f->!f.isFolder()).findFirst();
        Assertions.assertTrue(optFI.isPresent(), "No file was retrieved from archive to extract...");
        extractFilesFromArchive(testArchive.toAbsolutePath().toString(), optFI.get());
    }

    @Test
    @DisplayName("Test: Test empty archive returns success")
    public void testTestArchive_EmptyArchive_Success() {
        long sessionId = System.currentTimeMillis();
        assertTrue(service.testArchive(sessionId, emptyArchive.toAbsolutePath().toString()), "Archive not valid");
    }

    @Test
    @DisplayName("Test: Test non-empty archive returns success")
    public void testTestArchive_NonEmptyArchive_Success() {
        long sessionId = System.currentTimeMillis();
        assertTrue(service.testArchive(sessionId, testArchive.toAbsolutePath().toString()), "Archive not valid");
    }

    @Test
    @DisplayName("Test: Test faulty archive returns failure")
    public void testTestArchive_FaultyArchive_Fail() {
        long sessionId = System.currentTimeMillis();
        assertFalse(service.testArchive(sessionId, faultyArchive.toAbsolutePath().toString()), "Archive unexpectedly valid");

    }

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
