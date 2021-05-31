/*
 * Copyright © 2021 92AK
 */
package com.ntak.pearlzip.ui.testfx;

import com.ntak.pearlzip.archive.pub.FileInfo;
import com.ntak.pearlzip.ui.UITestSuite;
import com.ntak.pearlzip.ui.model.FXArchiveInfo;
import com.ntak.pearlzip.ui.util.AbstractPearlZipTestFX;
import com.ntak.testfx.FormUtil;
import javafx.scene.control.DialogPane;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseButton;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;

import static com.ntak.pearlzip.ui.UITestFXSuite.genSourceDataSet;
import static com.ntak.pearlzip.ui.constants.ResourceConstants.SSV;
import static com.ntak.pearlzip.ui.util.PearlZipFXUtil.*;
import static com.ntak.testfx.FormUtil.lookupNode;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

@Tag("fx-test")
public class MoveInArchiveTestFX extends AbstractPearlZipTestFX {

    private static Path dir;
    private static Path emptyDir;

   /*
    *  Test cases:
    *  + Move up button and menu
    *  + Move down button and menu
    *  + Move disabled for compressor archives
    *  + Move cancel
    *  + Move no file selected
    *  + Move folder fail
    *  + Table context menu drop
    *  + Move into empty folder
    */

    @BeforeEach
    public void setUp() throws IOException {
        dir = genSourceDataSet();
        emptyDir = Paths.get(dir.toAbsolutePath().getParent().toString(), "empty-dir");
        Files.createDirectories(emptyDir);
    }

    @AfterEach
    public void tearDown() throws Exception {
        super.tearDown();
        for (Path dir :
             Files.list(dir.getParent().getParent()).filter(p->p.getFileName().toString().startsWith("pz")).collect(
             Collectors.toList())) {
            UITestSuite.clearDirectory(dir);
        }
    }

    @Test
    @DisplayName("Test: Move file down by context menu and button in application within a zip archive successfully")
    public void testFX_moveFileDownByMenuAndButtonZip_MatchExpectations() throws IOException {
        final String archiveFormat = "zip";
        final String archiveName = String.format("test%s.%s", archiveFormat, archiveFormat);

        final Path archive = Paths.get(System.getProperty("user.home"), ".pz", "temp", archiveName);

        // Create archive of the appropriate format and add folder to archive...
        simNewArchive(this, archive);
        Assertions.assertTrue(lookupArchiveInfo(archiveName).isPresent(), "Archive is not open in PearlZip");
        simAddFolder(this, dir);

        Path file = Paths.get("root", "level1c", "MOVE_DOWN.txt");

        final String tableName = "#fileContentsView";
        simMoveFile(this, false, archiveName, tableName, file, "level1c1");
        simUp(this);
        simUp(this);
        simUp(this);
        file = Paths.get("root", "level1c", "level1c1", "MOVE_DOWN.txt");
        simMoveFile(this, true, archiveName, tableName, file, "level2b");

        FXArchiveInfo info = lookupArchiveInfo(archiveName).get();
        Assertions.assertEquals(1,
                                info.getFiles().stream().filter(f->f.getFileName().equals(Paths.get("root", "level1c"
                                        , "level1c1", "level2b", "MOVE_DOWN.txt").toString())).count(),
                                "File was not moved");
    }

    @Test
    @DisplayName("Test: Move file by table context menu and button in application within a zip archive successfully")
    public void testFX_moveFileByTableContextMenuAndButtonZip_MatchExpectations() throws IOException {
        final String archiveFormat = "zip";
        final String archiveName = String.format("test%s.%s", archiveFormat, archiveFormat);

        final Path archive = Paths.get(System.getProperty("user.home"), ".pz", "temp", archiveName);

        // Create archive of the appropriate format and add folder to archive...
        simNewArchive(this, archive);
        Assertions.assertTrue(lookupArchiveInfo(archiveName).isPresent(), "Archive is not open in PearlZip");
        simAddFolder(this, dir);

        Path file = Paths.get("root", "level1c", "level1c1", "level2b", "MOVE_UP.txt");

        final String tableName = "#fileContentsView";
        TableView<FileInfo> fileContentsView = FormUtil.lookupNode(s->s.getTitle().contains(archiveName),
                                                                   "#fileContentsView");
        simTraversalArchive(this, archiveName, tableName, (r)->{}, SSV.split(file.toString()));
        clickOn("#btnMove");
        clickOn("#mnuMoveSelected");
        simUp(this);

        clickOn(fileContentsView, MouseButton.SECONDARY);
        sleep(50, MILLISECONDS);
        clickOn("#mnuDrop");
        sleep(50, MILLISECONDS);

        FXArchiveInfo info = lookupArchiveInfo(archiveName).get();
        Assertions.assertEquals(1,
                                info.getFiles().stream().filter(f->f.getFileName().equals(Paths.get("root", "level1c", "level1c1", "MOVE_UP.txt").toString())).count(),
                                "File was not moved");
    }

