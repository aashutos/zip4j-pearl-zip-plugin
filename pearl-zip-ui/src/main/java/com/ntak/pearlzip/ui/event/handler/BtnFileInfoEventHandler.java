/*
 * Copyright (c) ${YEAR} 92AK
 */
package com.ntak.pearlzip.ui.event.handler;

import com.ntak.pearlzip.archive.pub.FileInfo;
import com.ntak.pearlzip.archive.util.LoggingUtil;
import com.ntak.pearlzip.ui.model.FXArchiveInfo;
import com.ntak.pearlzip.ui.pub.FrmFileInfoController;
import com.ntak.pearlzip.ui.pub.ZipLauncher;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.ntak.pearlzip.archive.constants.LoggingConstants.LOG_BUNDLE;
import static com.ntak.pearlzip.archive.util.LoggingUtil.resolveTextKey;
import static com.ntak.pearlzip.ui.constants.ZipConstants.*;
import static com.ntak.pearlzip.ui.util.JFXUtil.raiseAlert;

/**
 *  Event Handler for Display File Information functionality.
 *  @author Aashutos Kakshepati
*/
public class BtnFileInfoEventHandler implements EventHandler<MouseEvent> {
    private static final Logger LOGGER = LoggerContext.getContext().getLogger(BtnFileInfoEventHandler.class);
    private final TableView<FileInfo> fileContentsView;
    private final FXArchiveInfo fxArchiveInfo;
    private final AtomicBoolean isRendered = new AtomicBoolean(false);

    public BtnFileInfoEventHandler(TableView<FileInfo> fileContentsView, FXArchiveInfo fxArchiveInfo) {
        this.fileContentsView = fileContentsView;
        this.fxArchiveInfo = fxArchiveInfo;
    }

    @Override
    public void handle(MouseEvent event) {
        FileInfo selectedFile = fileContentsView.getSelectionModel().getSelectedItem();
        if (Objects.isNull(selectedFile)) {
            // LOG: A file or folder was not selected from archive %s
            LOGGER.warn(resolveTextKey(LOG_NO_FILE_FOLDER_SELECTED, fxArchiveInfo.getArchivePath()));
            // TITLE: Information: No file or folder selected
            // HEADER: A file or folder has not been selected
            // BODY: Please select a file or folder.
            raiseAlert(Alert.AlertType.INFORMATION,
                       resolveTextKey(TITLE_NO_FILE_FOLDER_SELECTED),
                       resolveTextKey(HEADER_NO_FILE_FOLDER_SELECTED),
                       resolveTextKey(BODY_NO_FILE_FOLDER_SELECTED),
                       fileContentsView.getScene().getWindow()
            );
            return;
        }

        try {
            if (isRendered.get() == false) {

                isRendered.getAndSet(true);
                Stage stage = new Stage();

                FXMLLoader loader = new FXMLLoader();
                loader.setLocation(ZipLauncher.class.getClassLoader()
                                                    .getResource("frmFileInfo.fxml"));
                loader.setResources(LOG_BUNDLE);
                AnchorPane root = loader.load();

                FrmFileInfoController controller = loader.getController();
                controller.initData(stage, fileContentsView.getSelectionModel()
                                                    .getSelectedItem(), isRendered);

                Scene scene = new Scene(root);
                stage.setScene(scene);

                stage.show();
                stage.setAlwaysOnTop(true);
                stage.setAlwaysOnTop(false);

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
            isRendered.set(false);
        }
    }
}
