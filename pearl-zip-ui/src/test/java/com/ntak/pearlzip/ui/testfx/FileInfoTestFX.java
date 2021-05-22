/*
 * Copyright © 2021 92AK
 */
package com.ntak.pearlzip.ui.testfx;

import com.ntak.pearlzip.archive.pub.FileInfo;
import com.ntak.pearlzip.ui.util.AbstractPearlZipTestFX;
import javafx.scene.control.Label;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.util.Pair;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testfx.api.FxRobot;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

import static com.ntak.pearlzip.ui.util.PearlZipFXUtil.*;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class FileInfoTestFX extends AbstractPearlZipTestFX {

    /*
     *  Test cases:
     *  + File Information dialog displayed for a file contains the expected information
     *  + File Information dialog displayed for a folder contains the expected information
     */

    @AfterEach
    public void tearDown() throws IOException {

    }

    @Test
    @DisplayName("Test: File Information dialog displayed for a file contains the expected information")
    public void testFX_FileInfoForFile_MatchExpectations() {
        final String archiveFormat = "zip";
        final String archiveName = String.format("test.%s", archiveFormat);

        final Path archive = Paths.get("src", "test", "resources", archiveName).toAbsolutePath();

        // Open archive...
        simOpenArchive(this, archive, true, false);

        // Traverse to file...
        TableRow<FileInfo> row =
                simTraversalArchive(this, archiveName, "#fileContentsView", (r)->{}, "first-file").get();

        // Open File Information...
        simFileInfo(this);

        // Verify contents
        FileInfo info = row.getItem();
        checkFileInfoScreenContents(this, info);
    }

    @Test
    @DisplayName("Test: File Information dialog displayed for a folder contains the expected information")
    public void testFX_FileInfoForFolder_MatchExpectations() {
        final String archiveFormat = "zip";
        final String archiveName = String.format("test.%s", archiveFormat);

        final Path archive = Paths.get("src", "test", "resources", archiveName).toAbsolutePath();

        // Open archive...
        simOpenArchive(this, archive, true, false);

        // Traverse to file...
        TableRow<FileInfo> row =
                simTraversalArchive(this, archiveName, "#fileContentsView", (r)->{}, "first-folder").get();

        // Open File Information...
        simFileInfo(this);

        // Verify contents
        FileInfo info = row.getItem();
        checkFileInfoScreenContents(this, info);
    }

    private static void checkFileInfoScreenContents(FxRobot robot, FileInfo fileInfo) {
        Label lblIndexValue = robot.lookup("#lblIndexValue").queryAs(Label.class);
        Label lblLevelValue = robot.lookup("#lblLevelValue").queryAs(Label.class);
        Label lblFilenameValue = robot.lookup("#lblFilenameValue").queryAs(Label.class);
        Label lblHashValue = robot.lookup("#lblHashValue").queryAs(Label.class);
        Label lblRawSizeValue = robot.lookup("#lblRawSizeValue").queryAs(Label.class);
        Label lblPackedSizeValue = robot.lookup("#lblPackedSizeValue").queryAs(Label.class);
        Label lblFolderValue = robot.lookup("#lblFolderValue").queryAs(Label.class);
        Label lblEncryptValue = robot.lookup("#lblEncryptValue").queryAs(Label.class);
        Label lblCommentsValue = robot.lookup("#lblCommentsValue").queryAs(Label.class);
        Label lblLastWriteTimeValue = robot.lookup("#lblLastWriteTimeValue").queryAs(Label.class);
        Label lblLastAccessTimeValue = robot.lookup("#lblLastAccessTimeValue").queryAs(Label.class);
        Label lblCreateTimeValue = robot.lookup("#lblCreateTimeValue").queryAs(Label.class);
        Label lblUserValue = robot.lookup("#lblUserValue").queryAs(Label.class);
        Label lblGroupValue = robot.lookup("#lblGroupValue").queryAs(Label.class);

        robot.clickOn("#tpGeneral");
        robot.sleep(5, MILLISECONDS);
        Assertions.assertEquals(String.valueOf(fileInfo.getIndex()), lblIndexValue.getText(), "Index does not match");
        Assertions.assertEquals(String.valueOf(fileInfo.getLevel()), lblLevelValue.getText(), "Level does not match");
        Assertions.assertEquals(String.valueOf(fileInfo.getFileName()), lblFilenameValue.getText(),
                                "Filename does not match");
        Assertions.assertEquals(String.format("0x%s",Long.toHexString(fileInfo.getCrcHash()).toUpperCase()), lblHashValue.getText(),
                                "Hash does not match");
        Assertions.assertEquals(String.valueOf(fileInfo.getRawSize()), lblRawSizeValue.getText(),
                                "Raw size does not match");
        Assertions.assertEquals(String.valueOf(fileInfo.getPackedSize()), lblPackedSizeValue.getText(),
                                "Packed size does not match");
        Assertions.assertEquals(fileInfo.isFolder()?"folder":"file", lblFolderValue.getText(),
                                "Is Folder does not match");
        Assertions.assertEquals(fileInfo.isEncrypted()?"encrypted":"plaintext", lblEncryptValue.getText(),
                                "Is Encrypted does not match");
        Assertions.assertEquals(fileInfo.getComments(), lblCommentsValue.getText(),
                                "Comments does not match");

        robot.clickOn("#tpTimestamps");
        robot.sleep(5, MILLISECONDS);
        Assertions.assertEquals(Objects.isNull(fileInfo.getLastWriteTime())?"-":fileInfo.getLastWriteTime()
                                                                                        .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME), lblLastWriteTimeValue.getText(),
                                "Last Write Timestamp does not match");
        Assertions.assertEquals(Objects.isNull(fileInfo.getLastAccessTime())?"-":fileInfo.getLastAccessTime()
                                                                                        .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME), lblLastAccessTimeValue.getText(),
                                "Last Access Timestamp does not match");
        Assertions.assertEquals(Objects.isNull(fileInfo.getCreationTime())?"-":fileInfo.getCreationTime()
                                                                                         .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME), lblCreateTimeValue.getText(),
                                "Creation Timestamp does not match");

        robot.clickOn("#tpOwnership");
        robot.sleep(5, MILLISECONDS);
        Assertions.assertEquals(fileInfo.getUser(), lblUserValue.getText(), "User does not match");
        Assertions.assertEquals(fileInfo.getGroup(), lblGroupValue.getText(), "Group does not match");

        robot.clickOn("#tpOther");
        robot.sleep(5, MILLISECONDS);
        TableView<Pair<String,String>> props = robot.lookup("#tblOtherInfo").queryAs(TableView.class);
        List<Pair<String,String>> propList = props.getItems();
        propList.stream().forEach(p->Assertions.assertEquals(fileInfo.getAdditionalInfoMap().get(p.getKey()),
                                                             p.getValue(),
                                                             String.format("Property (%s,%s) did not match",
                                                                           p.getKey(), p.getValue())));
    }
}