    @Test
    @DisplayName("Test: Move file down by context menu and button in application within a tar archive successfully")
    public void testFX_moveFileDownByMenuAndButtonTar_MatchExpectations() throws IOException {
        final String archiveFormat = "tar";
        final String archiveName = String.format("test%s.%s", archiveFormat, archiveFormat);

        final Path archive = Paths.get(System.getProperty("user.home"), ".pz", "temp", archiveName);

        // Create archive of the appropriate format and add folder to archive...
        simNewArchive(this, archive);
        Assertions.assertTrue(lookupArchiveInfo(archiveName).isPresent(), "Archive is not open in PearlZip");
        simAddFolder(this, dir);

        Path file = Paths.get("root", "level1c", "MOVE_DOWN.txt");

        final String tableName = "#fileContentsView";
        simMoveFile(this, false, archiveName, tableName, file, "level1c1");
        simUp(this);
        simUp(this);
        simUp(this);
        file = Paths.get("root", "level1c", "level1c1", "MOVE_DOWN.txt");
        simMoveFile(this, true, archiveName, tableName, file, "level2b");

        FXArchiveInfo info = lookupArchiveInfo(archiveName).get();
        Assertions.assertEquals(1,
                                info.getFiles().stream().filter(f->f.getFileName().equals(Paths.get("root", "level1c"
                                        , "level1c1", "level2b", "MOVE_DOWN.txt").toString())).count(),
                                "File was not moved");
    }

    @Test
    @DisplayName("Test: Move file down by context menu and button in application within a jar archive successfully")
    public void testFX_moveFileDownByMenuAndButtonJar_MatchExpectations() throws IOException {
        final String archiveFormat = "jar";
        final String archiveName = String.format("test%s.%s", archiveFormat, archiveFormat);

        final Path archive = Paths.get(System.getProperty("user.home"), ".pz", "temp", archiveName);

        // Create archive of the appropriate format and add folder to archive...
        simNewArchive(this, archive);
        Assertions.assertTrue(lookupArchiveInfo(archiveName).isPresent(), "Archive is not open in PearlZip");
        simAddFolder(this, dir);

        Path file = Paths.get("root", "level1c", "MOVE_DOWN.txt");

        final String tableName = "#fileContentsView";
        simMoveFile(this, false, archiveName, tableName, file, "level1c1");
        simUp(this);
        simUp(this);
        simUp(this);
        file = Paths.get("root", "level1c", "level1c1", "MOVE_DOWN.txt");
        simMoveFile(this, true, archiveName, tableName, file, "level2b");

        FXArchiveInfo info = lookupArchiveInfo(archiveName).get();
        Assertions.assertEquals(1,
                                info.getFiles().stream().filter(f->f.getFileName().equals(Paths.get("root", "level1c"
                                        , "level1c1", "level2b", "MOVE_DOWN.txt").toString())).count(),
                                "File was not moved");
    }

