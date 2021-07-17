/*
 * Copyright © 2021 92AK
 */
package com.ntak.pearlzip.ui.testfx;

import com.ntak.pearlzip.archive.pub.FileInfo;
import com.ntak.pearlzip.archive.util.CompressUtil;
import com.ntak.pearlzip.ui.UITestFXSuite;
import com.ntak.pearlzip.ui.UITestSuite;
import com.ntak.pearlzip.ui.constants.ZipConstants;
import com.ntak.pearlzip.ui.model.FXArchiveInfo;
import com.ntak.pearlzip.ui.util.AbstractPearlZipTestFX;
import com.ntak.pearlzip.ui.util.PearlZipFXUtil;
import com.ntak.testfx.FormUtil;
import javafx.geometry.Point2D;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.ntak.pearlzip.ui.util.PearlZipFXUtil.*;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

@Tag("fx-test")
public class AddToArchiveTestFX extends AbstractPearlZipTestFX {

    private static Path tempDirRoot;
    private static Path dir;

    /*
     * Test cases:
     * + Add folder to zip archive
     * + Add folder to tar archive
     * + Add folder to jar archive
     * + Add symbolic soft link file to zip archive
     * + Add symbolic hard link file and document file to tar archive
     * + Add image file to jar archive
     * + Add long name file to tar archive
     * + Table context menu Add File
     * + Table context menu Add Directory
     * + Add file to a no longer existing archive
     * + Add folder to a no longer existing archive
     * + Add self to archive raises warning
     * + Add directory with self to archive. Ignores self on addition
     */

    @BeforeEach
    public void setUp() {
        try {
            tempDirRoot = Files.createTempDirectory("pz");
            dir = UITestFXSuite.genSourceDataSet();
        } catch(IOException e) {
        }
    }

    @AfterEach
    public void tearDown() {
        try {
            super.tearDown();
            for (Path dir :
                    Files.list(dir.getParent().getParent()).filter(p->p.getFileName().toString().startsWith("pz")).collect(
                            Collectors.toSet())) {
                UITestSuite.clearDirectory(dir);
            }
        } catch(Exception e) {
        }
    }

    @Test
    @DisplayName("Test: Add folder to zip archive and verify contents")
    public void testFX_AddFolderToZipArchive_MatchExpectations() throws IOException {
        final String archiveFormat = "zip";
        final String archiveName = String.format("test%s.%s", archiveFormat, archiveFormat);

        final Path archive = Paths.get(System.getProperty("user.home"), ".pz", "temp", archiveName);
        simAddDirectoryToNewNonCompressorArchive(this, archive, dir, false);
    }

    @Test
    @DisplayName("Test: Add folder using context menu to zip archive and verify contents")
    public void testFX_AddFolderCtxMenuToZipArchive_MatchExpectations() throws IOException {
        final String archiveFormat = "zip";
        final String archiveName = String.format("test%s.%s", archiveFormat, archiveFormat);

        final Path archive = Paths.get(System.getProperty("user.home"), ".pz", "temp", archiveName);
        simAddDirectoryToNewNonCompressorArchive(this, archive, dir, true);
    }

    @Test
    @DisplayName("Test: Add folder to tar archive and verify contents")
    public void testFX_AddFolderToTarArchive_MatchExpectations() throws IOException {
        final String archiveFormat = "tar";
        final String archiveName = String.format("test%s.%s", archiveFormat, archiveFormat);

        final Path archive = Paths.get(System.getProperty("user.home"), ".pz", "temp", archiveName);
        simAddDirectoryToNewNonCompressorArchive(this, archive, dir, false);
    }

    @Test
    @DisplayName("Test: Add folder to jar archive and verify contents")
    public void testFX_AddFolderToJarArchive_MatchExpectations() throws IOException {
        final String archiveFormat = "jar";
        final String archiveName = String.format("test%s.%s", archiveFormat, archiveFormat);

        final Path archive = Paths.get(System.getProperty("user.home"), ".pz", "temp", archiveName);
        simAddDirectoryToNewNonCompressorArchive(this, archive, dir, false);
    }

