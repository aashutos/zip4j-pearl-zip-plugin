/*
 * Copyright © 2021 92AK
 */
package com.ntak.pearlzip.ui.testfx;

import com.ntak.pearlzip.archive.pub.FileInfo;
import com.ntak.pearlzip.ui.constants.ZipConstants;
import com.ntak.pearlzip.ui.util.AbstractPearlZipTestFX;
import com.ntak.pearlzip.ui.util.PearlZipFXUtil;
import com.ntak.testfx.FormUtil;
import javafx.scene.control.TableView;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.ntak.pearlzip.ui.UITestSuite.clearDirectory;
import static com.ntak.pearlzip.ui.util.PearlZipFXUtil.lookupArchiveInfo;
import static com.ntak.pearlzip.ui.util.PearlZipFXUtil.simOpenArchive;

public class ExtractFromArchiveTestFX extends AbstractPearlZipTestFX {

    /*
     *  Test cases:
     *  + Extract single file
     *  + Extract all files
     */

    @AfterEach
    public void tearDown() throws IOException {
        clearDirectory(Paths.get(ZipConstants.LOCAL_TEMP.toAbsolutePath()
                                                        .toString(), "output"));
        Files.deleteIfExists(Paths.get(ZipConstants.LOCAL_TEMP.toAbsolutePath().toString(), "1151.txt"));
        Files.deleteIfExists(Paths.get(ZipConstants.LOCAL_TEMP.toAbsolutePath().toString(), "first-file"));
    }

    ////////////////////////////////////////////////////////////////////////////////
    ////////// EXTRACT FILE ////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////

    @Test
    @DisplayName("Test: Extract single file from zip archive")
    public void testFX_extractSingleFileZipArchive_Success() throws IOException {
        // Open archive
        final Path archivePath = Paths.get("src", "test", "resources", "test.zip")
                                      .toAbsolutePath();
        simOpenArchive(this, archivePath, true, false);
        sleep(50, TimeUnit.MILLISECONDS);
        Assertions.assertTrue(lookupArchiveInfo(archivePath.getFileName().toString()).isPresent(), "Expected archive " +
                "was not present");

        // Select file to extract...
        TableView<FileInfo> fileContentsView = lookup("#fileContentsView").queryAs(TableView.class);
        FormUtil.selectTableViewEntry(this, fileContentsView, FileInfo::getFileName,
                                                               "first-file").get();

        // Extract to destination
        Path targetFilePath = Paths.get(ZipConstants.LOCAL_TEMP.toAbsolutePath().toString(), "first-file");
        Files.deleteIfExists(targetFilePath);
        PearlZipFXUtil.simExtractFile(this, targetFilePath);

        // Check extraction was successful...
        Assertions.assertTrue(Files.exists(targetFilePath), "File was not extracted");
    }

    @Test
    @DisplayName("Test: Extract single file from tar archive")
    public void testFX_extractSingleFileTarArchive_Success() throws IOException {
        // Open archive
        final Path archivePath = Paths.get("src", "test", "resources", "test.tar")
                                      .toAbsolutePath();
        simOpenArchive(this, archivePath, true, false);
        sleep(50, TimeUnit.MILLISECONDS);
        Assertions.assertTrue(lookupArchiveInfo(archivePath.getFileName().toString()).isPresent(), "Expected archive " +
                "was not present");

        // Select file to extract...
        TableView<FileInfo> fileContentsView = lookup("#fileContentsView").queryAs(TableView.class);
        FormUtil.selectTableViewEntry(this, fileContentsView, FileInfo::getFileName,
                                                               "1151.txt").get();

        // Extract to destination
        Path targetFilePath = Paths.get(ZipConstants.LOCAL_TEMP.toAbsolutePath().toString(), "1151.txt");
        Files.deleteIfExists(targetFilePath);
        PearlZipFXUtil.simExtractFile(this, targetFilePath);

        // Check extraction was successful...
        Assertions.assertTrue(Files.exists(targetFilePath), "File was not extracted");
    }

    @Test
    @DisplayName("Test: Extract single file from jar archive")
    public void testFX_extractSingleFileJarArchive_Success() throws IOException {
        // Open archive
        final Path archivePath = Paths.get("src", "test", "resources", "test.jar")
                                      .toAbsolutePath();
        simOpenArchive(this, archivePath, true, false);
        sleep(50, TimeUnit.MILLISECONDS);
        Assertions.assertTrue(lookupArchiveInfo(archivePath.getFileName().toString()).isPresent(), "Expected archive " +
                "was not present");

        // Select file to extract...
        TableView<FileInfo> fileContentsView = lookup("#fileContentsView").queryAs(TableView.class);
        FormUtil.selectTableViewEntry(this, fileContentsView, FileInfo::getFileName,
                                                               "first-file").get();

        // Extract to destination
        Path targetFilePath = Paths.get(ZipConstants.LOCAL_TEMP.toAbsolutePath().toString(), "1151.txt");
        Files.deleteIfExists(targetFilePath);
        PearlZipFXUtil.simExtractFile(this, targetFilePath);

        // Check extraction was successful...
        Assertions.assertTrue(Files.exists(targetFilePath), "File was not extracted");
    }

