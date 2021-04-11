/*
 * Copyright © 2021 92AK
 */
package com.ntak.pearlzip.ui.event.handler;

import com.ntak.pearlzip.archive.pub.ArchiveReadService;
import com.ntak.pearlzip.archive.pub.FileInfo;
import com.ntak.pearlzip.ui.model.FXArchiveInfo;
import com.ntak.pearlzip.ui.util.JFXUtil;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.control.TableView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;

import java.io.File;
import java.nio.file.Paths;
import java.util.Objects;

import static com.ntak.pearlzip.archive.util.LoggingUtil.resolveTextKey;
import static com.ntak.pearlzip.ui.constants.ZipConstants.*;
import static com.ntak.pearlzip.ui.util.JFXUtil.raiseAlert;

/**
 *  Event Handler for Extract File functionality.
 *  @author Aashutos Kakshepati
*/
public class BtnExtractFileEventHandler implements EventHandler<ActionEvent> {
    private final TableView<FileInfo> fileContentsView;
    private final FXArchiveInfo fxArchiveInfo;
    private static final Logger LOGGER = LoggerContext.getContext().getLogger(BtnExtractFileEventHandler.class);

    public BtnExtractFileEventHandler(TableView<FileInfo> fileContentsView, FXArchiveInfo fxArchiveInfo) {
        this.fileContentsView = fileContentsView;
        this.fxArchiveInfo = fxArchiveInfo;
    }


    @Override
    public void handle(ActionEvent event) {
        ArchiveReadService readService;
        if (Objects.isNull(readService = fxArchiveInfo.getReadService())) {
            // LOG: Extract functionality not supported for archive %s
            LOGGER.warn(resolveTextKey(LOG_EXT_FUNC_NOT_SUPPORTED, fxArchiveInfo.getArchivePath()));
            // TITLE: Warning: Extract functionality not supported
            // HEADER: No Write provider for archive format
            // BODY: Cannot extract file to archive as functionality is not supported for file: %s
            raiseAlert(Alert.AlertType.WARNING,
                       resolveTextKey(TITLE_EXT_FUNC_NOT_SUPPORTED),
                       resolveTextKey(HEADER_EXT_FUNC_NOT_SUPPORTED),
                       resolveTextKey(BODY_EXT_FUNC_NOT_SUPPORTED,
                                      Paths.get(fxArchiveInfo.getArchivePath())
                                          .getFileName()
                                          .toString()),
                       fileContentsView.getScene().getWindow()
            );
            return;
        }

        FileInfo selectedFile = fileContentsView.getSelectionModel().getSelectedItem();
        long sessionId = System.currentTimeMillis();
        if (Objects.isNull(selectedFile) || selectedFile.isFolder()) {
            // LOG: No file has been selected from archive %s
            LOGGER.warn(resolveTextKey(LOG_NO_FILE_SELECTED, fxArchiveInfo.getArchivePath()));
            // TITLE: Information: No file selected
            // HEADER: A file has not been selected
            // BODY: Please select a file.
            raiseAlert(Alert.AlertType.INFORMATION,
                       resolveTextKey(TITLE_NO_FILE_SELECTED),
                       resolveTextKey(HEADER_NO_FILE_SELECTED),
                       resolveTextKey(BODY_NO_FILE_SELECTED),
                       fileContentsView.getScene().getWindow()
            );
        } else {
            FileChooser addFileView = new FileChooser();
            // Title: Extract file %s to...
            addFileView.setTitle(resolveTextKey(TITLE_EXTRACT_ARCHIVE_PATTERN, fxArchiveInfo.getArchivePath()));
            addFileView.setInitialFileName(Paths.get(selectedFile.getFileName())
                                                .getFileName()
                                                .toString());
            File destPath = addFileView.showSaveDialog(new Stage());

            if (Objects.nonNull(destPath)) {
                JFXUtil.executeBackgroundProcess(sessionId, (Stage) fileContentsView.getScene().getWindow(),
                                                 ()->readService.extractFile(sessionId, destPath.toPath(),
                                                                             fxArchiveInfo.getArchivePath(), selectedFile),
                                                 (s)->{}
                );
            }
        }
    }
}