    @Test
    @DisplayName("Test: Move file up by context menu and button in application within a zip archive successfully")
    public void testFX_moveFileUpByMenuAndButtonZip_MatchExpectations() throws IOException {
        final String archiveFormat = "zip";
        final String archiveName = String.format("test%s.%s", archiveFormat, archiveFormat);

        final Path archive = Paths.get(System.getProperty("user.home"), ".pz", "temp", archiveName);

        // Create archive of the appropriate format and add folder to archive...
        simNewArchive(this, archive);
        Assertions.assertTrue(lookupArchiveInfo(archiveName).isPresent(), "Archive is not open in PearlZip");
        simAddFolder(this, dir);

        Path file = Paths.get("root", "level1c", "level1c1", "level2b", "MOVE_UP.txt");

        final String tableName = "#fileContentsView";
        simMoveFile(this, true, archiveName, tableName, file, "..", "..");
        simUp(this);
        simUp(this);
        file = Paths.get("root", "level1c", "MOVE_UP.txt");
        simMoveFile(this, false, archiveName, tableName, file, "..");

        FXArchiveInfo info = lookupArchiveInfo(archiveName).get();
        Assertions.assertEquals(1,
                                info.getFiles().stream().filter(f->f.getFileName().equals(Paths.get("root", "MOVE_UP" +
                                        ".txt").toString())).count(),
                                "File was not moved");
    }

    @Test
    @DisplayName("Test: Move file up by context menu and button in application within a tar archive successfully")
    public void testFX_moveFileUpByMenuAndButtonTar_MatchExpectations() throws IOException {
        final String archiveFormat = "tar";
        final String archiveName = String.format("test%s.%s", archiveFormat, archiveFormat);

        final Path archive = Paths.get(System.getProperty("user.home"), ".pz", "temp", archiveName);

        // Create archive of the appropriate format and add folder to archive...
        simNewArchive(this, archive);
        Assertions.assertTrue(lookupArchiveInfo(archiveName).isPresent(), "Archive is not open in PearlZip");
        simAddFolder(this, dir);

        Path file = Paths.get("root", "level1c", "level1c1", "level2b", "MOVE_UP.txt");

        final String tableName = "#fileContentsView";
        simMoveFile(this, true, archiveName, tableName, file, "..", "..");
        simUp(this);
        simUp(this);
        file = Paths.get("root", "level1c", "MOVE_UP.txt");
        simMoveFile(this, false, archiveName, tableName, file, "..");

        FXArchiveInfo info = lookupArchiveInfo(archiveName).get();
        Assertions.assertEquals(1,
                                info.getFiles().stream().filter(f->f.getFileName().equals(Paths.get("root", "MOVE_UP.txt").toString())).count(),
                                "File was not moved");
    }

    @Test
    @DisplayName("Test: Move file up by context menu and button in application within a jar archive successfully")
    public void testFX_moveFileUpByMenuAndButtonJar_MatchExpectations() throws IOException {
        final String archiveFormat = "jar";
        final String archiveName = String.format("test%s.%s", archiveFormat, archiveFormat);

        final Path archive = Paths.get(System.getProperty("user.home"), ".pz", "temp", archiveName);

        // Create archive of the appropriate format and add folder to archive...
        simNewArchive(this, archive);
        Assertions.assertTrue(lookupArchiveInfo(archiveName).isPresent(), "Archive is not open in PearlZip");
        simAddFolder(this, dir);

        Path file = Paths.get("root", "level1c", "level1c1", "level2b", "MOVE_UP.txt");

        final String tableName = "#fileContentsView";
        simMoveFile(this, true, archiveName, tableName, file, "..", "..");
        simUp(this);
        simUp(this);
        file = Paths.get("root", "level1c", "MOVE_UP.txt");
        simMoveFile(this, false, archiveName, tableName, file, "..");

        FXArchiveInfo info = lookupArchiveInfo(archiveName).get();
        Assertions.assertEquals(1,
                                info.getFiles().stream().filter(f->f.getFileName().equals(Paths.get("root", "MOVE_UP.txt").toString())).count(),
                                "File was not moved");
    }

    @Test
    @DisplayName("Test: Move file in application within a Gzip archive is blocked (Single file compressor)")
    public void testFX_moveFileUpByMenuGzip_Blocked() throws IOException {
        final String archiveFormat = "tar.gz";
        final String archiveName = String.format("testgz.%s", archiveFormat);

        final Path archive = Paths.get(System.getProperty("user.home"), ".pz", "temp", archiveName);

        // Create archive of the appropriate format and add folder to archive...
        simNewArchive(this, archive);
        Assertions.assertTrue(lookupArchiveInfo(archiveName).isPresent(), "Archive is not open in PearlZip");

        clickOn("#btnMove", MouseButton.PRIMARY);
        sleep(50, MILLISECONDS);

        Assertions.assertTrue(lookupNode(s->s.getTitle().contains(archiveName), "#btnMove")
                                                .isDisable(),
                              "Move is not disabled for compressor archives");
    }

