/*
 * Copyright © 2021 92AK
 */
package com.ntak.pearlzip.ui.event.handler;

import com.ntak.pearlzip.archive.util.LoggingUtil;
import com.ntak.pearlzip.ui.pub.FrmNewController;
import com.ntak.pearlzip.ui.pub.ZipLauncher;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;

import java.util.concurrent.atomic.AtomicBoolean;

import static com.ntak.pearlzip.archive.constants.LoggingConstants.LOG_BUNDLE;
import static com.ntak.pearlzip.archive.util.LoggingUtil.resolveTextKey;
import static com.ntak.pearlzip.ui.constants.ZipConstants.*;
import static com.ntak.pearlzip.ui.util.JFXUtil.raiseAlert;

/**
 *  Event Handler for Display New Dialog functionality.
 *  @author Aashutos Kakshepati
*/
public class BtnNewEventHandler implements EventHandler<MouseEvent> {

    private static final Logger LOGGER = LoggerContext.getContext().getLogger(BtnNewEventHandler.class);
    private static AtomicBoolean isRendered = new AtomicBoolean(false);

    @Override
    public void handle(MouseEvent event) {
        if (!isRendered.get()) {
            try {
                isRendered.getAndSet(true);

                // Initialise Stage
                Stage newStage = new Stage();
                VBox root;

                newStage.setTitle(resolveTextKey(TITLE_NEW_ARCHIVE_PATTERN));
                newStage.setResizable(false);
                FXMLLoader loader = new FXMLLoader();
                loader.setLocation(ZipLauncher.class.getClassLoader()
                                                    .getResource("frmNew.fxml"));
                loader.setResources(LOG_BUNDLE);
                root = loader.load();

                Scene scene = new Scene(root);
                newStage.setScene(scene);
                FrmNewController controller = loader.getController();
                controller.initData(newStage, isRendered);
                newStage.setOnCloseRequest((e)->isRendered.set(false));

                newStage.show();
                newStage.setAlwaysOnTop(true);
                newStage.setAlwaysOnTop(false);
            } catch(Exception e) {
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
                           null);
            }
        }
    }
}