    @Test
    @DisplayName("Test: Extract single file from 7zip archive")
    public void testFX_extractSingleFile7zArchive_Success() throws IOException {
        // Open archive
        final Path archivePath = Paths.get("src", "test", "resources", "test.7z")
                                      .toAbsolutePath();
        simOpenArchive(this, archivePath, true, false);
        sleep(50, TimeUnit.MILLISECONDS);
        Assertions.assertTrue(lookupArchiveInfo(archivePath.getFileName().toString()).isPresent(), "Expected archive " +
                "was not present");

        // Select file to extract...
        TableView<FileInfo> fileContentsView = lookup("#fileContentsView").queryAs(TableView.class);
        FormUtil.selectTableViewEntry(this, fileContentsView, FileInfo::getFileName,
                                                               "1151.txt").get();

        // Extract to destination
        Path targetFilePath = Paths.get(ZipConstants.LOCAL_TEMP.toAbsolutePath().toString(), "1151.txt");
        Files.deleteIfExists(targetFilePath);
        PearlZipFXUtil.simExtractFile(this, targetFilePath);

        // Check extraction was successful...
        Assertions.assertTrue(Files.exists(targetFilePath), "File was not extracted");
    }

    @Test
    @DisplayName("Test: Extract single file from cab archive")
    public void testFX_extractSingleFileCabArchive_Success() throws IOException {
        // Open archive
        final Path archivePath = Paths.get("src", "test", "resources", "test.cab")
                                      .toAbsolutePath();
        simOpenArchive(this, archivePath, true, false);
        sleep(50, TimeUnit.MILLISECONDS);
        Assertions.assertTrue(lookupArchiveInfo(archivePath.getFileName().toString()).isPresent(), "Expected archive " +
                "was not present");

        // Select file to extract...
        PearlZipFXUtil.simTraversalArchive(this, archivePath.toString(), "#fileContentsView", (r)->{}, "lala", "1151.txt");

        // Extract to destination
        Path targetFilePath = Paths.get(ZipConstants.LOCAL_TEMP.toAbsolutePath().toString(), "1151.txt");
        Files.deleteIfExists(targetFilePath);
        PearlZipFXUtil.simExtractFile(this, targetFilePath);

        // Check extraction was successful...
        Assertions.assertTrue(Files.exists(targetFilePath), "File was not extracted");
    }

    @Test
    @DisplayName("Test: Extract single file from iso archive")
    public void testFX_extractSingleFileIsoArchive_Success() throws IOException {
        // Open archive
        final Path archivePath = Paths.get("src", "test", "resources", "test.iso")
                                      .toAbsolutePath();
        simOpenArchive(this, archivePath, true, false);
        sleep(50, TimeUnit.MILLISECONDS);
        Assertions.assertTrue(lookupArchiveInfo(archivePath.getFileName().toString()).isPresent(), "Expected archive " +
                "was not present");

        // Select file to extract...
        TableView<FileInfo> fileContentsView = lookup("#fileContentsView").queryAs(TableView.class);
        FormUtil.selectTableViewEntry(this, fileContentsView, FileInfo::getFileName,
                                                               "1151.txt").get();

        // Extract to destination
        Path targetFilePath = Paths.get(ZipConstants.LOCAL_TEMP.toAbsolutePath().toString(), "1151.txt");
        Files.deleteIfExists(targetFilePath);
        PearlZipFXUtil.simExtractFile(this, targetFilePath);

        // Check extraction was successful...
        Assertions.assertTrue(Files.exists(targetFilePath), "File was not extracted");
    }

    @Test
    @DisplayName("Test: Extract single file from rar archive")
    public void testFX_extractSingleFileRarArchive_Success() throws IOException {
        // Open archive
        final Path archivePath = Paths.get("src", "test", "resources", "test.rar")
                                      .toAbsolutePath();
        simOpenArchive(this, archivePath, true, false);
        sleep(50, TimeUnit.MILLISECONDS);
        Assertions.assertTrue(lookupArchiveInfo(archivePath.getFileName().toString()).isPresent(), "Expected archive " +
                "was not present");

        // Select file to extract...
        TableView<FileInfo> fileContentsView = lookup("#fileContentsView").queryAs(TableView.class);
        FormUtil.selectTableViewEntry(this, fileContentsView, FileInfo::getFileName,
                                                               "1151.txt").get();

        // Extract to destination
        Path targetFilePath = Paths.get(ZipConstants.LOCAL_TEMP.toAbsolutePath().toString(), "1151.txt");
        Files.deleteIfExists(targetFilePath);
        PearlZipFXUtil.simExtractFile(this, targetFilePath);

        // Check extraction was successful...
        Assertions.assertTrue(Files.exists(targetFilePath), "File was not extracted");
    }

