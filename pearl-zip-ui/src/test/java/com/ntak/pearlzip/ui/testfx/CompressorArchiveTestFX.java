/*
 * Copyright © 2021 92AK
 */
package com.ntak.pearlzip.ui.testfx;

import com.ntak.pearlzip.ui.UITestSuite;
import com.ntak.pearlzip.ui.model.FXArchiveInfo;
import com.ntak.pearlzip.ui.util.AbstractPearlZipTestFX;
import com.ntak.pearlzip.ui.util.PearlZipFXUtil;
import javafx.geometry.Point2D;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.control.TableRow;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class CompressorArchiveTestFX extends AbstractPearlZipTestFX {

    private static Path tempDirRoot;
    private Path file;
    private Path folder;
    private Path nestedFile;
    private Path outputDir;

    /*
         *  Test cases:
         *  + Nest tarball into the compressor archive and verify contents is as expected
         *  + Extract archive contents from nested tarball generates expected files/folders
         */

    @BeforeEach
    public void setUp() throws IOException {
        tempDirRoot = Files.createTempDirectory("pz");
        outputDir = Paths.get(tempDirRoot.toAbsolutePath().toString(), "output");
        file = Paths.get(tempDirRoot.toString(), "temp-file");
        folder = Paths.get(tempDirRoot.toString(), "temp-folder");
        nestedFile = Paths.get(tempDirRoot.toString(), "temp-folder", "sub-temp-file");

        Files.createFile(file);
        Files.createDirectories(folder);
        Files.createFile(nestedFile);
    }

    @AfterEach
    public void tearDown() throws Exception {
        super.tearDown();
        UITestSuite.clearDirectory(tempDirRoot);
    }

    @Test
    @DisplayName("Test: Nest tarball into the Gzip compressor archive and verify contents is as expected")
    public void testFX_CreateGzipArchiveAndUpdateNestedTarball_Success() throws IOException {
        // Create archive
        String archiveFormat = "tar.gz";
        final String archiveName = String.format("nest-test.%s", archiveFormat);
        Path archivePath = Paths.get(tempDirRoot.toAbsolutePath().toString(), archiveName);
        final String nestedArchiveName = archiveName.substring(0,
                                                               archiveName.lastIndexOf("."));
        PearlZipFXUtil.simNewArchive(this, archivePath);

        // Open nested tar archive
        TableRow row = PearlZipFXUtil.simTraversalArchive(this, archiveName, "#fileContentsView", (r)->{},
                                                          nestedArchiveName).get();
        sleep(250, MILLISECONDS);
        doubleClickOn(row);

        // Verify nested tarball is empty
        FXArchiveInfo archiveInfo = PearlZipFXUtil.lookupArchiveInfo(nestedArchiveName).get();
        Assertions.assertEquals(0, archiveInfo.getFiles().size(), "The nested archive was not empty");

        // Add file and folder to archive
        PearlZipFXUtil.simAddFile(this, file);
        sleep(50, MILLISECONDS);
        PearlZipFXUtil.simAddFolder(this, folder);
        sleep(50, MILLISECONDS);

        // Exit tarball instance and save archive into compressor
        clickOn(Point2D.ZERO.add(110, 10)).clickOn(Point2D.ZERO.add(110, 150));
        sleep(50, MILLISECONDS);
        DialogPane dialogPane = lookup(".dialog-pane").query();
        Assertions.assertTrue(dialogPane.getContentText().startsWith(
                "Please specify if you wish to persist the changes of the nested archive"));
        clickOn(dialogPane.lookupButton(ButtonType.YES));
        sleep(250, MILLISECONDS);

        // Open nested tarball archive and verify existence of files/folders
        doubleClickOn(row);
        Assertions.assertEquals(3, archiveInfo.getFiles().size(),
                                "The nested archive has not stored the expected files");
        Assertions.assertTrue(archiveInfo.getFiles().stream().anyMatch(f->f.getLevel() == 0 && f.getFileName().equals(file.getFileName().toString()) && !f.isFolder()), "Expected top-level file was not found");
        Assertions.assertTrue(archiveInfo.getFiles().stream().anyMatch(f->f.getLevel() == 0 && f.getFileName().equals(folder.getFileName().toString()) && f.isFolder()), "Expected top-level folder was not found");
        Assertions.assertTrue(archiveInfo.getFiles().stream().anyMatch(f->f.getLevel() == 1 && f.getFileName().contains(nestedFile.getFileName().toString()) && !f.isFolder()), "Expected nested file was not found");
        sleep(50, MILLISECONDS);
    }

    @Test
    @DisplayName("Test: Nest tarball into the Gzip compressor archive (shortname) and verify contents is as expected")
    public void testFX_OpenGzipArchiveAndUpdateNestedTarballShortName_Success() throws IOException {
        // Create archive
        String archiveFormat = "tgz";
        final String archiveName = String.format("test.%s", archiveFormat);
        Path srcArchivePath = Paths.get("src","test","resources", archiveName).toAbsolutePath();
        Path archivePath = Paths.get(tempDirRoot.toAbsolutePath().toString(), archiveName).toAbsolutePath();
        Files.copy(srcArchivePath, archivePath);
        final String nestedArchiveName = String.format("%s.tar", archiveName.substring(0,
                                                               archiveName.lastIndexOf(".")));
        PearlZipFXUtil.simOpenArchive(this, archivePath, true, false);

        // Open nested tar archive
        TableRow row = PearlZipFXUtil.simTraversalArchive(this, archiveName, "#fileContentsView", (r)->{},
                                                          nestedArchiveName).get();
        sleep(250, MILLISECONDS);
        doubleClickOn(row);

        FXArchiveInfo archiveInfo = PearlZipFXUtil.lookupArchiveInfo(nestedArchiveName).get();

        // Add file and folder to archive
        PearlZipFXUtil.simAddFile(this, file);
        sleep(50, MILLISECONDS);

        // Exit tarball instance and save archive into compressor
        clickOn(Point2D.ZERO.add(110, 10)).clickOn(Point2D.ZERO.add(110, 150));
        sleep(50, MILLISECONDS);
        DialogPane dialogPane = lookup(".dialog-pane").query();
        Assertions.assertTrue(dialogPane.getContentText().startsWith(
                "Please specify if you wish to persist the changes of the nested archive"));
        clickOn(dialogPane.lookupButton(ButtonType.YES));
        sleep(250, MILLISECONDS);

        // Open nested tarball archive and verify existence of files/folders
        doubleClickOn(row);
        Assertions.assertEquals(5, archiveInfo.getFiles().size(),
                                "The nested archive has not stored the expected files");
        Assertions.assertTrue(archiveInfo.getFiles().stream().anyMatch(f->f.getLevel() == 0 && f.getFileName().equals(file.getFileName().toString()) && !f.isFolder()), "Expected top-level file was not found");
        sleep(50, MILLISECONDS);
    }

    @Test
    @DisplayName("Test: Nest tarball into the Bzip compressor archive and verify contents is as expected")
    public void testFX_CreateBzipArchiveAndUpdateNestedTarball_Success() throws IOException {
        // Create archive
        String archiveFormat = "tar.bz2";
        final String archiveName = String.format("nest-test.%s", archiveFormat);
        Path archivePath = Paths.get(tempDirRoot.toAbsolutePath().toString(), archiveName);
        final String nestedArchiveName = archiveName.substring(0,
                                                               archiveName.lastIndexOf("."));
        PearlZipFXUtil.simNewArchive(this, archivePath);

        // Open nested tar archive
        TableRow row = PearlZipFXUtil.simTraversalArchive(this, archiveName, "#fileContentsView", (r)->{},
                                                          nestedArchiveName).get();
        sleep(250, MILLISECONDS);
        doubleClickOn(row);

        // Verify nested tarball is empty
        FXArchiveInfo archiveInfo = PearlZipFXUtil.lookupArchiveInfo(nestedArchiveName).get();
        Assertions.assertEquals(0, archiveInfo.getFiles().size(), "The nested archive was not empty");

        // Add file and folder to archive
        PearlZipFXUtil.simAddFile(this, file);
        sleep(50, MILLISECONDS);
        PearlZipFXUtil.simAddFolder(this, folder);
        sleep(50, MILLISECONDS);

        // Exit tarball instance and save archive into compressor
        clickOn(Point2D.ZERO.add(110, 10)).clickOn(Point2D.ZERO.add(110, 150));
        sleep(50, MILLISECONDS);
        DialogPane dialogPane = lookup(".dialog-pane").query();
        Assertions.assertTrue(dialogPane.getContentText().startsWith(
                "Please specify if you wish to persist the changes of the nested archive"));
        clickOn(dialogPane.lookupButton(ButtonType.YES));
        sleep(250, MILLISECONDS);

        // Open nested tarball archive and verify existence of files/folders
        doubleClickOn(row);
        Assertions.assertEquals(3, archiveInfo.getFiles().size(),
                                "The nested archive has not stored the expected files");
        Assertions.assertTrue(archiveInfo.getFiles().stream().anyMatch(f->f.getLevel() == 0 && f.getFileName().equals(file.getFileName().toString()) && !f.isFolder()), "Expected top-level file was not found");
        Assertions.assertTrue(archiveInfo.getFiles().stream().anyMatch(f->f.getLevel() == 0 && f.getFileName().equals(folder.getFileName().toString()) && f.isFolder()), "Expected top-level folder was not found");
        Assertions.assertTrue(archiveInfo.getFiles().stream().anyMatch(f->f.getLevel() == 1 && f.getFileName().contains(nestedFile.getFileName().toString()) && !f.isFolder()), "Expected nested file was not found");
        sleep(50, MILLISECONDS);
    }

    @Test
    @DisplayName("Test: Nest tarball into the xz compressor archive and verify contents is as expected")
    public void testFX_CreateXZArchiveAndUpdateNestedTarball_Success() throws IOException {
        // Create archive
        String archiveFormat = "tar.xz";
        final String archiveName = String.format("nest-test.%s", archiveFormat);
        Path archivePath = Paths.get(tempDirRoot.toAbsolutePath().toString(), archiveName);
        final String nestedArchiveName = archiveName.substring(0,
                                                               archiveName.lastIndexOf("."));
        PearlZipFXUtil.simNewArchive(this, archivePath);

        // Open nested tar archive
        TableRow row = PearlZipFXUtil.simTraversalArchive(this, archiveName, "#fileContentsView", (r)->{},
                                                          nestedArchiveName).get();
        sleep(250, MILLISECONDS);
        doubleClickOn(row);

        // Verify nested tarball is empty
        FXArchiveInfo archiveInfo = PearlZipFXUtil.lookupArchiveInfo(nestedArchiveName).get();
        Assertions.assertEquals(0, archiveInfo.getFiles().size(), "The nested archive was not empty");

        // Add file and folder to archive
        PearlZipFXUtil.simAddFile(this, file);
        sleep(50, MILLISECONDS);
        PearlZipFXUtil.simAddFolder(this, folder);
        sleep(50, MILLISECONDS);

        // Exit tarball instance and save archive into compressor
        clickOn(Point2D.ZERO.add(110, 10)).clickOn(Point2D.ZERO.add(110, 150));
        sleep(50, MILLISECONDS);
        DialogPane dialogPane = lookup(".dialog-pane").query();
        Assertions.assertTrue(dialogPane.getContentText().startsWith(
                "Please specify if you wish to persist the changes of the nested archive"));
        clickOn(dialogPane.lookupButton(ButtonType.YES));
        sleep(250, MILLISECONDS);

        // Open nested tarball archive and verify existence of files/folders
        doubleClickOn(row);
        Assertions.assertEquals(3, archiveInfo.getFiles().size(),
                                "The nested archive has not stored the expected files");
        Assertions.assertTrue(archiveInfo.getFiles().stream().anyMatch(f->f.getLevel() == 0 && f.getFileName().equals(file.getFileName().toString()) && !f.isFolder()), "Expected top-level file was not found");
        Assertions.assertTrue(archiveInfo.getFiles().stream().anyMatch(f->f.getLevel() == 0 && f.getFileName().equals(folder.getFileName().toString()) && f.isFolder()), "Expected top-level folder was not found");
        Assertions.assertTrue(archiveInfo.getFiles().stream().anyMatch(f->f.getLevel() == 1 && f.getFileName().contains(nestedFile.getFileName().toString()) && !f.isFolder()), "Expected nested file was not found");
        sleep(50, MILLISECONDS);
    }

    @Test
    @DisplayName("Test: Extract GZip archive contents from nested tarball generates expected files/folders")
    public void testFX_CreatePopulatedGzipArchiveExtractAll_MatchExpectations() throws IOException {
        // Create archive
        String archiveFormat = "tar.gz";
        final String archiveName = String.format("nest-test.%s", archiveFormat);
        Path archivePath = Paths.get(tempDirRoot.toAbsolutePath().toString(), archiveName);
        final String nestedArchiveName = archiveName.substring(0,
                                                               archiveName.lastIndexOf("."));
        PearlZipFXUtil.simNewArchive(this, archivePath);

        // Open nested tar archive
        TableRow row = PearlZipFXUtil.simTraversalArchive(this, archiveName, "#fileContentsView", (r)->{},
                                                          nestedArchiveName).get();
        sleep(250, MILLISECONDS);
        doubleClickOn(row);

        // Verify nested tarball is empty
        FXArchiveInfo archiveInfo = PearlZipFXUtil.lookupArchiveInfo(nestedArchiveName).get();
        Assertions.assertEquals(0, archiveInfo.getFiles().size(), "The nested archive was not empty");

        // Add file and folder to archive
        PearlZipFXUtil.simAddFile(this, file);
        sleep(50, MILLISECONDS);
        PearlZipFXUtil.simAddFolder(this, folder);
        sleep(50, MILLISECONDS);

        // Exit tarball instance and save archive into compressor
        clickOn(Point2D.ZERO.add(110, 10)).clickOn(Point2D.ZERO.add(110, 150));
        sleep(50, MILLISECONDS);
        DialogPane dialogPane = lookup(".dialog-pane").query();
        Assertions.assertTrue(dialogPane.getContentText().startsWith(
                "Please specify if you wish to persist the changes of the nested archive"));
        clickOn(dialogPane.lookupButton(ButtonType.YES));
        sleep(250, MILLISECONDS);

        // Open nested tarball archive and verify existence of files/folders
        doubleClickOn(row);
        Assertions.assertEquals(3, archiveInfo.getFiles().size(),
                                "The nested archive has not stored the expected files");
        PearlZipFXUtil.simExtractAll(this, outputDir);
        Map<Integer,Map<String,String[]>> expectations = PearlZipFXUtil.genArchiveContentsExpectationsAuto(outputDir);
        PearlZipFXUtil.checkArchiveFileHierarchy(this, expectations, nestedArchiveName);
        sleep(50, MILLISECONDS);
    }

    @Test
    @DisplayName("Test: Extract Bzip archive contents from nested tarball generates expected files/folders")
    public void testFX_CreatePopulatedBzipArchiveExtractAll_MatchExpectations() throws IOException {
        // Create archive
        String archiveFormat = "tar.bz2";
        final String archiveName = String.format("nest-test.%s", archiveFormat);
        Path archivePath = Paths.get(tempDirRoot.toAbsolutePath().toString(), archiveName);
        final String nestedArchiveName = archiveName.substring(0,
                                                               archiveName.lastIndexOf("."));
        PearlZipFXUtil.simNewArchive(this, archivePath);

        // Open nested tar archive
        TableRow row = PearlZipFXUtil.simTraversalArchive(this, archiveName, "#fileContentsView", (r)->{},
                                                          nestedArchiveName).get();
        sleep(250, MILLISECONDS);
        doubleClickOn(row);

        // Verify nested tarball is empty
        FXArchiveInfo archiveInfo = PearlZipFXUtil.lookupArchiveInfo(nestedArchiveName).get();
        Assertions.assertEquals(0, archiveInfo.getFiles().size(), "The nested archive was not empty");

        // Add file and folder to archive
        PearlZipFXUtil.simAddFile(this, file);
        sleep(50, MILLISECONDS);
        PearlZipFXUtil.simAddFolder(this, folder);
        sleep(50, MILLISECONDS);

        // Exit tarball instance and save archive into compressor
        clickOn(Point2D.ZERO.add(110, 10)).clickOn(Point2D.ZERO.add(110, 150));
        sleep(50, MILLISECONDS);
        DialogPane dialogPane = lookup(".dialog-pane").query();
        Assertions.assertTrue(dialogPane.getContentText().startsWith(
                "Please specify if you wish to persist the changes of the nested archive"));
        clickOn(dialogPane.lookupButton(ButtonType.YES));
        sleep(250, MILLISECONDS);

        // Open nested tarball archive and verify existence of files/folders
        doubleClickOn(row);
        Assertions.assertEquals(3, archiveInfo.getFiles().size(),
                                "The nested archive has not stored the expected files");
        PearlZipFXUtil.simExtractAll(this, outputDir);
        Map<Integer,Map<String,String[]>> expectations = PearlZipFXUtil.genArchiveContentsExpectationsAuto(outputDir);
        PearlZipFXUtil.checkArchiveFileHierarchy(this, expectations, nestedArchiveName);
        sleep(50, MILLISECONDS);
    }

    @Test
    @DisplayName("Test: Extract xz archive contents from nested tarball generates expected files/folders")
    public void testFX_CreatePopulatedXZArchiveExtractAll_MatchExpectations() throws IOException {
        // Create archive
        String archiveFormat = "tar.xz";
        final String archiveName = String.format("nest-test.%s", archiveFormat);
        Path archivePath = Paths.get(tempDirRoot.toAbsolutePath().toString(), archiveName);
        final String nestedArchiveName = archiveName.substring(0,
                                                               archiveName.lastIndexOf("."));
        PearlZipFXUtil.simNewArchive(this, archivePath);

        // Open nested tar archive
        TableRow row = PearlZipFXUtil.simTraversalArchive(this, archiveName, "#fileContentsView", (r)->{},
                                                          nestedArchiveName).get();
        sleep(250, MILLISECONDS);
        doubleClickOn(row);

        // Verify nested tarball is empty
        FXArchiveInfo archiveInfo = PearlZipFXUtil.lookupArchiveInfo(nestedArchiveName).get();
        Assertions.assertEquals(0, archiveInfo.getFiles().size(), "The nested archive was not empty");

        // Add file and folder to archive
        PearlZipFXUtil.simAddFile(this, file);
        sleep(50, MILLISECONDS);
        PearlZipFXUtil.simAddFolder(this, folder);
        sleep(50, MILLISECONDS);

        // Exit tarball instance and save archive into compressor
        clickOn(Point2D.ZERO.add(110, 10)).clickOn(Point2D.ZERO.add(110, 150));
        sleep(50, MILLISECONDS);
        DialogPane dialogPane = lookup(".dialog-pane").query();
        Assertions.assertTrue(dialogPane.getContentText().startsWith(
                "Please specify if you wish to persist the changes of the nested archive"));
        clickOn(dialogPane.lookupButton(ButtonType.YES));
        sleep(250, MILLISECONDS);

        // Open nested tarball archive and verify existence of files/folders
        doubleClickOn(row);
        Assertions.assertEquals(3, archiveInfo.getFiles().size(),
                                "The nested archive has not stored the expected files");
        PearlZipFXUtil.simExtractAll(this, outputDir);
        Map<Integer,Map<String,String[]>> expectations = PearlZipFXUtil.genArchiveContentsExpectationsAuto(outputDir);
        PearlZipFXUtil.checkArchiveFileHierarchy(this, expectations, nestedArchiveName);
        sleep(50, MILLISECONDS);
    }
}
