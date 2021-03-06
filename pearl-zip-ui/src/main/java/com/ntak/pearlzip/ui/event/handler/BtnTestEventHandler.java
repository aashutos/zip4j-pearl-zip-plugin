/*
 *  Copyright (c) 2021 92AK
 */
package com.ntak.pearlzip.ui.event.handler;

import com.ntak.pearlzip.archive.pub.ArchiveReadService;
import com.ntak.pearlzip.ui.model.FXArchiveInfo;
import com.ntak.pearlzip.ui.util.JFXUtil;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;

import java.nio.file.Paths;

import static com.ntak.pearlzip.archive.util.LoggingUtil.resolveTextKey;
import static com.ntak.pearlzip.ui.constants.ZipConstants.*;
import static com.ntak.pearlzip.ui.util.JFXUtil.raiseAlert;

/**
 *  Event Handler for Test Archive functionality.
 *  @author Aashutos Kakshepati
*/
public class BtnTestEventHandler implements EventHandler<MouseEvent> {
    private static final Logger LOGGER = LoggerContext.getContext().getLogger(BtnTestEventHandler.class);
    private final Stage stage;
    private final FXArchiveInfo fxArchiveInfo;

    public BtnTestEventHandler(Stage stage, FXArchiveInfo fxArchiveInfo) {
        this.stage = stage;
        this.fxArchiveInfo = fxArchiveInfo;
    }

    @Override
    public void handle(MouseEvent event) {
        ArchiveReadService readService = fxArchiveInfo.getReadService();
        long sessionId = System.currentTimeMillis();

        JFXUtil.executeBackgroundProcess(sessionId, stage,
                                         ()-> {
                                             boolean success = readService.testArchive(sessionId,
                                                                                       fxArchiveInfo.getArchivePath());

                                             String archiveName = Paths.get(fxArchiveInfo.getArchivePath())
                                                                       .getFileName()
                                                                       .toString();

                                             Platform.runLater(() -> {
                                                 if (success) {
                                                     // TITLE: Test successful
                                                     // HEADER: Successful test of archive %s
                                                     // BODY: Parsing of Zip file %s successfully
                                                     raiseAlert(Alert.AlertType.INFORMATION,
                                                                resolveTextKey(TITLE_TEST_ARCHIVE_SUCCESS),
                                                                resolveTextKey(HEADER_TEST_ARCHIVE_SUCCESS,
                                                                                           archiveName),
                                                                resolveTextKey(BODY_TEST_ARCHIVE_SUCCESS,
                                                                                           archiveName),
                                                                stage
                                                     );
                                                 } else {
                                                     // TITLE: Test failure
                                                     // HEADER: Unsuccessful test of archive %s
                                                     // BODY: Parsing of Zip file %s failed. Check log output for more information.
                                                     raiseAlert(Alert.AlertType.INFORMATION,
                                                                resolveTextKey(TITLE_TEST_ARCHIVE_FAILURE),
                                                                resolveTextKey(HEADER_TEST_ARCHIVE_FAILURE,
                                                                                           archiveName),
                                                                resolveTextKey(BODY_TEST_ARCHIVE_FAILURE,
                                                                                           archiveName),
                                                                stage
                                                     );
                                                 }
                                             });
                                         },
                                         (s)->{}
        );
    }
}