    @Test
    @DisplayName("Test: Move file in application within a Bzip archive is blocked (Single file compressor)")
    public void testFX_moveFileUpByMenuBzip_Blocked() throws IOException {
        final String archiveFormat = "tar.bz2";
        final String archiveName = String.format("testbzip.%s", archiveFormat);

        final Path archive = Paths.get(System.getProperty("user.home"), ".pz", "temp", archiveName);

        // Create archive of the appropriate format and add folder to archive...
        simNewArchive(this, archive);
        Assertions.assertTrue(lookupArchiveInfo(archiveName).isPresent(), "Archive is not open in PearlZip");

        clickOn("#btnMove", MouseButton.PRIMARY);
        sleep(50, MILLISECONDS);

        Assertions.assertTrue(lookupNode(s->s.getTitle().contains(archiveName), "#btnMove")
                                                .isDisable(),
                              "Move is not disabled for compressor archives");
    }

    @Test
    @DisplayName("Test: Move file in application within a xz archive is blocked (Single file compressor)")
    public void testFX_moveFileUpByMenuXz_Blocked() throws IOException {
        final String archiveFormat = "tar.gz";
        final String archiveName = String.format("testxz.%s", archiveFormat);

        final Path archive = Paths.get(System.getProperty("user.home"), ".pz", "temp", archiveName);

        // Create archive of the appropriate format and add folder to archive...
        simNewArchive(this, archive);
        Assertions.assertTrue(lookupArchiveInfo(archiveName).isPresent(), "Archive is not open in PearlZip");

        clickOn("#btnMove", MouseButton.PRIMARY);
        sleep(50, MILLISECONDS);

        Assertions.assertTrue(lookupNode(s->s.getTitle().contains(archiveName), "#btnMove")
                                                .isDisable(),
                              "Move is not disabled for compressor archives");
    }

    @Test
    @DisplayName("Test: Move file and cancel")
    public void testFX_moveFileCancel() throws IOException {
        final String archiveFormat = "jar";
        final String archiveName = String.format("test%s.%s", archiveFormat, archiveFormat);

        final Path archive = Paths.get(System.getProperty("user.home"), ".pz", "temp", archiveName);

        // Create archive of the appropriate format and add folder to archive...
        simNewArchive(this, archive);
        Assertions.assertTrue(lookupArchiveInfo(archiveName).isPresent(), "Archive is not open in PearlZip");
        simAddFolder(this, dir);

        Path file = Paths.get("root", "level1c", "level1c1", "level2b", "MOVE_UP.txt");

        final String tableName = "#fileContentsView";
        simTraversalArchive(this, archiveName, tableName, (r)->{}, SSV.split(file.toString()));
        clickOn("#btnMove");
        sleep(5, MILLISECONDS);
        clickOn("#mnuMoveSelected");
        sleep(5, MILLISECONDS);
        clickOn("#btnMove");
        sleep(5, MILLISECONDS);
        clickOn("#mnuCancelMove");

        FXArchiveInfo info = lookupArchiveInfo(archiveName).get();
        Assertions.assertEquals(1,
                                info.getFiles().stream().filter(f->f.getFileName().equals(file.toString())).count(),
                                "File was not kept in the original location");
    }

