/*
 * Copyright (c) ${YEAR} 92AK
 */
package com.ntak.pearlzip.ui.event.handler;

import com.ntak.pearlzip.archive.pub.FileInfo;
import com.ntak.pearlzip.ui.model.FXArchiveInfo;
import com.ntak.pearlzip.ui.util.JFXUtil;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.TableView;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;

import java.io.File;
import java.util.Objects;

import static com.ntak.pearlzip.archive.util.LoggingUtil.resolveTextKey;
import static com.ntak.pearlzip.ui.constants.ZipConstants.TITLE_TARGET_DIR_LOCATION;
import static com.ntak.pearlzip.ui.util.ArchiveUtil.extractToDirectory;

/**
 *  Event Handler for Extract All functionality.
 *  @author Aashutos Kakshepati
*/
public class BtnExtractAllEventHandler implements EventHandler<ActionEvent> {
    private final TableView<FileInfo> fileContentsView;
    private final FXArchiveInfo fxArchiveInfo;
    private static final Logger LOGGER = LoggerContext.getContext().getLogger(BtnExtractFileEventHandler.class);

    public BtnExtractAllEventHandler(TableView<FileInfo> fileContentsView, FXArchiveInfo fxArchiveInfo) {
        this.fileContentsView = fileContentsView;
        this.fxArchiveInfo = fxArchiveInfo;
    }
    @Override
    public void handle(ActionEvent event) {
        if (Objects.nonNull(fxArchiveInfo.getReadService())) {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            // TITLE: Select target directory location for extraction...
            directoryChooser.setTitle(resolveTextKey(TITLE_TARGET_DIR_LOCATION));
            File dir = directoryChooser.showDialog(new Stage());

            if (Objects.nonNull(dir)) {
                long sessionId = System.currentTimeMillis();
                JFXUtil.executeBackgroundProcess(sessionId, (Stage) fileContentsView.getScene()
                                                                         .getWindow(),
                                                 () -> extractToDirectory(sessionId, fxArchiveInfo, dir),
                                                 (s) -> {}
                );
            }
        }
    }

}