    @Test
    @DisplayName("Test: Add symbolic soft link file to zip archive and verify contents")
    public void testFX_AddSymSoftLinkFileToZipArchive_MatchExpectations() throws IOException {
        final String archiveFormat = "zip";
        final String archiveName = String.format("test%s.%s", archiveFormat, archiveFormat);

        Path file = Paths.get("src", "test", "resources", "test.lnk").toAbsolutePath();
        final long sourceHash = CompressUtil.crcHashFile(file.toFile());
        final Path archive = Paths.get(System.getProperty("user.home"), ".pz", "temp", archiveName);
        simNewArchive(this, archive);
        simAddFile(this, file);
        push(KeyCode.ENTER);
        TableView<FileInfo> fileContentsView = FormUtil.lookupNode(s->s.getTitle().contains(archiveName),
                                                                   "#fileContentsView");
        Assertions.assertEquals("test.zip", fileContentsView.getItems().get(0).getFileName(), "Shortcut was not " +
                "followed to original file");

        // Extract file and check consistency
        Path targetFile = Paths.get(dir.getParent().toAbsolutePath().toString(), file.getFileName().toString()).toAbsolutePath();
        FormUtil.selectTableViewEntry(this, fileContentsView,
                                      FileInfo::getFileName, file.getFileName().toString());
        simExtractFile(this, targetFile);
        final long targetHash = CompressUtil.crcHashFile(targetFile.toFile());

        Assertions.assertEquals(sourceHash, targetHash, "File hashes were not identical");
    }

    @Test
    @DisplayName("Test: Add symbolic hard link and document file to tar archive and verify contents")
    public void testFX_AddSymHardLinkFileToTarArchive_MatchExpectations() throws IOException {
        final String archiveFormat = "tar";
        final String archiveName = String.format("test%s.%s", archiveFormat, archiveFormat);

        Path fileHardLink = Paths.get("src", "test", "resources", "test-hard.lnk").toAbsolutePath();
        Path fileDoc = Paths.get("src", "test", "resources", "test.docx").toAbsolutePath();
        final Path archive = Paths.get(System.getProperty("user.home"), ".pz", "temp", archiveName);
        simNewArchive(this, archive);
        simAddFile(this, fileHardLink);
        push(KeyCode.ENTER);
        simAddFile(this, fileDoc);
        push(KeyCode.ENTER);

        TableView<FileInfo> fileContentsView = FormUtil.lookupNode(s->s.getTitle().contains(archiveName),
                                                                   "#fileContentsView");
        final List<String> files = fileContentsView.getItems()
                                                     .stream()
                                                     .map(FileInfo::getFileName)
                                                     .collect(Collectors.toList());
        Assertions.assertTrue(files.contains("test-hard.lnk"),
                              "Hard link was not found in archive");
        Assertions.assertTrue(files.contains("test.docx"), "Document was not found in archive");

        // Extract file and check consistency
        for (Path file : Arrays.asList(fileDoc, fileHardLink)) {
            final long sourceHash = CompressUtil.crcHashFile(file.toFile());
            Path targetFile = Paths.get(dir.getParent()
                                           .toAbsolutePath()
                                           .toString(),
                                        file.getFileName()
                                            .toString())
                                   .toAbsolutePath();
            FormUtil.selectTableViewEntry(this,
                                          fileContentsView,
                                          FileInfo::getFileName,
                                          file.getFileName()
                                              .toString());
            simExtractFile(this, targetFile);
            final long targetHash = CompressUtil.crcHashFile(targetFile.toFile());

            Assertions.assertEquals(sourceHash, targetHash, "File hashes were not identical");
        }
    }

    @Test
    @DisplayName("Test: Add image file using context menu to tar archive and verify contents")
    public void testFX_AddImageFileCtxMenuToZipArchive_MatchExpectations() throws IOException {
        final String archiveFormat = "jar";
        final String archiveName = String.format("test%s.%s", archiveFormat, archiveFormat);

        Path file = Paths.get("src", "test", "resources", "img.png").toAbsolutePath();
        final long sourceHash = CompressUtil.crcHashFile(file.toFile());
        final Path archive = Paths.get(System.getProperty("user.home"), ".pz", "temp", archiveName);
        simNewArchive(this, archive);
        simAddFile(this, file, true, archiveName);

        TableView<FileInfo> fileContentsView = FormUtil.lookupNode(s->s.getTitle().contains(archiveName),
                                                                   "#fileContentsView");
        final List<String> files = fileContentsView.getItems()
                                                   .stream()
                                                   .map(FileInfo::getFileName)
                                                   .collect(Collectors.toList());
        Assertions.assertTrue(files.contains("img.png"),
                              "Image was not found in archive");

        // Extract file and check consistency
        Path targetFile = Paths.get(dir.getParent().toAbsolutePath().toString(), file.getFileName().toString()).toAbsolutePath();
        FormUtil.selectTableViewEntry(this, fileContentsView,
                                      FileInfo::getFileName, file.getFileName().toString());
        simExtractFile(this, targetFile);
        final long targetHash = CompressUtil.crcHashFile(targetFile.toFile());

        Assertions.assertEquals(sourceHash, targetHash, "File hashes were not identical");
    }

