/*
 * Copyright © 2021 92AK
 */
package com.ntak.pearlzip.ui.testfx;

import com.ntak.pearlzip.archive.pub.FileInfo;
import com.ntak.pearlzip.ui.UITestSuite;
import com.ntak.pearlzip.ui.util.AbstractPearlZipTestFX;
import com.ntak.pearlzip.ui.util.PearlZipFXUtil;
import com.ntak.testfx.FormUtil;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;

import static com.ntak.pearlzip.ui.UITestFXSuite.genSourceDataSet;
import static com.ntak.pearlzip.ui.constants.ResourceConstants.SSV;
import static com.ntak.pearlzip.ui.util.PearlZipFXUtil.*;

public class DeleteInArchiveTestFX extends AbstractPearlZipTestFX {

    private static Path dir;
    private static Path setRoot;

    static {
        try {
            setRoot = Paths.get(Files.createTempDirectory("pz").toString(), "root");
        } catch(IOException e) {
        }
    }

    /*
     *  Test cases:
     *  + Delete file successfully in non-compressor archive
     *  + Delete failed in compressor archive
     *  + Delete folder successfully in non-compressor archive
     */

    @BeforeEach
    public void setUp() throws IOException {
        dir = genSourceDataSet();
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
    @DisplayName("Test: Delete file successfully within Zip archive")
    public void testFX_DeleteFileZip_Success() throws IOException {
        final String archiveFormat = "zip";
        final String archiveName = String.format("test%s.%s", archiveFormat, archiveFormat);

        final Path archive = Paths.get(System.getProperty("user.home"), ".pz", "temp", archiveName);

        // Create archive of the appropriate format and add folder to archive...
        simNewArchive(this, archive);
        Assertions.assertTrue(lookupArchiveInfo(archiveName).isPresent(), "Archive is not open in PearlZip");
        simAddFolder(this, dir);

        Path file = Paths.get("root", "level1b", "DELETE_ME.txt");
        PearlZipFXUtil.simTraversalArchive(this, archiveName,"#fileContentsView", (r)->{}, SSV.split(file.toString()));
        PearlZipFXUtil.simDelete(this);
        Assertions.assertTrue(PearlZipFXUtil.lookupArchiveInfo(archiveName)
                                            .get()
                                            .getFiles()
                                            .stream()
                                            .map(FileInfo::getFileName)
                                            .noneMatch(f->f.equals(file.toString())),
                              "File was not deleted"
        );
    }

    @Test
    @DisplayName("Test: Delete file successfully within Tar archive")
    public void testFX_DeleteFileTar_Success() throws IOException {
        final String archiveFormat = "tar";
        final String archiveName = String.format("test%s.%s", archiveFormat, archiveFormat);

        final Path archive = Paths.get(System.getProperty("user.home"), ".pz", "temp", archiveName);

        // Create archive of the appropriate format and add folder to archive...
        simNewArchive(this, archive);
        Assertions.assertTrue(lookupArchiveInfo(archiveName).isPresent(), "Archive is not open in PearlZip");
        simAddFolder(this, dir);

        Path file = Paths.get("root", "level1b", "DELETE_ME.txt");
        PearlZipFXUtil.simTraversalArchive(this, archiveName,"#fileContentsView",(r)->{}, SSV.split(file.toString()));
        PearlZipFXUtil.simDelete(this);
        Assertions.assertTrue(PearlZipFXUtil.lookupArchiveInfo(archiveName)
                                            .get()
                                            .getFiles()
                                            .stream()
                                            .map(FileInfo::getFileName)
                                            .noneMatch(f->f.equals(file.toString())),
                              "File was not deleted"
        );
    }

    @Test
    @DisplayName("Test: Delete file successfully within Jar archive")
    public void testFX_DeleteFileJar_Success() throws IOException {
        final String archiveFormat = "jar";
        final String archiveName = String.format("test%s.%s", archiveFormat, archiveFormat);

        final Path archive = Paths.get(System.getProperty("user.home"), ".pz", "temp", archiveName);

        // Create archive of the appropriate format and add folder to archive...
        simNewArchive(this, archive);
        Assertions.assertTrue(lookupArchiveInfo(archiveName).isPresent(), "Archive is not open in PearlZip");
        simAddFolder(this, dir);

        Path file = Paths.get("root", "level1b", "DELETE_ME.txt");
        PearlZipFXUtil.simTraversalArchive(this, archiveName,"#fileContentsView",(r)->{}, SSV.split(file.toString()));
        PearlZipFXUtil.simDelete(this);
        Assertions.assertTrue(PearlZipFXUtil.lookupArchiveInfo(archiveName)
                                            .get()
                                            .getFiles()
                                            .stream()
                                            .map(FileInfo::getFileName)
                                            .noneMatch(f->f.equals(file.toString())),
                              "File was not deleted"

        );
    }

    @Test
    @DisplayName("Test: Delete file not possible within GZip archive")
    public void testFX_DeleteFileGZip_Fail() throws IOException {
        final String archiveFormat = "tar.gz";
        final String archiveName = String.format("testgz.%s", archiveFormat);

        final Path archive = Paths.get(System.getProperty("user.home"), ".pz", "temp", archiveName);

        // Create archive of the appropriate format and add folder to archive...
        simNewArchive(this, archive);
        Assertions.assertTrue(lookupArchiveInfo(archiveName).isPresent(), "Archive is not open in PearlZip");

        Path file = Paths.get("testgz.tar");
        PearlZipFXUtil.simTraversalArchive(this, archiveName,"#fileContentsView",(r)->{}, SSV.split(file.toString()));
        PearlZipFXUtil.simDelete(this);
        Assertions.assertTrue(PearlZipFXUtil.lookupArchiveInfo(archiveName)
                                            .get()
                                            .getFiles()
                                            .stream()
                                            .map(FileInfo::getFileName)
                                            .anyMatch(f->f.equals(file.toString())),
                              "File was deleted unexpectedly"

        );
        Assertions.assertTrue(FormUtil.lookupNode((s)->s.getTitle().contains(archiveName), "#btnDelete").isDisable(), "Delete was not disabled");
    }

    @Test
    @DisplayName("Test: Delete file not possible within BZip archive")
    public void testFX_DeleteFileBZip_Fail() throws IOException {
        final String archiveFormat = "tar.bz2";
        final String archiveName = String.format("testbz2.%s", archiveFormat);

        final Path archive = Paths.get(System.getProperty("user.home"), ".pz", "temp", archiveName);

        // Create archive of the appropriate format and add folder to archive...
        simNewArchive(this, archive);
        Assertions.assertTrue(lookupArchiveInfo(archiveName).isPresent(), "Archive is not open in PearlZip");

        Path file = Paths.get("testbz2.tar");
        PearlZipFXUtil.simTraversalArchive(this, archiveName,"#fileContentsView",(r)->{}, SSV.split(file.toString()));
        PearlZipFXUtil.simDelete(this);
        Assertions.assertTrue(PearlZipFXUtil.lookupArchiveInfo(archiveName)
                                            .get()
                                            .getFiles()
                                            .stream()
                                            .map(FileInfo::getFileName)
                                            .anyMatch(f->f.equals(file.toString())),
                              "File was deleted unexpectedly"

        );
        Assertions.assertTrue(FormUtil.lookupNode((s)->s.getTitle().contains(archiveName), "#btnDelete").isDisable(), "Delete was not disabled");
    }

    @Test
    @DisplayName("Test: Delete file not possible within xz archive")
    public void testFX_DeleteFileXZ_Fail() throws IOException {
        final String archiveFormat = "tar.xz";
        final String archiveName = String.format("testxz.%s", archiveFormat);

        final Path archive = Paths.get(System.getProperty("user.home"), ".pz", "temp", archiveName);

        // Create archive of the appropriate format and add folder to archive...
        simNewArchive(this, archive);
        Assertions.assertTrue(lookupArchiveInfo(archiveName).isPresent(), "Archive is not open in PearlZip");

        Path file = Paths.get("testxz.tar");
        PearlZipFXUtil.simTraversalArchive(this, archiveName,"#fileContentsView",(r)->{}, SSV.split(file.toString()));
        PearlZipFXUtil.simDelete(this);
        Assertions.assertTrue(PearlZipFXUtil.lookupArchiveInfo(archiveName)
                                            .get()
                                            .getFiles()
                                            .stream()
                                            .map(FileInfo::getFileName)
                                            .anyMatch(f->f.equals(file.toString())),
                              "File was deleted unexpectedly"

        );
        Assertions.assertTrue(FormUtil.lookupNode((s)->s.getTitle().contains(archiveName), "#btnDelete").isDisable(), "Delete " +
                "was not " +
                "disabled");
    }

    @Test
    @DisplayName("Test: Delete folder successfully within Zip archive")
    public void testFX_DeleteFolderZip_Success() throws IOException {
        final String archiveFormat = "zip";
        final String archiveName = String.format("test%s.%s", archiveFormat, archiveFormat);

        final Path archive = Paths.get(System.getProperty("user.home"), ".pz", "temp", archiveName);

        // Create archive of the appropriate format and add folder to archive...
        simNewArchive(this, archive);
        Assertions.assertTrue(lookupArchiveInfo(archiveName).isPresent(), "Archive is not open in PearlZip");
        simAddFolder(this, dir);

        Path file = Paths.get("root", "level1b", "DELETE_ME_ALSO");
        PearlZipFXUtil.simTraversalArchive(this, archiveName,"#fileContentsView",(r)->{}, SSV.split(file.toString()));
        PearlZipFXUtil.simDelete(this);
        Assertions.assertTrue(PearlZipFXUtil.lookupArchiveInfo(archiveName)
                                            .get()
                                            .getFiles()
                                            .stream()
                                            .map(FileInfo::getFileName)
                                            .noneMatch(f->f.equals(file.toString())),
                              "File was not deleted"
        );
        Assertions.assertTrue(PearlZipFXUtil.lookupArchiveInfo(archiveName)
                                            .get()
                                            .getFiles()
                                            .stream()
                                            .map(FileInfo::getFileName)
                                            .noneMatch(f->f.endsWith("AUTO_DELETED.txt")),
                              "File was not deleted"
        );
    }

    @Test
    @DisplayName("Test: Delete folder successfully within Tar archive")
    public void testFX_DeleteFolderTar_Success() throws IOException {
        final String archiveFormat = "tar";
        final String archiveName = String.format("test%s.%s", archiveFormat, archiveFormat);

        final Path archive = Paths.get(System.getProperty("user.home"), ".pz", "temp", archiveName);

        // Create archive of the appropriate format and add folder to archive...
        simNewArchive(this, archive);
        Assertions.assertTrue(lookupArchiveInfo(archiveName).isPresent(), "Archive is not open in PearlZip");
        simAddFolder(this, dir);

        Path file = Paths.get("root", "level1b", "DELETE_ME_ALSO");
        PearlZipFXUtil.simTraversalArchive(this, archiveName,"#fileContentsView",(r)->{}, SSV.split(file.toString()));
        PearlZipFXUtil.simDelete(this);
        Assertions.assertTrue(PearlZipFXUtil.lookupArchiveInfo(archiveName)
                                            .get()
                                            .getFiles()
                                            .stream()
                                            .map(FileInfo::getFileName)
                                            .noneMatch(f->f.equals(file.toString())),
                              "File was not deleted"
        );
        Assertions.assertTrue(PearlZipFXUtil.lookupArchiveInfo(archiveName)
                                            .get()
                                            .getFiles()
                                            .stream()
                                            .map(FileInfo::getFileName)
                                            .noneMatch(f->f.endsWith("AUTO_DELETED.txt")),
                              "File was not deleted"
        );
    }

    @Test
    @DisplayName("Test: Delete folder successfully within Jar archive")
    public void testFX_DeleteFolderJar_Success() throws IOException {
        final String archiveFormat = "jar";
        final String archiveName = String.format("test%s.%s", archiveFormat, archiveFormat);

        final Path archive = Paths.get(System.getProperty("user.home"), ".pz", "temp", archiveName);

        // Create archive of the appropriate format and add folder to archive...
        simNewArchive(this, archive);
        Assertions.assertTrue(lookupArchiveInfo(archiveName).isPresent(), "Archive is not open in PearlZip");
        simAddFolder(this, dir);

        Path file = Paths.get("root", "level1b", "DELETE_ME_ALSO");
        PearlZipFXUtil.simTraversalArchive(this, archiveName,"#fileContentsView",(r)->{}, SSV.split(file.toString()));
        PearlZipFXUtil.simDelete(this);
        Assertions.assertTrue(PearlZipFXUtil.lookupArchiveInfo(archiveName)
                                            .get()
                                            .getFiles()
                                            .stream()
                                            .map(FileInfo::getFileName)
                                            .noneMatch(f->f.equals(file.toString())),
                              "File was not deleted"

        );
        Assertions.assertTrue(PearlZipFXUtil.lookupArchiveInfo(archiveName)
                                            .get()
                                            .getFiles()
                                            .stream()
                                            .map(FileInfo::getFileName)
                                            .noneMatch(f->f.endsWith("AUTO_DELETED.txt")),
                              "File was not deleted"
        );
    }
}
