/*
 * Copyright (c) ${YEAR} 92AK
 */
package com.ntak.pearlzip.ui.event.handler;

import com.ntak.pearlzip.archive.pub.ArchiveWriteService;
import com.ntak.pearlzip.archive.pub.FileInfo;
import com.ntak.pearlzip.archive.util.LoggingUtil;
import com.ntak.pearlzip.ui.model.FXArchiveInfo;
import com.ntak.pearlzip.ui.util.ArchiveUtil;
import com.ntak.pearlzip.ui.util.JFXUtil;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.control.TableView;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

import static com.ntak.pearlzip.archive.util.LoggingUtil.resolveTextKey;
import static com.ntak.pearlzip.ui.constants.ZipConstants.*;
import static com.ntak.pearlzip.ui.util.JFXUtil.raiseAlert;

/**
 *  Event Handler for Add Directory functionality.
 *  @author Aashutos Kakshepati
*/
public class BtnAddDirEventHandler implements EventHandler<ActionEvent> {

    private static final Logger LOGGER = LoggerContext.getContext().getLogger(BtnAddDirEventHandler.class);

    private final TableView<FileInfo> fileContentsView;
    private final FXArchiveInfo fxArchiveInfo;

    public BtnAddDirEventHandler(TableView<FileInfo> fileContentsView, FXArchiveInfo fxArchiveInfo) {
        this.fileContentsView = fileContentsView;
        this.fxArchiveInfo = fxArchiveInfo;
    }

    @Override
    public void handle(ActionEvent event) {
        ArchiveWriteService archiveWriteService;
        try {
        if (Objects.nonNull(archiveWriteService = fxArchiveInfo.getWriteService())) {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            // TITLE: Select source directory location for augmentation...
            directoryChooser.setTitle(resolveTextKey(TITLE_SOURCE_DIR_LOCATION));
            final File dir = directoryChooser.showDialog(new Stage());
            final Path dirPath = dir.toPath();

            int depth = fxArchiveInfo.getDepth().get();
            int index = fxArchiveInfo.getFiles().size();
            String prefix = fxArchiveInfo.getPrefix();

            long sessionId = System.currentTimeMillis();
            JFXUtil.executeBackgroundProcess(sessionId, (Stage) fileContentsView.getScene().getWindow(),
                                             ()-> {
                    List<FileInfo> files = ArchiveUtil.handleDirectory(prefix, dirPath.getParent(), dirPath, depth+1, index);
                    archiveWriteService.addFile(sessionId, fxArchiveInfo.getArchivePath(),
                                                files.toArray(new FileInfo[0]));
                },
                                             (s)->JFXUtil.refreshFileView(fileContentsView, fxArchiveInfo, depth, prefix)
            );
        }
        } catch (Exception e) {
            // LOG: Issue creating stage.\nException type: %s\nMessage:%s\nStack trace:\n%s
            LOGGER.warn(resolveTextKey(LOG_ISSUE_CREATING_STAGE, e.getClass().getCanonicalName(),
                                       e.getMessage(),
                                       LoggingUtil.getStackTraceFromException(e)));
            // TITLE: ERROR: Issue creating stage
            // HEADER: There was an issue creating the required dialog
            // BODY: Upon initiating function '%s', an issue occurred on attempting to create the dialog. This
            // function will not proceed any further.
            raiseAlert(Alert.AlertType.ERROR, resolveTextKey(TITLE_ISSUE_CREATING_STAGE),
                       resolveTextKey(HEADER_ISSUE_CREATING_STAGE),
                       resolveTextKey(BODY_ISSUE_CREATING_STAGE, this.getClass().getName()), e,
                       fileContentsView.getScene().getWindow());
        }
    }

}
