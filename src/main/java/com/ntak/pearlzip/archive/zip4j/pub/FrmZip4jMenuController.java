/*
 * Copyright © 2021 92AK
 */

package com.ntak.pearlzip.archive.zip4j.pub;

import com.ntak.pearlzip.archive.pub.ArchiveInfo;
import com.ntak.pearlzip.archive.pub.ArchiveService;
import com.ntak.pearlzip.archive.pub.FileInfo;
import com.ntak.pearlzip.ui.constants.ZipConstants;
import com.ntak.pearlzip.ui.model.FXArchiveInfo;
import com.ntak.pearlzip.ui.util.JFXUtil;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.MenuItem;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;

import static com.ntak.pearlzip.archive.constants.ArchiveConstants.CURRENT_SETTINGS;
import static com.ntak.pearlzip.archive.constants.ConfigurationConstants.KEY_FILE_PATH;
import static com.ntak.pearlzip.archive.util.LoggingUtil.resolveTextKey;
import static com.ntak.pearlzip.archive.zip4j.constants.Zip4jConstants.*;
import static com.ntak.pearlzip.ui.constants.ZipConstants.TITLE_TARGET_ARCHIVE_LOCATION;
import static com.ntak.pearlzip.ui.util.ArchiveUtil.extractToDirectory;

public class FrmZip4jMenuController {
    private static final Logger LOGGER = LoggerContext.getContext()
                                                      .getLogger(FrmZip4jMenuController.class);

    @FXML
    MenuItem mnuSplitArchive;
    @FXML
    MenuItem mnuEncrypt;

