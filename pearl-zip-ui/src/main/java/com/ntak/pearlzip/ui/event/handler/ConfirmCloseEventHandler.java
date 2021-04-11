/*
 * Copyright © 2021 92AK
 */
package com.ntak.pearlzip.ui.event.handler;

import com.ntak.pearlzip.archive.pub.ArchiveWriteService;
import com.ntak.pearlzip.archive.pub.FileInfo;
import com.ntak.pearlzip.ui.model.FXArchiveInfo;
import com.ntak.pearlzip.ui.model.ZipState;
import com.ntak.pearlzip.ui.util.JFXUtil;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;

import static com.ntak.pearlzip.archive.constants.ConfigurationConstants.KEY_FILE_PATH;
import static com.ntak.pearlzip.archive.util.LoggingUtil.resolveTextKey;
import static com.ntak.pearlzip.ui.constants.ZipConstants.*;
import static com.ntak.pearlzip.ui.util.ArchiveUtil.addToRecentFile;
import static com.ntak.pearlzip.ui.util.JFXUtil.raiseAlert;

/**
 *  Event Handler functionality, which confirms the save of temporary archives on closure.
 *  @author Aashutos Kakshepati
*/
public class ConfirmCloseEventHandler implements EventHandler<WindowEvent> {
    private static final Logger LOGGER = LoggerContext.getContext().getLogger(ConfirmCloseEventHandler.class);
    private final Stage stage;
    private final FXArchiveInfo fxArchiveInfo;

    public ConfirmCloseEventHandler(Stage stage, FXArchiveInfo fxArchiveInfo) {
        this.stage = stage;
        this.fxArchiveInfo = fxArchiveInfo;
    }

    @Override
    public void handle(WindowEvent event) {
        try {
            if (fxArchiveInfo.getArchivePath()
                             .startsWith(STORE_TEMP.toString())) {
                // If a nested file from a parent archive an option is given to update it
                final String archiveFilePath = fxArchiveInfo.getArchivePath();
                final String parentPath = fxArchiveInfo.getParentPath();
                if (Objects.nonNull(parentPath)) {
                    ArchiveWriteService archiveWriteService =
                            ZipState.getWriteArchiveServiceForFile(parentPath).orElse(null);
                    if (Objects.nonNull(archiveWriteService)
                        // NOTE: Temporarily only execute for compressor archives
                        && ZipState.getCompressorArchives().contains(fxArchiveInfo.getParentPath().substring(fxArchiveInfo.getParentPath().lastIndexOf(".")+1))
                    ) {
                        // Nested archive - Ask user to update parent archive
                        // TITLE: Confirmation: Reintegrate archive changes in parent archive
                        // HEADER: Do you wish to reintegrate the nested archive changes into %s
                        // BODY: Please specify if you wish to persist the changes of the nested archive %s into %s.
                        Optional<ButtonType> response = raiseAlert(Alert.AlertType.CONFIRMATION,
                                                                   resolveTextKey(TITLE_CONFIRM_SAVE_NESTED_ARCHIVE),
                                                                   resolveTextKey(HEADER_CONFIRM_SAVE_NESTED_ARCHIVE,
                                                                                  parentPath),
                                                                   resolveTextKey(BODY_CONFIRM_SAVE_NESTED_ARCHIVE,
                                                                                  archiveFilePath,
                                                                                  parentPath),
                                                                   null,
                                                                   stage,
                                                                   ButtonType.YES, ButtonType.NO);

                        if (response.get().getButtonData() == ButtonBar.ButtonData.YES) {
                            // If compressor - create new archive with nested file. replace existing archive.
                            if (ZipState.getCompressorArchives().contains(fxArchiveInfo.getParentPath().substring(fxArchiveInfo.getParentPath().lastIndexOf(".")+1))) {
                                long sessionId = System.currentTimeMillis();
                                Path parentTempArchive = Paths.get(LOCAL_TEMP.toString(),
                                                             Paths.get(parentPath).getFileName().toString());
                                JFXUtil.executeBackgroundProcess(sessionId, stage,
                                                                 () -> archiveWriteService.createArchive(sessionId,
                                                                                                         parentTempArchive.toString(),
                                                                                                         new FileInfo(0, 0,
                                                                                                                      Paths.get(archiveFilePath).getFileName().toString(),
                                                                                                                      0,0,0,null,null,
                                                                                                                      null,null,null,0,"updated via PearlZip",
                                                                                                                      false,false,
                                                                                                                      Collections.singletonMap(KEY_FILE_PATH, fxArchiveInfo.getArchivePath())
                                                                                                         )),
                                                                 (s)->{
                                                                     if (Files.exists(parentTempArchive)) {
                                                                         try {
                                                                             Files.move(parentTempArchive, Paths.get(parentPath),
                                                                                        StandardCopyOption.REPLACE_EXISTING);
                                                                         } catch(IOException e) {
                                                                             // LOG: Error integrating changes from %s to %s
                                                                             LOGGER.error(resolveTextKey(
                                                                                     LOG_ISSUE_INTEGRATING_CHANGES, parentTempArchive, Paths.get(parentPath)));
                                                                         }
                                                                     }
                                                                 }
                                );

                                return;
                            }
                            // TODO: o/w - add file to existing archive? Need to pass nesting information down
                        }
                    }
                }

                // Archive is found in temporary storage and so prompt to save or delete
                // TITLE: Confirmation: Save temporary archive before exit
                // HEADER: Do you wish to save the open archive %s
                // BODY: Please specify if you wish to save the archive %s. If you do not wish to save the archive, it
                // will be removed from temporary storage.
                final String archiveFileName = Paths.get(fxArchiveInfo.getArchivePath())
                                      .getFileName()
                                      .toString();
                Optional<ButtonType> response = raiseAlert(Alert.AlertType.CONFIRMATION,
                                                           resolveTextKey(TITLE_CONFIRM_SAVE_ARCHIVE),
                                                           resolveTextKey(HEADER_CONFIRM_SAVE_ARCHIVE,
                                                                          archiveFileName),
                                                           resolveTextKey(BODY_CONFIRM_SAVE_ARCHIVE,
                                                                          archiveFileName),
                                                           null,
                                                           stage,
                                                           ButtonType.YES, ButtonType.NO);
                Path tempArchive = Paths.get(fxArchiveInfo.getArchivePath());
                if (response.isPresent()) {
                    if (response.get()
                                .equals(ButtonType.YES)) {
                        FileChooser saveDialog = new FileChooser();
                        // TITLE: Save archive to location...
                        saveDialog.setTitle(TITLE_TARGET_ARCHIVE_LOCATION);
                        String archiveName = tempArchive
                                .getFileName()
                                .toString();
                        saveDialog.setInitialFileName(archiveName);
                        // Save archive %s
                        saveDialog.setTitle(resolveTextKey(TITLE_SAVE_ARCHIVE_PATTERN, archiveName));
                        File savedFile = saveDialog.showSaveDialog(new Stage());

                        if (Objects.nonNull(savedFile)) {
                            Files.move(tempArchive, savedFile.toPath(), StandardCopyOption.ATOMIC_MOVE,
                                       StandardCopyOption.REPLACE_EXISTING);
                            addToRecentFile(savedFile);
                        }
                    }
                }
                Files.delete(tempArchive);
            }
        } catch(IOException e) {
            // Issue with IO Process when saving down archive %s
            LOGGER.warn(resolveTextKey(LOG_ISSUE_SAVE_ARCHIVE, fxArchiveInfo.getArchivePath()));
        }
    }
}