    @Test
    @DisplayName("Test: Add image file to jar archive and verify contents")
    public void testFX_AddImageFileToJarArchive_MatchExpectations() throws IOException {
        final String archiveFormat = "jar";
        final String archiveName = String.format("test%s.%s", archiveFormat, archiveFormat);

        Path file = Paths.get("src", "test", "resources", "img.png").toAbsolutePath();
        final long sourceHash = CompressUtil.crcHashFile(file.toFile());
        final Path archive = Paths.get(System.getProperty("user.home"), ".pz", "temp", archiveName);
        simNewArchive(this, archive);
        simAddFile(this, file);

        TableView<FileInfo> fileContentsView = FormUtil.lookupNode(s->s.getTitle().contains(archiveName),
                                                                   "#fileContentsView");
        final List<String> files = fileContentsView.getItems()
                                                   .stream()
                                                   .map(FileInfo::getFileName)
                                                   .collect(Collectors.toList());
        Assertions.assertTrue(files.contains("img.png"),
                              "Image was not found in archive");

        // Extract file and check consistency
        Path targetFile = Paths.get(dir.getParent().toAbsolutePath().toString(), file.getFileName().toString()).toAbsolutePath();
        FormUtil.selectTableViewEntry(this, fileContentsView,
                                      FileInfo::getFileName, file.getFileName().toString());
        simExtractFile(this, targetFile);
        final long targetHash = CompressUtil.crcHashFile(targetFile.toFile());

        Assertions.assertEquals(sourceHash, targetHash, "File hashes were not identical");
    }

    @Test
    @DisplayName("Test: Add long name file to tar archive and verify contents")
    public void testFX_AddLongNameFileToTarArchive_MatchExpectations() throws IOException {
        final String archiveFormat = "tar";
        final String archiveName = String.format("test%s.%s", archiveFormat, archiveFormat);

        Path file =
                Paths.get(ZipConstants.LOCAL_TEMP.toAbsolutePath().toString(),
                          "QuickBrownFoxJumpsOverTheLazyDog01234567890_QuickBrownFoxJumpsOverTheLazyDog01234567890_QuickBrownFoxJumpsOverTheLazyDog01234567890_QuickBrownFoxJumpsOverTheLazyDog01234567890");
        Files.deleteIfExists(file);
        Files.createFile(file);
        final long sourceHash = CompressUtil.crcHashFile(file.toFile());
        final Path archive = Paths.get(System.getProperty("user.home"), ".pz", "temp", archiveName);
        simNewArchive(this, archive);
        simAddFile(this, file);

        TableView<FileInfo> fileContentsView = FormUtil.lookupNode(s->s.getTitle().contains(archiveName),
                                                                   "#fileContentsView");
        final List<String> files = fileContentsView.getItems()
                                                   .stream()
                                                   .map(FileInfo::getFileName)
                                                   .collect(Collectors.toList());
        Assertions.assertTrue(files.contains(file.getFileName().toString().substring(0,100)),
                              "File was not found in archive");

        // Extract file and check consistency
        Path targetFile = Paths.get(dir.getParent().toAbsolutePath().toString(), file.getFileName().toString()).toAbsolutePath();
        FormUtil.selectTableViewEntry(this, fileContentsView,
                                      FileInfo::getFileName, file.getFileName().toString().substring(0,100));
        simExtractFile(this, targetFile);
        final long targetHash = CompressUtil.crcHashFile(targetFile.toFile());

        Assertions.assertEquals(sourceHash, targetHash, "File hashes were not identical");
    }

    @Test
    @DisplayName("Test: Add file to a non-existent archive will raise the appropriate exception alert")
    public void testFX_AddFileNonExistentArchive_Fail() throws IOException {
        final String archiveFormat = "zip";
        final String archiveName = String.format("test%s.%s", archiveFormat, archiveFormat);

        final Path archive = Paths.get(System.getProperty("user.home"), ".pz", "temp", archiveName);
        simNewArchive(this, archive);
        Files.deleteIfExists(archive);

        clickOn("#btnAdd", MouseButton.PRIMARY);
        sleep(50, MILLISECONDS);

        clickOn("#mnuAddFile", MouseButton.PRIMARY);
        sleep(50, MILLISECONDS);

        DialogPane dialogPane = lookup(".dialog-pane").queryAs(DialogPane.class);
        Assertions.assertTrue(dialogPane.getContentText().matches("Archive .* does not exist. PearlZip will now close the instance."), "The text in warning dialog was not matched as expected");
    }

    @Test
    @DisplayName("Test: Add folder to a non-existent archive will raise the appropriate exception alert")
    public void testFX_AddFolderNonExistentArchive_Fail() throws IOException {
        final String archiveFormat = "zip";
        final String archiveName = String.format("test%s.%s", archiveFormat, archiveFormat);

        final Path archive = Paths.get(System.getProperty("user.home"), ".pz", "temp", archiveName);
        simNewArchive(this, archive);
        Files.deleteIfExists(archive);

        clickOn("#btnAdd", MouseButton.PRIMARY);
        sleep(50, MILLISECONDS);

        clickOn("#mnuAddDir", MouseButton.PRIMARY);
        sleep(50, MILLISECONDS);

        DialogPane dialogPane = lookup(".dialog-pane").queryAs(DialogPane.class);
        Assertions.assertTrue(dialogPane.getContentText().matches("Archive .* does not exist. PearlZip will now close the instance."), "The text in warning dialog was not matched as expected");
    }

