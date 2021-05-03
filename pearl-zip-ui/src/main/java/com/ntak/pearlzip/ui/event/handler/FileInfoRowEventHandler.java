/*
 * Copyright © 2021 92AK
 */
package com.ntak.pearlzip.ui.event.handler;

import com.ntak.pearlzip.archive.pub.ArchiveReadService;
import com.ntak.pearlzip.archive.pub.ArchiveWriteService;
import com.ntak.pearlzip.archive.pub.FileInfo;
import com.ntak.pearlzip.ui.model.FXArchiveInfo;
import com.ntak.pearlzip.ui.model.ZipState;
import com.ntak.pearlzip.ui.pub.ContextMenuController;
import com.ntak.pearlzip.ui.pub.ZipLauncher;
import com.ntak.pearlzip.ui.util.ArchiveUtil;
import com.ntak.pearlzip.ui.util.JFXUtil;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;

import static com.ntak.pearlzip.archive.constants.LoggingConstants.LOG_BUNDLE;
import static com.ntak.pearlzip.archive.util.LoggingUtil.resolveTextKey;
import static com.ntak.pearlzip.ui.constants.ZipConstants.*;
import static com.ntak.pearlzip.ui.util.ArchiveUtil.launchMainStage;
import static com.ntak.pearlzip.ui.util.JFXUtil.isFileInArchiveLevel;
import static com.ntak.pearlzip.ui.util.JFXUtil.raiseAlert;

/**
 *  Event Handler for processing archive file-entry click events.
 *  @author Aashutos Kakshepati
*/
public class FileInfoRowEventHandler implements  EventHandler<MouseEvent> {

    private static final Logger LOGGER = LoggerContext.getContext().getLogger(FileInfoRowEventHandler.class);
    private final TableView<FileInfo> fileContentsView;
    private final Button btnUp;
    private final TableRow<FileInfo> row;
    private final FXArchiveInfo fxArchiveInfo;

    public FileInfoRowEventHandler(TableView<FileInfo> fileContentsView, Button btnUp, TableRow<FileInfo> row,
            FXArchiveInfo fxArchiveInfo) {
        super();
        this.fileContentsView = fileContentsView;
        this.btnUp = btnUp;
        this.row = row;
        this.fxArchiveInfo = fxArchiveInfo;
    }

