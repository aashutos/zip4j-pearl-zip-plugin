/*
 * Copyright © 2021 92AK
 */
package com.ntak.pearlzip.ui.util;

import com.ntak.pearlzip.archive.pub.ArchiveInfo;
import com.ntak.pearlzip.archive.pub.ArchiveReadService;
import com.ntak.pearlzip.archive.pub.ArchiveWriteService;
import com.ntak.pearlzip.archive.pub.FileInfo;
import com.ntak.pearlzip.ui.constants.ZipConstants;
import com.ntak.pearlzip.ui.model.FXArchiveInfo;
import com.ntak.pearlzip.ui.model.ZipState;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

import static com.ntak.pearlzip.ui.UITestSuite.clearDirectory;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.*;

public class ArchiveUtilTest {

    private static MessageDigest digest;
    private static FXArchiveInfo mockArchiveInfo;
    private static ArchiveReadService mockArchiveReadService;
    private static ArchiveWriteService mockArchiveWriteService;
    private static Path tempDirectory;
    private static Menu menuRecent;

    /*
        Test cases:
        + Extract with null directory passed in
        + Extract to a non-existent directory
        + Extract successfully

        + Back up archive successfully (hash check SHA256)

        + Handle directory. List all files/folders in specified directory

        + Recent files - Create files up to 5 files New up to 5 files (NO_FILES_HISTORY=5). No overwrite
        + Recent files - Add sixth file (with NO_FILES_HISTORY=5). Overwrite oldest

        + Render menu items for recent files as expected
        + Render menu items - exclude non-existent entries. Therefore entries <= NO_FILES_HISTORY

        + create new Archive successfully when conditions are right to do so
    */