    @FXML
    public void initialize() {
        mnuSplitArchive.setOnAction((e) -> {
            /*
                1. Check if is a zip archive (and unencrypted?)
                2. Set split flag and split size (by configuration)
                3. Prompt for folder location to create archive
                4. Create split archive
             */
            if (JFXUtil.getWindowsFromMenu().size() > 0) {
                Optional<String> optMenuItem = JFXUtil.getActiveWindowFromMenu();
                if (optMenuItem.isPresent()) {
                    String archivePath = optMenuItem.get();
                    Zip4jArchiveWriteService writeService = new Zip4jArchiveWriteService();
                    Optional<FXArchiveInfo> optFXArchiveInfo = JFXUtil.lookupArchiveInfo(archivePath);

                    FXArchiveInfo fxArchiveInfo;
                    if (archivePath.endsWith("zip") && !archivePath.startsWith(ZipConstants.LOCAL_TEMP.toString())
                            && !archivePath.startsWith(ZipConstants.STORE_TEMP.toString())
                            && (fxArchiveInfo = optFXArchiveInfo.orElse(null)) != null && !fxArchiveInfo.getArchiveInfo()
                                                                                                        .<Boolean>getProperty(
                                                                                                                KEY_ENCRYPTION_ENABLE)
                                                                                                        .orElse(false)) {
                        try {
                            final ArchiveInfo oneFileArchiveInfo = fxArchiveInfo.getArchiveInfo();
                            final ArchiveInfo archiveInfo = new ArchiveInfo();
                            archiveInfo.addProperty(KEY_SPLIT_ARCHIVE_ENABLE, true);
                            try {
                                archiveInfo.addProperty(KEY_SPLIT_ARCHIVE_SIZE,
                                                        Objects.isNull(CURRENT_SETTINGS.getProperty(
                                                                CNS_DEFAULT_SPLIT_ARCHIVE_SIZE)) ?
                                                                DEFAULT_SPLIT_ARCHIVE_SIZE :
                                                                Long.parseLong(CURRENT_SETTINGS.getProperty(
                                                                        CNS_DEFAULT_SPLIT_ARCHIVE_SIZE))
                                );
                            } catch(Exception exc) {
                                archiveInfo.addProperty(KEY_SPLIT_ARCHIVE_SIZE, DEFAULT_SPLIT_ARCHIVE_SIZE);
                            }

                            // Prompt for folder location
                            DirectoryChooser dirChooser = new DirectoryChooser();
                            dirChooser.setTitle(resolveTextKey(TITLE_TARGET_ARCHIVE_LOCATION));
                            File dir = dirChooser.showDialog(new Stage());
                            final String newArchivePath = Paths.get(dir.getAbsolutePath(),
                                                                    Paths.get(oneFileArchiveInfo.getArchivePath())
                                                                         .getFileName()
                                                                         .toString())
                                                               .toString();
                            archiveInfo.setArchivePath(newArchivePath);

                            // Extract archive
                            long backupSessionId = System.currentTimeMillis();
                            Path tempDir = Paths.get(ZipConstants.LOCAL_TEMP.toAbsolutePath()
                                                                            .toString(),
                                                     String.format("pz%d", backupSessionId),
                                                     Paths.get(archivePath)
                                                          .getFileName()
                                                          .toString());
                            Files.createDirectories(tempDir);
                            CountDownLatch latch = new CountDownLatch(1);
                            final Stage stage = (Stage) fxArchiveInfo.getController()
                                                                     .get()
                                                                     .getFileContentsView()
                                                                     .getScene()
                                                                     .getWindow();
                            JFXUtil.executeBackgroundProcess(backupSessionId,
                                                             stage,
                                                             () -> {
                                                                 try {
                                                                     extractToDirectory(backupSessionId,
                                                                                        fxArchiveInfo,
                                                                                        tempDir.toFile());
                                                                 } finally {
                                                                     latch.countDown();
                                                                 }
                                                             },
                                                             (s) -> {});
                            latch.await();

                            // Create FileInfo for extracted files...
                            FileInfo[] files = new FileInfo[]{new FileInfo(0,
                                                                           0,
                                                                           tempDir.getFileName()
                                                                                  .toString(),
                                                                           0,
                                                                           0,
                                                                           0
                                    ,
                                                                           LocalDateTime.now(),
                                                                           LocalDateTime.now(),
                                                                           LocalDateTime.now(),
                                                                           null,
                                                                           null,
                                                                           0,
                                                                           ""
                                    ,
                                                                           true,
                                                                           false,
                                                                           Collections.singletonMap(KEY_FILE_PATH,
                                                                                                    tempDir.toAbsolutePath()
                                                                                                           .toString()))
                            };

                            // Create Split archive
                            long splitSessionId = System.currentTimeMillis();
                            JFXUtil.executeBackgroundProcess(splitSessionId,
                                                             stage,
                                                             () -> writeService.createArchive(splitSessionId,
                                                                                              archiveInfo,
                                                                                              files),
                                                             (s) -> {}
                            );
                        } catch(Exception exc) {
                            // LOG: Archive %s could not be split. Exception message: %s.
                            // TITLE: ERROR: Issue splitting archive
                            // HEADER: Archive %s could not be split
                            // BODY: An exception was raised when trying to split archive %s. The process did not
                            // complete. Please see details below for more information.
                            LOGGER.error(resolveTextKey(LOG_ARCHIVE_Z4J_SPLIT_ARCHIVE_FAILED));
                            JFXUtil.raiseAlert(Alert.AlertType.ERROR,
                                               resolveTextKey(TITLE_ARCHIVE_Z4J_SPLIT_ARCHIVE_FAILED),
                                               resolveTextKey(HEADER_ARCHIVE_Z4J_SPLIT_ARCHIVE_FAILED,
                                                              archivePath),
                                               resolveTextKey(BODY_ARCHIVE_Z4J_SPLIT_ARCHIVE_FAILED,
                                                              archivePath),
                                               exc,
                                               null);
                        }
                    } else {
                            // LOG: Archive %s cannot be split
                            // TITLE: Incompatible archive for split process
                            // BODY: Current archive (%s) is not an archive that the Zip4J plugin can split. This could
                            //       be because:\n• It is not a zip archive\n• It is an encrypted archive
                            LOGGER.warn(resolveTextKey(LOG_ARCHIVE_Z4J_INCOMPATIBLE_SPLIT,
                                                       archivePath));
                            JFXUtil.raiseAlert(Alert.AlertType.INFORMATION,
                                               resolveTextKey(TITLE_ARCHIVE_Z4J_INCOMPATIBLE_SPLIT),
                                               null,
                                               resolveTextKey(BODY_ARCHIVE_Z4J_INCOMPATIBLE_SPLIT,
                                                              archivePath),
                                               null,
                                               null);
                        }
                }
            }
        });

        mnuEncrypt.setOnAction((e) -> {
            if (JFXUtil.getWindowsFromMenu().size() > 0) {
                Optional<String> optMenuItem =
                        JFXUtil.getActiveWindowFromMenu();
                if (optMenuItem.isPresent()) {
                    String archivePath = optMenuItem.get();
                    Zip4jArchiveWriteService writeService = new Zip4jArchiveWriteService();
                    Optional<FXArchiveInfo> optFXArchiveInfo = JFXUtil.lookupArchiveInfo(archivePath);

                    FXArchiveInfo fxArchiveInfo;
                    if (archivePath.endsWith("zip") && !archivePath.startsWith(ZipConstants.LOCAL_TEMP.toString())
                            && !archivePath.startsWith(ZipConstants.STORE_TEMP.toString())
                            && (fxArchiveInfo = optFXArchiveInfo.orElse(null)) != null && !fxArchiveInfo.getArchiveInfo()
                                                                                                        .<Boolean>getProperty(KEY_ENCRYPTION_ENABLE)
                                                                                                        .orElse(false)) {
                            // Prompt encryption of selected archive
                            Optional<ArchiveService.FXForm> encryptionPrompt =
                                    writeService.getFXFormByIdentifier(ENCRYPT_ARCHIVE_PROMPT, fxArchiveInfo);
                            JFXUtil.runLater(() -> {
                                    Stage stgEncPrompt = new Stage();
                                    Node root = encryptionPrompt.get()
                                                                .getContent();
                                    Scene scene = new Scene((Parent) root);
                                    stgEncPrompt.setScene(scene);
                                    stgEncPrompt.setTitle(encryptionPrompt.get().getName());
                                    stgEncPrompt.setAlwaysOnTop(true);
                                    stgEncPrompt.showAndWait();
                            });
                    } else {
                        // LOG: Archive %s cannot be encrypted
                        // TITLE: Incompatible archive for encryption
                        // BODY: Current archive (%s) is not an archive that the Zip4J plugin can encrypt. This could
                        //       be because:\n• It is not a zip archive\n• It is a temporary archive\n• Archive is
                        //       already encrypted
                        LOGGER.warn(resolveTextKey(LOG_ARCHIVE_Z4J_INCOMPATIBLE_ENCRYPT, archivePath));
                        JFXUtil.raiseAlert(Alert.AlertType.INFORMATION,
                                           resolveTextKey(TITLE_ARCHIVE_Z4J_INCOMPATIBLE_ENCRYPT),
                                           null,
                                           resolveTextKey(BODY_ARCHIVE_Z4J_INCOMPATIBLE_ENCRYPT,
                                                          archivePath),
                                           null,
                                           null);
                    }
                }
            }
        });
    }
}