    @Override
    public void handle(MouseEvent event) {
            if (!row.isEmpty() && event.getButton() == MouseButton.PRIMARY
                    && event.getClickCount() == 2) {

                FileInfo clickedRow = row.getItem();
                LOGGER.debug(resolveTextKey(LOG_CLICKED_ROW, clickedRow.getFileName()));

                final Path selectedFile = Paths.get(clickedRow.getFileName());
                // An archive that can be opened by this application...
                long sessionId = System.currentTimeMillis();
                if (ZipState.supportedReadArchives().stream().anyMatch(e -> clickedRow.getFileName().endsWith(String.format(".%s",e)))) {
                    JFXUtil.executeBackgroundProcess(sessionId, (Stage) fileContentsView.getScene().getWindow(),
                                                     ()-> {
                                                         Platform.runLater(()->row.setDisable(true));

                                                         // LOG: An archive which can be extracted...
                                                         LOGGER.debug(resolveTextKey(LOG_ARCHIVE_CAN_EXTRACT));

                                                         // Extract tar ball into temp location from wrapped zip
                                                         final Path nestedArchive = Paths.get(STORE_TEMP.toAbsolutePath()
                                                                                                        .toString(),
                                                                                              selectedFile
                                                                                                      .getFileName().toString());
                                                         Files.deleteIfExists(nestedArchive);
                                                         ArchiveReadService archiveService =
                                                                 ZipState.getReadArchiveServiceForFile(clickedRow.getFileName())
                                                                         .get();
                                                         archiveService.extractFile(sessionId, nestedArchive,
                                                                                    fxArchiveInfo.getArchivePath(),
                                                                                    clickedRow
                                                         );

                                                         ArchiveWriteService archiveWriteService =
                                                                 ZipState.getWriteArchiveServiceForFile(clickedRow.getFileName())
                                                                         .get();

                                                         FXArchiveInfo archiveInfo;

                                                         if (ZipState.getCompressorArchives().contains(fxArchiveInfo.getArchivePath().substring(fxArchiveInfo.getArchivePath().lastIndexOf(".")+1))) {
                                                             archiveInfo = new FXArchiveInfo(fxArchiveInfo.getArchivePath(),
                                                                                             nestedArchive.toAbsolutePath().toString(), archiveService,
                                                                                             archiveWriteService);
                                                         } else {
                                                             archiveInfo = new FXArchiveInfo(nestedArchive.toAbsolutePath().toString(), archiveService,
                                                                                             archiveWriteService);
                                                         }

                                                         Platform.runLater(()->launchMainStage(archiveInfo));

                                                     },
                                                     (e)->{
                                                         // LOG: %s occurred on trying to open nested tar ball. Message: %s
                                                         LOGGER.error(resolveTextKey(LOG_ERR_OPEN_NESTED_TARBALL, e.getClass().getCanonicalName(), e.getMessage()));
                                                         // TITLE: Error: On extracting tarball
                                                         // HEADER: Issue extracting tarball
                                                         // BODY: An issue occurred on loading tar file: %s
                                                         Platform.runLater(
                                                                 ()-> raiseAlert(Alert.AlertType.WARNING,
                                                                                 resolveTextKey(TITLE_ERR_OPEN_NESTED_TARBALL),
                                                                                 resolveTextKey(HEADER_ERR_OPEN_NESTED_TARBALL),
                                                                                 resolveTextKey(BODY_ERR_OPEN_NESTED_TARBALL,
                                                                                   clickedRow.getFileName()),
                                                                                 (Exception)e,
                                                                                 fileContentsView.getScene().getWindow()));
                                                     },
                                                     (s)->{
                                                         final KeyFrame step1 = new KeyFrame(Duration.millis(300),
                                                                                             e -> {row.setDisable(false);
                                                                                             ((Stage)fileContentsView.getScene().getWindow()).toBack();
                                                         });
                                                         final Timeline timeline = new Timeline(step1);
                                                         Platform.runLater(timeline::play);
                                                     }
                    );
                    return;
                }

                if (clickedRow.isFolder()) {
                    fxArchiveInfo.getDepth()
                            .incrementAndGet();
                    fxArchiveInfo.setPrefix(clickedRow.getFileName());
                    fileContentsView.setItems(FXCollections.observableArrayList(fxArchiveInfo.getFiles()
                                                                                        .stream()
                                                                                        .filter(isFileInArchiveLevel(fxArchiveInfo))
                                                                                        .collect(
                                                                                                Collectors.toList())));
                    fileContentsView.refresh();
                    if (fxArchiveInfo.getDepth()
                                .get() > 0) {
                        btnUp.setVisible(true);
                    }
                } else { // Open file externally?
                    ArchiveUtil.openExternally(sessionId, (Stage) fileContentsView.getScene().getWindow(),
                                               fxArchiveInfo, clickedRow);
                }
            }
            else if (!row.isEmpty() && event.getButton() == MouseButton.SECONDARY) {
                try {
                    FXMLLoader loader = new FXMLLoader();
                    loader.setLocation(ZipLauncher.class.getClassLoader()
                                                        .getResource("contextmenu.fxml"));
                    loader.setResources(LOG_BUNDLE);
                    ContextMenu root = loader.load();
                    ContextMenuController controller = loader.getController();
                    controller.initData(fxArchiveInfo, row);
                    root.show(row, event.getScreenX(), event.getScreenY());
                } catch (Exception e) {

                }
            }
    }

}
