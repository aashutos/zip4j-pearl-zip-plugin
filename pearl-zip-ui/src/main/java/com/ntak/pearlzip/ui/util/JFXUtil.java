/*
 * Copyright © 2021 92AK
 */
package com.ntak.pearlzip.ui.util;

import com.ntak.pearlzip.archive.pub.ArchiveService;
import com.ntak.pearlzip.archive.pub.FileInfo;
import com.ntak.pearlzip.archive.pub.ProgressMessage;
import com.ntak.pearlzip.ui.model.FXArchiveInfo;
import javafx.collections.FXCollections;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.ntak.pearlzip.archive.constants.LoggingConstants.*;
import static com.ntak.pearlzip.archive.util.LoggingUtil.getStackTraceFromException;
import static com.ntak.pearlzip.archive.util.LoggingUtil.resolveTextKey;
import static com.ntak.pearlzip.ui.constants.ZipConstants.*;
import static com.ntak.pearlzip.ui.util.ArchiveUtil.launchProgress;
import static javafx.scene.control.ProgressIndicator.INDETERMINATE_PROGRESS;

/**
 *  Utility methods used by Pearl Zip to perform common JavaFX UI routines.
 *  @author Aashutos Kakshepati
*/
public class JFXUtil {
    private static final Logger LOGGER = LoggerContext.getContext().getLogger(JFXUtil.class);

    public static Optional<ButtonType> raiseAlert(Alert.AlertType type, String title, String header, String body,
            Window stage) {
        return raiseAlert(type, title, header, body, null, stage);
    }

    public static Optional<ButtonType> raiseAlert(Alert.AlertType type, String title, String header, String body,
            Exception exception, Window stage, ButtonType... buttons) {
        Alert alert = new Alert(type, body, buttons);
        alert.initOwner(stage);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(body);

        if (Objects.nonNull(exception)) {
            VBox vbox = new VBox();

            Label lblStackTrace = new Label("Stacktrace:");
            TextArea taTrace = new TextArea(getStackTraceFromException(exception));
            taTrace.setEditable(false);
            taTrace.setWrapText(true);

            taTrace.setMaxWidth(Double.MAX_VALUE);
            taTrace.setMaxHeight(Double.MAX_VALUE);

            vbox.getChildren().addAll(lblStackTrace,taTrace);
            alert.getDialogPane().setExpandableContent(vbox);
        }
        ((Stage)alert.getDialogPane().getScene().getWindow()).setAlwaysOnTop(true);
        return alert.showAndWait();
    }

    public static void changeButtonPicText(ButtonBase button, String imgResource, String labelText) {
        ((ImageView)button.getGraphic()).setImage(new Image(String.valueOf(JFXUtil.class.getClassLoader().getResource(imgResource))));
        button.setText(labelText);
    }

    public static void highlightCellIfMatch(TableCell cell, FileInfo row, FileInfo ref, BackgroundFill backgroundColor) {
        if (row.getFileName().equals(ref.getFileName())) {
            cell.setBackground(new Background(backgroundColor));
        }
    }

    public static Optional<Stage> getActiveStage() {
        return (Stage.getWindows()
                     .stream()
                     .filter(Window::isFocused)
                     .map(Stage.class::cast))
                .findFirst();
    }

    public static List<Stage> getMainStageInstances() {
        return Stage.getWindows()
                    .stream()
                    .map(Stage.class::cast)
                    .filter(s -> s.isShowing() && Optional.ofNullable(s.getTitle()).orElse("")
                                                   .matches(resolveTextKey(TITLE_FILE_PATTERN, ".*", ".*", ".*")))
                    .collect(Collectors.toList());
    }

    public static void refreshFileView(TableView<FileInfo> fileInfoTableView, FXArchiveInfo fxArchiveInfo, int depth,
            String prefix) {
        fxArchiveInfo.refresh();
        fxArchiveInfo.getDepth()
                     .set(depth);
        fxArchiveInfo.setPrefix(prefix);
        fileInfoTableView.setItems(FXCollections.observableArrayList(fxArchiveInfo.getFiles()
                                                                                  .stream()
                                                                                  .filter(f -> f.getLevel() == fxArchiveInfo.getDepth()
                                                                                                                           .get()
                                                                                         && f.getFileName()
                                                                                             .startsWith(
                                                                                                     fxArchiveInfo.getPrefix()))
                                                                                  .collect(
                                                                                         Collectors.toList())));
        fileInfoTableView.refresh();
    }

    public static void executeBackgroundProcess(long sessionId, Stage parent, CaughtRunnable process,
            Consumer<Stage> callback) {
        executeBackgroundProcess(sessionId, parent, process, (e)->{}, callback);
    }

    public static void executeBackgroundProcess(long sessionId, Stage parent, CaughtRunnable process,
            Consumer<Throwable> handler, Consumer<Stage> callback) {
        CountDownLatch latch = new CountDownLatch(1);

        PRIMARY_EXECUTOR_SERVICE.submit(()-> {
            try {
                latch.await();
                ArchiveService.DEFAULT_BUS.post(new ProgressMessage(sessionId, PROGRESS,
                                                                    resolveTextKey(LBL_PROGRESS_LOADING),
                                                                    INDETERMINATE_PROGRESS, 1));
                process.run();
            } catch (Exception e) {
                handler.accept(e);
                // LOG: %s thrown running a background process. \nMessage: %s\nStack trace:\n%s
                LOGGER.error(resolveTextKey(LOG_ISSUE_RUNNING_BACKGROUND_PROCESS), e.getClass().getCanonicalName(),
                             e.getMessage(),
                             getStackTraceFromException(e));
            } finally {
                ArchiveService.DEFAULT_BUS.post(new ProgressMessage(sessionId, COMPLETED, COMPLETED, 1, 1));
            }
        });

        launchProgress(sessionId, parent, latch, callback);
    }
}