    @Test
    @DisplayName("Test: Add self to archive raises warning")
    public void testFX_AddSelfToArchive_Warn() throws IOException {
        final String archiveFormat = "zip";
        final String archiveName = String.format("test%s.%s", archiveFormat, archiveFormat);

        final Path archive = Paths.get(System.getProperty("user.home"), ".pz", "temp", archiveName);
        simNewArchive(this, archive);
        simAddFile(this, archive);
        sleep(100, MILLISECONDS);

        DialogPane dialogPane = lookup(".dialog-pane").queryAs(DialogPane.class);
        Assertions.assertTrue(dialogPane.getContentText().matches("Ignoring the addition of file .* into the archive .*"),
                              "The text in warning dialog was not matched as expected");
    }

    @Test
    @DisplayName("Test: Add directory with self to archive. Ignores self on addition")
    public void testFX_AddDirectoryWithSelf_Ignore() throws IOException {
        final String archiveFormat = "zip";
        final String archiveName = String.format("test%s.%s", archiveFormat, archiveFormat);

        final Path archive = Paths.get(System.getProperty("user.home"), ".pz", "temp", archiveName);
        simNewArchive(this, archive);
        simAddFolder(this, archive.getParent());
        sleep(100, MILLISECONDS);

        Optional<FXArchiveInfo> optArchiveInfo = lookupArchiveInfo(archiveName);
        Assertions.assertTrue(optArchiveInfo.isPresent(), "Archive window not open");
        Assertions.assertTrue(optArchiveInfo.get().getFiles().stream().noneMatch(f->f.getFileName().endsWith(archiveName)), "Archive was added unexpectedly");
    }

    @Test
    @DisplayName("Test: Nest zip archive into the parent zip archive and verify contents is as expected")
    public void testFX_CreateZipArchiveAndUpdateNestedZipArchive_Success() throws IOException {
        // Create archive
        String archiveFormat = "zip";
        final String archiveName = String.format("nest-test.%s", archiveFormat);
        Path archivePath = Paths.get(tempDirRoot.toAbsolutePath().toString(), archiveName);
        final String nestedArchiveName = "nested-archive.zip";
        final Path nestedArchivePath = Paths.get("src", "test", "resources", nestedArchiveName).toAbsolutePath();
        final Path file = Files.createTempFile("","");
        Files.deleteIfExists(file);
        Files.createFile(file);
        PearlZipFXUtil.simNewArchive(this, archivePath);

        // Add nested archive
        PearlZipFXUtil.simAddFile(this, nestedArchivePath);
        sleep(50, MILLISECONDS);

        // Open nested zip archive
        TableRow row = PearlZipFXUtil.simTraversalArchive(this, archiveName, "#fileContentsView", (r)->{},
                                                          nestedArchiveName).get();
        sleep(250, MILLISECONDS);
        doubleClickOn(row);

        // Verify nested archive is empty
        FXArchiveInfo archiveInfo = PearlZipFXUtil.lookupArchiveInfo(nestedArchiveName).get();
        Assertions.assertEquals(1, archiveInfo.getFiles().size(), "The nested archive was not in the expected state");

        // Add file and folder to archive
        PearlZipFXUtil.simAddFile(this, file);
        sleep(50, MILLISECONDS);

        // Exit nested archive and save archive into parent archive
        clickOn(Point2D.ZERO.add(110, 10)).clickOn(Point2D.ZERO.add(110, 160));
        sleep(50, MILLISECONDS);
        DialogPane dialogPane = lookup(".dialog-pane").query();
        Assertions.assertTrue(dialogPane.getContentText().startsWith(
                "Please specify if you wish to persist the changes of the nested archive"));
        clickOn(dialogPane.lookupButton(ButtonType.YES));
        sleep(250, MILLISECONDS);

        // Open nested archive and verify existence of files/folders
        doubleClickOn(row);
        Assertions.assertEquals(2, archiveInfo.getFiles().size(),
                                "The nested archive has not stored the expected files");
        Assertions.assertTrue(archiveInfo.getFiles().stream().anyMatch(f->f.getLevel() == 0 && f.getFileName().equals(file.getFileName().toString()) && !f.isFolder()), "Expected top-level file was not found");
        Assertions.assertTrue(archiveInfo.getFiles().stream().anyMatch(f->f.getLevel() == 0 && f.getFileName().equals("1") && !f.isFolder()), "Expected pre-existing top-level file was not found");
        sleep(50, MILLISECONDS);

        Files.deleteIfExists(file);
    }
}