    @Test
    @DisplayName("Test: Move with no file selected raises alert")
    public void testFX_moveNoFileSelected_Alert() throws IOException {
        final String archiveFormat = "jar";
        final String archiveName = String.format("test%s.%s", archiveFormat, archiveFormat);

        final Path archive = Paths.get(System.getProperty("user.home"), ".pz", "temp", archiveName);

        // Create archive of the appropriate format and add folder to archive...
        simNewArchive(this, archive);
        Assertions.assertTrue(lookupArchiveInfo(archiveName).isPresent(), "Archive is not open in PearlZip");
        simAddFolder(this, dir);

        Path file = Paths.get("root", "level1c", "level1c1", "level2b");

        final String tableName = "#fileContentsView";
        simTraversalArchive(this, archiveName, tableName, (r)->{}, SSV.split(file.toString())).get();
        simUp(this);
        clickOn("#btnMove");
        sleep(5, MILLISECONDS);
        clickOn("#mnuMoveSelected");

        DialogPane dialogPane = lookup(".dialog-pane").query();
        Assertions.assertTrue(dialogPane.getContentText().startsWith("Move could not be initiated"), "The text in warning dialog was not matched as expected");
    }

    @Test
    @DisplayName("Test: Move on folder raises alert")
    public void testFX_moveFolderSelected_Alert() throws IOException {
        final String archiveFormat = "jar";
        final String archiveName = String.format("test%s.%s", archiveFormat, archiveFormat);

        final Path archive = Paths.get(System.getProperty("user.home"), ".pz", "temp", archiveName);

        // Create archive of the appropriate format and add folder to archive...
        simNewArchive(this, archive);
        Assertions.assertTrue(lookupArchiveInfo(archiveName).isPresent(), "Archive is not open in PearlZip");
        simAddFolder(this, dir);

        Path file = Paths.get("root", "level1c", "level1c1", "level2b");

        final String tableName = "#fileContentsView";
        simTraversalArchive(this, archiveName, tableName, (r)->{}, SSV.split(file.toString())).get();
        clickOn("#btnMove");
        sleep(5, MILLISECONDS);
        clickOn("#mnuMoveSelected");

        DialogPane dialogPane = lookup(".dialog-pane").query();
        Assertions.assertTrue(dialogPane.getContentText().startsWith("Move could not be initiated"), "The text in warning dialog was not matched as expected");
    }

    @Test
    @DisplayName("Test: Move file into empty directory")
    public void testFX_moveFileIntoEmptyDirectory_Success() throws IOException {
        final String archiveFormat = "jar";
        final String archiveName = String.format("test%s.%s", archiveFormat, archiveFormat);

        final Path archive = Paths.get(System.getProperty("user.home"), ".pz", "temp", archiveName);

        // Create archive of the appropriate format and add folder to archive...
        simNewArchive(this, archive);
        Assertions.assertTrue(lookupArchiveInfo(archiveName).isPresent(), "Archive is not open in PearlZip");
        simAddFolder(this, dir);
        simAddFolder(this, emptyDir);

        Path file = Paths.get("root", "level1c", "level1c1", "level2b", "MOVE_UP.txt");

        final String tableName = "#fileContentsView";
        simTraversalArchive(this, archiveName, tableName, (r)->{}, SSV.split(file.toString()));
        clickOn("#btnMove");
        sleep(5, MILLISECONDS);
        clickOn("#mnuMoveSelected");
        sleep(5, MILLISECONDS);
        simUp(this);
        sleep(5, MILLISECONDS);
        simUp(this);
        sleep(5, MILLISECONDS);
        simUp(this);
        sleep(5, MILLISECONDS);
        simUp(this);
        sleep(5, MILLISECONDS);
        simTraversalArchive(this, archiveName, tableName, this::doubleClickOn, "empty-dir");
        TableView<FileInfo> fileContentsView = FormUtil.lookupNode(s->s.getTitle().contains(archiveName),
                                                                   tableName);
        clickOn(fileContentsView, MouseButton.SECONDARY);
        sleep(5, MILLISECONDS);
        clickOn("#mnuDrop");
        sleep(5, MILLISECONDS);

        FXArchiveInfo info = lookupArchiveInfo(archiveName).get();
        Assertions.assertEquals(1,
                                info.getFiles().stream().filter(f->f.getFileName().equals(Paths.get("empty-dir",
                                                                                                    "MOVE_UP.txt").toString())).count(),
                                "File was not moved");
        Assertions.assertEquals(1,
                                info.getFiles().stream().filter(f->f.getFileName().endsWith("MOVE_UP.txt")).count(),
                                "File was not moved, may have been copied unexpectedly");
    }
}