    @BeforeAll
    public static void setUpOnce() throws NoSuchAlgorithmException, IOException {
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException e) {

        } finally {
            menuRecent = new Menu();

            digest = MessageDigest.getInstance("SHA-256");
            tempDirectory = Files.createTempDirectory("pz");
            ZipConstants.RECENT_FILE = Paths.get(tempDirectory.toAbsolutePath()
                                                              .toString(), "rf");

            mockArchiveInfo = Mockito.mock(FXArchiveInfo.class);
            mockArchiveReadService = Mockito.mock(ArchiveReadService.class);
            when(mockArchiveReadService.supportedReadFormats()).thenReturn(List.of("zip"));

            mockArchiveWriteService = Mockito.mock(ArchiveWriteService.class);
            when(mockArchiveWriteService.supportedWriteFormats()).thenReturn(List.of("zip"));

            when(mockArchiveInfo.getArchivePath()).thenReturn(Paths.get("src", "test", "resources", "test.zip")
                                                                   .toString());
            when(mockArchiveInfo.getFiles()).thenReturn(FXCollections.observableArrayList(List.of(
                    new FileInfo(0, 0, "folder", 0, 0, 0,
                                 LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(), "user", "group",
                                 0, "", true, false, Collections.emptyMap()),
                    new FileInfo(1, 0, "file", 0, 0, 0,
                                 LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(), "user", "group",
                                 0, "", false, false, Collections.emptyMap()),
                    new FileInfo(2, 1, "inner-file", 0, 0, 0,
                                 LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(), "user", "group",
                                 0, "", false, false, Collections.emptyMap()),
                    new FileInfo(3, 1, "another-inner-file", 0, 0, 0,
                                 LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(), "user", "group",
                                 0, "", false, false, Collections.emptyMap())
            )));
            when(mockArchiveInfo.getReadService()).thenReturn(mockArchiveReadService);

            ZipState.addArchiveProvider(mockArchiveWriteService);
            ZipState.addArchiveProvider(mockArchiveReadService);
        }
    }

    @AfterAll
    public static void tearDownOnce() throws IOException {
        clearDirectory(tempDirectory);
    }

    @AfterEach
    public void tearDown() throws IOException {
        Files.deleteIfExists(ZipConstants.RECENT_FILE);
    }

    @Test
    @DisplayName("Test: Extract to directory with no directory will not execute")
    public void testExtractToDirectory_NullDirectory_Nothing() {
        ArchiveUtil.extractToDirectory(1L, mockArchiveInfo, null);
        verify(mockArchiveInfo, never()).getFiles();
    }

    @Test
    @DisplayName("Test: Extract to directory with non-existent directory will not execute")
    public void testExtractToDirectory_NonExistentDirectory_Nothing() {
        ArchiveUtil.extractToDirectory(2L, mockArchiveInfo, Paths.get("non-existent-path").toFile());
        verify(mockArchiveInfo, never()).getFiles();
    }

    @Test
    @DisplayName("Test: Extract to directory with valid directory will execute")
    public void testExtractToDirectory_ValidDirectory_Success() {
        ArchiveUtil.extractToDirectory(2L, mockArchiveInfo, tempDirectory.toFile());
        verify(mockArchiveInfo, times(1)).getFiles();
        verify(mockArchiveReadService,
               times((int)mockArchiveInfo.getFiles().stream().filter(f->!f.isFolder()).count())).extractFile(anyLong(),
                                                                                                          any(Path.class),
                                                                                                             anyString(),
                                                                                                             any(FileInfo.class));
    }

    @Test
    @DisplayName("Test: Back up existing archive successfully")
    public void testBackUpArchiver_ExistingArchive_Success() throws IOException {
        Path archive = Path.of(mockArchiveInfo.getArchivePath());
        Path backup = ArchiveUtil.createBackupArchive(mockArchiveInfo, tempDirectory);

        Assertions.assertTrue(Files.exists(archive), "Archive does not exist");
        Assertions.assertTrue(Files.exists(backup), "Back up does not exist");
        try (
                InputStream archiveStream = Files.newInputStream(archive);
                InputStream backupStream = Files.newInputStream(backup)
        ) {
            long archiveHash = ByteBuffer.wrap(digest.digest(archiveStream.readAllBytes())).getLong();
            long backupHash = ByteBuffer.wrap(digest.digest(backupStream.readAllBytes())).getLong();
            String archiveHashString = Long.toHexString(archiveHash).toUpperCase();
            String backupHashString = Long.toHexString(backupHash).toUpperCase();

            Assertions.assertEquals(archiveHash, backupHash,
                String.format(
                        """
                              The archive and back up SHA-256 hashes were not identical. Details:
                              + Archive SHA-256 = 0x%s
                              + Back up SHA=256 = 0x%s
                        """,
                        archiveHashString, backupHashString)
            );
        }
    }

    @Test
    @DisplayName("Test: Handle directory of a valid directory will list all files and directories under the specified directory")
    public void testHandleDirectory_MatchExpectations() throws IOException {
        String[] dirs = {"top-level", "top-level/subdir", "top-level/subdir2", "top-level/subdir/subdir3"};
        String[] files = {"a-file", "top-level/subdir/b-file", "top-level/subdir2/c-file",
                          "top-level/subdir/subdir3/a-file"};
        Path tempDirectory = Files.createTempDirectory("pz");

        Arrays.stream(dirs).forEach(d -> {
            try {
                Files.createDirectory(Paths.get(tempDirectory.toAbsolutePath().toString(), d));
            } catch(IOException e) {
            }
        });
        Arrays.stream(files).forEach(f -> {
            try {
                Files.createFile(Paths.get(tempDirectory.toAbsolutePath().toString(), f));
            } catch(IOException e) {
            }
        });
        List<FileInfo> detectedFiles = ArchiveUtil.handleDirectory("", tempDirectory, tempDirectory,0,0);

        Assertions.assertEquals(dirs.length + files.length, detectedFiles.size(), "The expected number of files " +
                "were not returned");
        List<String> filenames = detectedFiles.stream().map(FileInfo::getFileName).collect(Collectors.toList());
        Arrays.stream(dirs).forEach(d->Assertions.assertTrue(filenames.contains(d),
                                                             String.format("File %s not found", d)));
        Arrays.stream(files).forEach(f->Assertions.assertTrue(filenames.contains(f),
                                                             String.format("File %s not found", f)));

        // clean up temporary directory...
        clearDirectory(tempDirectory);
    }

    @Test
    @DisplayName("Test: Adding first 5 recent files keeps all entries")
    public void testAddRecentFile_NoOverwrite_Success() throws IOException {
        for (int i = 1; i <= 5; i++) {
            Path file = Path.of(tempDirectory.toAbsolutePath().toString(), String.format("recent-file-%d", i));
            Files.deleteIfExists(file);
            Files.createFile(file);
            ArchiveUtil.addToRecentFile(file.toFile());
            final List<String> contents = Files.readAllLines(ZipConstants.RECENT_FILE);
            Assertions.assertEquals(contents.size(),
                                    i,
                                    String.format("After adding file %d. The expected number of files were not kept",
                                                  i));
            for (int j = 1; j <= i; j++) {
                int fileIndex = j;
                Assertions.assertEquals(1,
                                        contents.stream()
                                                .filter(l -> l.matches(String.format(".*recent-file-%d$", fileIndex)))
                                                .count(),
                                        String.format("File recent-file-%d was not found", j));
            }
        }
    }

    @Test
    @DisplayName("Test: Adding sixth-most recent file, replaces oldest entry")
    public void testAddRecentFile_WithOverwrite_Success() throws IOException {
        // Prepare first five entries
        testAddRecentFile_NoOverwrite_Success();

        // Prepare sixth entry
        Path file = Path.of(tempDirectory.toAbsolutePath().toString(), "recent-file-6");
        Files.createFile(file);
        ArchiveUtil.addToRecentFile(file.toFile());

        // Apply sixth entry
        final List<String> contents = Files.readAllLines(ZipConstants.RECENT_FILE);

        // Confirm only 5 most recent entries exist
        Assertions.assertEquals(5, contents.size(), "After adding file 6. The expected number of files were not kept");
        for (int j = 2; j <= 6; j++) {
            int fileIndex = j;
            Assertions.assertEquals(1,
                                    contents.stream()
                                            .filter(l -> l.matches(String.format(".*recent-file-%d$", fileIndex)))
                                            .count(),
                                    String.format("File recent-file-%d was not found", j));
        }

        Assertions.assertNotEquals(1,
                                   contents.stream()
                                           .filter(l -> l.matches(".*recent-file-1$"))
                                           .count(),
                                   "File recent-file-1 was unexpectedly found");
    }

    @Test
    @DisplayName("Test: Render menu items for recent files as expected")
    public List<MenuItem> testRenderRecentMenu_Success() throws IOException, InterruptedException {
        String[] expectations = {".*recent-file-1$",".*recent-file-2$",
                                 ".*recent-file-3$",".*recent-file-4$",
                                 ".*recent-file-5$"};

        // Prepare first five entries
        testAddRecentFile_NoOverwrite_Success();
        CountDownLatch latch = new CountDownLatch(1);

        Platform.runLater(()->{
            ArchiveUtil.refreshRecentFileMenu(menuRecent);
            latch.countDown();
        });
        latch.await();

        final ObservableList<MenuItem> items = menuRecent.getItems();
        Assertions.assertEquals(expectations.length, items.size(), "The number of items were not as expected");

        for (String regex : expectations) {
            Assertions.assertTrue(items.stream().anyMatch(i-> i.getText().matches(regex)),
                                  String.format("No item in matched pattern %s", regex)
            );
        }

        return items;
    }

    @Test
    @DisplayName("Test: Render menu items for recent files that exist only")
    public void testRenderRecentMenu_NonExistentFile_MatchExpectations() throws IOException, InterruptedException {
        // Prepare menu with 5 entries
        List<MenuItem> items = testRenderRecentMenu_Success();
        String[] expectations = {".*recent-file-2$",
                                 ".*recent-file-3$",
                                 ".*recent-file-4$",
                                 ".*recent-file-5$"};

        // Delete one file
        Optional<MenuItem> item;
        if ((item = items.stream().filter(f->f.getText().matches(".*recent-file-1$")).findFirst()).isPresent()) {
            Files.deleteIfExists(Paths.get(item.get().getText().split(" ")[1]));

            CountDownLatch latch = new CountDownLatch(1);

            Platform.runLater(()->{
                ArchiveUtil.refreshRecentFileMenu(menuRecent);
                latch.countDown();
            });
            latch.await();

            items = menuRecent.getItems();
            Assertions.assertEquals(expectations.length, items.size(), "The number of items were not as expected");

            for (String regex : expectations) {
                Assertions.assertTrue(items.stream().anyMatch(i-> i.getText().matches(regex)),
                                      String.format("No item in matched pattern %s", regex)
                );
            }
        } else {
            fail("No data was found in recent files");
        }
    }

    @Test
    @DisplayName("Test: Create new archive successfully")
    public void testCreateArchive_Success() throws IOException {
        ArchiveInfo archiveInfo = new ArchiveInfo();
        archiveInfo.setArchiveFormat("zip");

        final Path archive = Paths.get(tempDirectory.toAbsolutePath()
                                                 .toString(),
                                    "temp-archive.zip");
        Files.deleteIfExists(archive);
        Files.createFile(archive);

        archiveInfo.setArchivePath(archive.toAbsolutePath().toString());

        ArchiveUtil.newArchive(1L, archiveInfo, archive.toFile());
        verify(mockArchiveWriteService, times(1)).createArchive(anyLong(), any(ArchiveInfo.class));
    }
}
