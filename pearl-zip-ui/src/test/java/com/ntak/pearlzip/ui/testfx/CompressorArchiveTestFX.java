/*
 * Copyright © 2021 92AK
 */
package com.ntak.pearlzip.ui.testfx;

import com.ntak.pearlzip.ui.UITestSuite;
import com.ntak.pearlzip.ui.model.FXArchiveInfo;
import com.ntak.pearlzip.ui.util.AbstractPearlZipTestFX;
import com.ntak.pearlzip.ui.util.PearlZipFXUtil;
import com.ntak.testfx.FormUtil;
import javafx.geometry.Point2D;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DialogPane;
import javafx.scene.control.TableRow;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.ntak.pearlzip.ui.util.PearlZipFXUtil.checkArchiveFileHierarchy;
import static com.ntak.pearlzip.ui.util.PearlZipFXUtil.simOpenArchive;
import static com.ntak.testfx.NativeFileChooserUtil.chooseFile;
import static com.ntak.testfx.TestFXConstants.PLATFORM;
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
         *  + Create single file xz compressor archive
         *  + Create single file GZip compressor archive
         *  + Create single file BZip compressor archive
         *  + Create single file BZip compressor archive from main window
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
        clickOn(Point2D.ZERO.add(110, 10)).clickOn(Point2D.ZERO.add(110, 160));
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
        clickOn(Point2D.ZERO.add(110, 10)).clickOn(Point2D.ZERO.add(110, 160));
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
        clickOn(Point2D.ZERO.add(110, 10)).clickOn(Point2D.ZERO.add(110, 160));
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
        clickOn(Point2D.ZERO.add(110, 10)).clickOn(Point2D.ZERO.add(110, 160));
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
        clickOn(Point2D.ZERO.add(110, 10)).clickOn(Point2D.ZERO.add(110, 160));
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
        checkArchiveFileHierarchy(this, expectations, nestedArchiveName);
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
        clickOn(Point2D.ZERO.add(110, 10)).clickOn(Point2D.ZERO.add(110, 160));
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
        checkArchiveFileHierarchy(this, expectations, nestedArchiveName);
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
        clickOn(Point2D.ZERO.add(110, 10)).clickOn(Point2D.ZERO.add(110, 160));
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
        checkArchiveFileHierarchy(this, expectations, nestedArchiveName);
        sleep(50, MILLISECONDS);
    }

    @Test
    @DisplayName("Test: Create single file xz compressor archive")
    public void testFX_CreateSingleFileXZCompressorArchive_Success() throws IOException {
        // New single file compressor archive
        clickOn(Point2D.ZERO.add(110, 10)).clickOn(Point2D.ZERO.add(110, 60));

        // Set archive format
        String format = "xz";
        ComboBox<String> cmbArchiveFormat = FormUtil.lookupNode(s -> s.isShowing() && s.getTitle().equals("Create new archive..."), "#comboArchiveFormat");
        FormUtil.selectComboBoxEntry(this, cmbArchiveFormat, format);

        // Create a file to archive...
        Path path = Paths.get(tempDirRoot.toAbsolutePath().toString(), "arbitrary-file.txt");
        Files.deleteIfExists(path);
        Files.createFile(path);

        // Select file to archive
        clickOn("#btnSelectFile");
        simOpenArchive(this, path, false, false);
        clickOn("#btnCreate").sleep(50, MILLISECONDS);
        chooseFile(PLATFORM, this, path.getParent().toAbsolutePath());

        // Check archive exists
        Path pathArchive = Paths.get(tempDirRoot.toAbsolutePath().toString(), String.format("arbitrary-file.txt.%s",
                                                                                            format));
        // Check file is in archive...
        checkArchiveFileHierarchy(this,
                                  Collections.singletonMap(0,Collections.singletonMap("", List.of("arbitrary-file" +
                                                                                                          ".txt").toArray(new String[0]))),
                                  pathArchive.toAbsolutePath()
                                             .toString()
        );

        Files.deleteIfExists(pathArchive);
    }

    @Test
    @DisplayName("Test: Create single file GZip compressor archive")
    public void testFX_CreateSingleFileGZipCompressorArchive_Success() throws IOException {
        // New single file compressor archive
        clickOn(Point2D.ZERO.add(110, 10)).clickOn(Point2D.ZERO.add(110, 60));

        // Set archive format
        String format = "gz";
        ComboBox<String> cmbArchiveFormat = FormUtil.lookupNode(s -> s.isShowing() && s.getTitle().equals("Create new archive..."), "#comboArchiveFormat");
        FormUtil.selectComboBoxEntry(this, cmbArchiveFormat, format);

        // Create a file to archive...
        Path path = Paths.get(tempDirRoot.toAbsolutePath().toString(), "arbitrary-file.txt");
        Files.deleteIfExists(path);
        Files.createFile(path);

        // Select file to archive
        clickOn("#btnSelectFile");
        simOpenArchive(this, path, false, false);
        clickOn("#btnCreate").sleep(50, MILLISECONDS);
        chooseFile(PLATFORM, this, path.getParent().toAbsolutePath());

        // Check archive exists
        Path pathArchive = Paths.get(tempDirRoot.toAbsolutePath().toString(), String.format("arbitrary-file.txt.%s",
                                                                                            format));
        // Check file is in archive...
        checkArchiveFileHierarchy(this,
                                  Collections.singletonMap(0,Collections.singletonMap("", List.of("arbitrary-file" +
                                                                                                          ".txt").toArray(new String[0]))),
                                  pathArchive.toAbsolutePath()
                                             .toString()
        );

        Files.deleteIfExists(pathArchive);
    }

    @Test
    @DisplayName("Test: Create single file BZip compressor archive")
    public void testFX_CreateSingleFileBZipCompressorArchive_Success() throws IOException {
        // New single file compressor archive
        clickOn(Point2D.ZERO.add(110, 10)).clickOn(Point2D.ZERO.add(110, 60));

        // Set archive format
        String format = "bz2";
        ComboBox<String> cmbArchiveFormat = FormUtil.lookupNode(s -> s.isShowing() && s.getTitle().equals("Create new archive..."), "#comboArchiveFormat");
        FormUtil.selectComboBoxEntry(this, cmbArchiveFormat, format);

        // Create a file to archive...
        Path path = Paths.get(tempDirRoot.toAbsolutePath().toString(), "arbitrary-file.txt");
        Files.deleteIfExists(path);
        Files.createFile(path);

        // Select file to archive
        clickOn("#btnSelectFile");
        simOpenArchive(this, path, false, false);
        clickOn("#btnCreate").sleep(50, MILLISECONDS);
        chooseFile(PLATFORM, this, path.getParent().toAbsolutePath());

        // Check archive exists
        Path pathArchive = Paths.get(tempDirRoot.toAbsolutePath().toString(), String.format("arbitrary-file.txt.%s",
                                                                                            format));
        // Check file is in archive...
        checkArchiveFileHierarchy(this,
                                  Collections.singletonMap(0,Collections.singletonMap("", List.of("arbitrary-file" +
                                                                                                          ".txt").toArray(new String[0]))),
                                  pathArchive.toAbsolutePath()
                                             .toString()
        );

        Files.deleteIfExists(pathArchive);
    }

    @Test
    @DisplayName("Test: Create single file BZip compressor archive via main window")
    public void testFX_CreateSingleFileBZipCompressorArchiveMainWindow_Success() throws IOException {
        // New single file compressor archive
        clickOn("#btnNew").clickOn("#mnuNewSingleFileCompressor");

        // Set archive format
        String format = "bz2";
        ComboBox<String> cmbArchiveFormat = FormUtil.lookupNode(s -> s.isShowing() && s.getTitle().equals("Create new archive..."), "#comboArchiveFormat");
        FormUtil.selectComboBoxEntry(this, cmbArchiveFormat, format);

        // Create a file to archive...
        Path path = Paths.get(tempDirRoot.toAbsolutePath().toString(), "arbitrary-file.txt");
        Files.deleteIfExists(path);
        Files.createFile(path);

        // Select file to archive
        clickOn("#btnSelectFile");
        simOpenArchive(this, path, false, false);
        clickOn("#btnCreate").sleep(50, MILLISECONDS);
        chooseFile(PLATFORM, this, path.getParent().toAbsolutePath());

        // Check archive exists
        Path pathArchive = Paths.get(tempDirRoot.toAbsolutePath().toString(), String.format("arbitrary-file.txt.%s",
                                                                                            format));
        // Check file is in archive...
        checkArchiveFileHierarchy(this,
                                  Collections.singletonMap(0,Collections.singletonMap("", List.of("arbitrary-file" +
                                                                                                          ".txt").toArray(new String[0]))),
                                  pathArchive.toAbsolutePath()
                                             .toString()
        );

        Files.deleteIfExists(pathArchive);
    }
}