    ////////////////////////////////////////////////////////////////////////////////
    ////////// EXTRACT ALL //////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////

    @Test
    @DisplayName("Test: Extract whole zip archive")
    public void testFX_extractAllFilesZipArchive_MatchExpectations() throws IOException {
        // Open archive
        final Path archivePath = Paths.get("src", "test", "resources", "test.zip")
                                      .toAbsolutePath();
        simOpenArchive(this, archivePath, true, false);
        sleep(50, TimeUnit.MILLISECONDS);
        Assertions.assertTrue(lookupArchiveInfo(archivePath.getFileName().toString()).isPresent(), "Expected archive was not present");

        // Store contents
        Path targetFilePath = Paths.get(ZipConstants.LOCAL_TEMP.toAbsolutePath().toString(), "output");
        clearDirectory(targetFilePath);
        Files.createDirectories(targetFilePath);
        TableView<FileInfo> fileContentsView = lookup("#fileContentsView").queryAs(TableView.class);
        List<Path> paths = fileContentsView.getItems()
                                             .stream()
                                             .map(f->Paths.get(targetFilePath.toString(), f.getFileName()))
                                             .collect(Collectors.toList());

        // Extract archive...
        PearlZipFXUtil.simExtractAll(this, targetFilePath);
        for (Path path : paths) {
            Assertions.assertTrue(Files.exists(path), String.format("Path %s does not exist", path));
        }
    }

    @Test
    @DisplayName("Test: Extract whole tar archive")
    public void testFX_extractAllFilesTarArchive_MatchExpectations() throws IOException {
        // Open archive
        final Path archivePath = Paths.get("src", "test", "resources", "test.tar")
                                      .toAbsolutePath();
        simOpenArchive(this, archivePath, true, false);
        sleep(50, TimeUnit.MILLISECONDS);
        Assertions.assertTrue(lookupArchiveInfo(archivePath.getFileName().toString()).isPresent(), "Expected archive was not present");

        // Store contents
        Path targetFilePath = Paths.get(ZipConstants.LOCAL_TEMP.toAbsolutePath().toString(), "output");
        clearDirectory(targetFilePath);
        Files.createDirectories(targetFilePath);
        TableView<FileInfo> fileContentsView = lookup("#fileContentsView").queryAs(TableView.class);
        List<Path> paths = fileContentsView.getItems()
                                           .stream()
                                           .map(f->Paths.get(targetFilePath.toString(), f.getFileName()))
                                           .collect(Collectors.toList());

        // Extract archive...
        PearlZipFXUtil.simExtractAll(this, targetFilePath);
        for (Path path : paths) {
            Assertions.assertTrue(Files.exists(path), String.format("Path %s does not exist", path));
        }
    }

    @Test
    @DisplayName("Test: Extract whole jar archive")
    public void testFX_extractAllFilesJarArchive_MatchExpectations() throws IOException {
        // Open archive
        final Path archivePath = Paths.get("src", "test", "resources", "test.jar")
                                      .toAbsolutePath();
        simOpenArchive(this, archivePath, true, false);
        sleep(50, TimeUnit.MILLISECONDS);
        Assertions.assertTrue(lookupArchiveInfo(archivePath.getFileName().toString()).isPresent(), "Expected archive was not present");

        // Store contents
        Path targetFilePath = Paths.get(ZipConstants.LOCAL_TEMP.toAbsolutePath().toString(), "output");
        clearDirectory(targetFilePath);
        Files.createDirectories(targetFilePath);
        TableView<FileInfo> fileContentsView = lookup("#fileContentsView").queryAs(TableView.class);
        List<Path> paths = fileContentsView.getItems()
                                           .stream()
                                           .map(f->Paths.get(targetFilePath.toString(), f.getFileName()))
                                           .collect(Collectors.toList());

        // Extract archive...
        PearlZipFXUtil.simExtractAll(this, targetFilePath);
        for (Path path : paths) {
            Assertions.assertTrue(Files.exists(path), String.format("Path %s does not exist", path));
        }
    }

