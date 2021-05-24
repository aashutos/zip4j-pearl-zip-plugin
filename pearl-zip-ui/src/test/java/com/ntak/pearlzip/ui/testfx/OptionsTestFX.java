/*
 * Copyright © 2021 92AK
 */
package com.ntak.pearlzip.ui.testfx;

import com.ntak.pearlzip.archive.acc.pub.CommonsCompressArchiveService;
import com.ntak.pearlzip.archive.pub.ArchiveReadService;
import com.ntak.pearlzip.archive.pub.ArchiveService;
import com.ntak.pearlzip.archive.pub.ArchiveWriteService;
import com.ntak.pearlzip.archive.szjb.pub.SevenZipArchiveService;
import com.ntak.pearlzip.ui.constants.ZipConstants;
import com.ntak.pearlzip.ui.util.AbstractPearlZipTestFX;
import com.ntak.pearlzip.ui.util.PearlZipFXUtil;
import javafx.geometry.Point2D;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import javafx.util.Pair;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import static com.ntak.pearlzip.ui.constants.ZipConstants.CNS_NTAK_PEARL_ZIP_NO_FILES_HISTORY;
import static com.ntak.pearlzip.ui.constants.ZipConstants.LOCAL_TEMP;
import static com.ntak.pearlzip.ui.util.PearlZipFXUtil.lookupArchiveInfo;
import static com.ntak.pearlzip.ui.util.PearlZipFXUtil.simOpenArchive;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class OptionsTestFX extends AbstractPearlZipTestFX {

    /*
     *  Test cases:
     *  + Clear cache with an open temporary archive. Temporary files except for open archive is removed
     *  + Clear cache with a saved archive. All temp files are removed
     *  + Check reserved keys in Bootstrap properties are as expected
     *  + Check the expected providers have been loaded
     */

    @Override
    public void start(Stage stage) throws IOException, TimeoutException {
        System.setProperty(CNS_NTAK_PEARL_ZIP_NO_FILES_HISTORY, "5");
        ZipConstants.LOCAL_TEMP = Paths.get(System.getProperty("user.home"), ".pz", "temp");
        ZipConstants.STORE_TEMP = Paths.get(System.getProperty("user.home"), ".pz", "temp");

        Files.list(LOCAL_TEMP)
             .filter(Files::isRegularFile)
             .forEach(f-> {
            try {
                Files.deleteIfExists(f);
            } catch(IOException e) {
            }
        });
        PearlZipFXUtil.initialise(stage,
                                  List.of(new CommonsCompressArchiveService()),
                                  List.of(new SevenZipArchiveService()),
                                  Paths.get(LOCAL_TEMP.toAbsolutePath().toString(), String.format("a%d.zip",
                                                                                                  System.currentTimeMillis()))
        );

        Files.createFile(Paths.get(LOCAL_TEMP.toAbsolutePath().toString(), "a1234567890.zip"));
    }

    @AfterEach
    @Override
    public void tearDown() throws Exception {
        Files.list(LOCAL_TEMP)
             .filter(Files::isRegularFile)
             .forEach(f-> {
                 try {
                     Files.deleteIfExists(f);
                 } catch(IOException e) {
                 }
             });
    }

    @Test
    @DisplayName("Test: Clear cache with an open temporary archive. Temporary files except for open archive is removed")
    public void testFX_ClearCacheTemporaryArchiveOpen_MatchExpectations() throws IOException {
        // Verify initial state
        Assertions.assertEquals(2,
                                Files.list(LOCAL_TEMP)
                                     .filter(Files::isRegularFile)
                                     .count(),
                                "Initial files have not been setup");

        Path fileToBeKept =
                Files.list(LOCAL_TEMP)
                     .filter(f->!f.getFileName().toString().endsWith("a1234567890.zip"))
                     .findFirst()
                     .get();

        Path fileToBeDeleted =
                Files.list(LOCAL_TEMP)
                     .filter(f->f.getFileName().toString().endsWith("a1234567890.zip"))
                     .findFirst()
                     .get();

        // Navigate to the clear cache option
        this.clickOn(Point2D.ZERO.add(160, 10))
            .clickOn(Point2D.ZERO.add(160, 30))
            .clickOn("#tabGeneral")
            .clickOn("#btnClearCache");

        DialogPane dialogPane = lookup(".dialog-pane").query();
        clickOn(dialogPane.lookupButton(ButtonType.YES));
        sleep(50, MILLISECONDS);

        // Check the outcomes are as expected
        Assertions.assertTrue(Files.exists(fileToBeKept), String.format("File %s was deleted unexpectedly", fileToBeKept));
        Assertions.assertFalse(Files.exists(fileToBeDeleted), String.format("File %s was kept unexpectedly", fileToBeDeleted));
    }

    @Test
    @DisplayName("Test: Clear cache with a saved archive. All temp files are removed")
    public void testFX_ClearCacheSavedArchiveOpen_MatchExpectations() throws IOException {
        // Verify initial state
        final List<Path> filesToBeDeleted = Files.list(LOCAL_TEMP)
                                             .filter(Files::isRegularFile).collect(Collectors.toList());
        Assertions.assertEquals(2, filesToBeDeleted.size(), "Initial files have not been setup");

        // Open existing archive in current window
        clickOn(Point2D.ZERO.add(110, 10)).clickOn(Point2D.ZERO.add(110, 60));
        final Path archivePath = Paths.get("src", "test", "resources", "test.zip")
                                      .toAbsolutePath();
        // Via Sys menu
        simOpenArchive(this, archivePath, false, false);
        sleep(50, TimeUnit.MILLISECONDS);
        Assertions.assertTrue(lookupArchiveInfo(archivePath.getFileName().toString()).isPresent(),"Expected archive " +
                "was not present");

        DialogPane dialogPane = lookup(".dialog-pane").query();
        clickOn(dialogPane.lookupButton(ButtonType.NO));
        sleep(50, MILLISECONDS);

        // Navigate to the clear cache option
        this.clickOn(Point2D.ZERO.add(160, 10))
            .clickOn(Point2D.ZERO.add(160, 30))
            .clickOn("#tabGeneral")
            .clickOn("#btnClearCache");

        dialogPane = lookup(".dialog-pane").query();
        clickOn(dialogPane.lookupButton(ButtonType.YES));
        sleep(250, MILLISECONDS);

        // Check the outcomes are as expected
        Assertions.assertTrue(filesToBeDeleted.stream().noneMatch(Files::exists), "Some temp files were not deleted");
    }

    @Test
    @DisplayName("Test: Check reserved keys in Bootstrap properties are as expected")
    public void testFX_ReservedKeysBootstrapProperties_MatchExpectations() throws IOException {
        // Initialise properties
        Properties bootstrap = new Properties();
        bootstrap.load(OptionsTestFX.class.getResourceAsStream("/application.properties"));
        bootstrap.entrySet().stream().forEach(e->System.setProperty(e.getKey().toString(), e.getValue().toString()));

        // Navigate to the Bootstrap properties tab
        this.clickOn(Point2D.ZERO.add(160, 10))
            .clickOn(Point2D.ZERO.add(160, 30))
            .clickOn("#tabBootstrap");

        // Retrieve properties TableView
        TableView<Pair<String,String>> propsGrid = lookup("#tblBootstrap").queryAs(TableView.class);
        List<Pair<String,String>> props = propsGrid.getItems();

        // Validate keys have expected values
        Map<String,String> actuals = new HashMap<>();
        props.stream().forEach(p->actuals.put(p.getKey(), p.getValue()));

        Files.lines(Paths.get(System.getProperty("user.home"), ".pz", "rk"))
             .forEach(k->Assertions.assertTrue(actuals.containsKey(k), String.format("Key %s does not exist in actuals",
                                                                                     k)));

        Files.lines(Paths.get(OptionsTestFX.class.getResource("/application.properties").getPath()));
    }

    @Test
    @DisplayName("Test: Check the expected providers have been loaded")
    public void testFX_Providers_MatchExpectations() throws IOException {
        // Initialise properties
        Properties bootstrap = new Properties();
        bootstrap.load(OptionsTestFX.class.getResourceAsStream("/application.properties"));
        bootstrap.entrySet().stream().forEach(e->System.setProperty(e.getKey().toString(), e.getValue().toString()));

        // Navigate to the Providers properties tab
        this.clickOn(Point2D.ZERO.add(160, 10))
            .clickOn(Point2D.ZERO.add(160, 30))
            .clickOn("#tabProviders");
        sleep(50, MILLISECONDS);

        // Retrieve properties TableView
        TableView<Pair<Boolean,ArchiveService>> propsGrid = lookup("#tblProviders").queryAs(TableView.class);
        List<Pair<Boolean,ArchiveService>> props = propsGrid.getItems();

        // Validate expected services
        Assertions.assertTrue(props.stream().map(Pair::getValue).anyMatch(s->s instanceof ArchiveReadService && s.getClass().getCanonicalName().equals("com.ntak.pearlzip.archive.szjb.pub.SevenZipArchiveService")), "No Read Service");
        Assertions.assertTrue(props.stream().map(Pair::getValue).anyMatch(s->s instanceof ArchiveWriteService && s.getClass().getCanonicalName().equals("com.ntak.pearlzip.archive.acc.pub.CommonsCompressArchiveService")), "No Write Service");
    }
}