    @Test
    @DisplayName("Test: Extract whole 7zip archive")
    public void testFX_extractAllFiles7zArchive_MatchExpectations() throws IOException {
        // Open archive
        final Path archivePath = Paths.get("src", "test", "resources", "test.7z")
                                      .toAbsolutePath();
        simOpenArchive(this, archivePath, true, false);
        sleep(50, TimeUnit.MILLISECONDS);
        Assertions.assertTrue(lookupArchiveInfo(archivePath.getFileName().toString()).isPresent(), "Expected archive was not present");

        // Store contents
        Path targetFilePath = Paths.get(ZipConstants.LOCAL_TEMP.toAbsolutePath().toString(), "output");
        clearDirectory(targetFilePath);
        Files.createDirectories(targetFilePath);
        TableView<FileInfo> fileContentsView = lookup("#fileContentsView").queryAs(TableView.class);
        List<Path> paths = fileContentsView.getItems()
                                           .stream()
                                           .map(f->Paths.get(targetFilePath.toString(), f.getFileName()))
                                           .collect(Collectors.toList());

        // Extract archive...
        PearlZipFXUtil.simExtractAll(this, targetFilePath);
        for (Path path : paths) {
            Assertions.assertTrue(Files.exists(path), String.format("Path %s does not exist", path));
        }
    }

    @Test
    @DisplayName("Test: Extract whole cab archive")
    public void testFX_extractAllFilesCabArchive_MatchExpectations() throws IOException {
        // Open archive
        final Path archivePath = Paths.get("src", "test", "resources", "test.cab")
                                      .toAbsolutePath();
        simOpenArchive(this, archivePath, true, false);
        sleep(50, TimeUnit.MILLISECONDS);
        Assertions.assertTrue(lookupArchiveInfo(archivePath.getFileName().toString()).isPresent(), "Expected archive was not present");

        // Store contents
        Path targetFilePath = Paths.get(ZipConstants.LOCAL_TEMP.toAbsolutePath().toString(), "output");
        clearDirectory(targetFilePath);
        Files.createDirectories(targetFilePath);
        TableView<FileInfo> fileContentsView = lookup("#fileContentsView").queryAs(TableView.class);
        List<Path> paths = fileContentsView.getItems()
                                           .stream()
                                           .map(f->Paths.get(targetFilePath.toString(), f.getFileName()))
                                           .collect(Collectors.toList());

        // Extract archive...
        PearlZipFXUtil.simExtractAll(this, targetFilePath);
        for (Path path : paths) {
            Assertions.assertTrue(Files.exists(path), String.format("Path %s does not exist", path));
        }
    }

    @Test
    @DisplayName("Test: Extract whole iso archive")
    public void testFX_extractAllFilesIsoArchive_MatchExpectations() throws IOException {
        // Open archive
        final Path archivePath = Paths.get("src", "test", "resources", "test.iso")
                                      .toAbsolutePath();
        simOpenArchive(this, archivePath, true, false);
        sleep(50, TimeUnit.MILLISECONDS);
        Assertions.assertTrue(lookupArchiveInfo(archivePath.getFileName().toString()).isPresent(), "Expected archive was not present");

        // Store contents
        Path targetFilePath = Paths.get(ZipConstants.LOCAL_TEMP.toAbsolutePath().toString(), "output");
        clearDirectory(targetFilePath);
        Files.createDirectories(targetFilePath);
        TableView<FileInfo> fileContentsView = lookup("#fileContentsView").queryAs(TableView.class);
        List<Path> paths = fileContentsView.getItems()
                                           .stream()
                                           .map(f->Paths.get(targetFilePath.toString(), f.getFileName()))
                                           .collect(Collectors.toList());

        // Extract archive...
        PearlZipFXUtil.simExtractAll(this, targetFilePath);
        for (Path path : paths) {
            Assertions.assertTrue(Files.exists(path), String.format("Path %s does not exist", path));
        }
    }

    @Test
    @DisplayName("Test: Extract whole rar archive")
    public void testFX_extractAllFilesRarArchive_MatchExpectations() throws IOException {
        // Open archive
        final Path archivePath = Paths.get("src", "test", "resources", "test.rar")
                                      .toAbsolutePath();
        simOpenArchive(this, archivePath, true, false);
        sleep(50, TimeUnit.MILLISECONDS);
        Assertions.assertTrue(lookupArchiveInfo(archivePath.getFileName().toString()).isPresent(), "Expected archive was not present");

        // Store contents
        Path targetFilePath = Paths.get(ZipConstants.LOCAL_TEMP.toAbsolutePath().toString(), "output");
        clearDirectory(targetFilePath);
        Files.createDirectories(targetFilePath);
        TableView<FileInfo> fileContentsView = lookup("#fileContentsView").queryAs(TableView.class);
        List<Path> paths = fileContentsView.getItems()
                                           .stream()
                                           .map(f->Paths.get(targetFilePath.toString(), f.getFileName()))
                                           .collect(Collectors.toList());

        // Extract archive...
        PearlZipFXUtil.simExtractAll(this, targetFilePath);
        for (Path path : paths) {
            Assertions.assertTrue(Files.exists(path), String.format("Path %s does not exist", path));
        }
    }
}
