/*
 * Copyright © 2021 92AK
 */
package com.ntak.pearlzip.ui.pub;

import com.ntak.pearlzip.archive.pub.ArchiveInfo;
import com.ntak.pearlzip.archive.pub.ArchiveService;
import com.ntak.pearlzip.archive.pub.ArchiveWriteService;
import com.ntak.pearlzip.archive.pub.FileInfo;
import com.ntak.pearlzip.ui.model.FXArchiveInfo;
import com.ntak.pearlzip.ui.model.ZipState;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Pair;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;

import java.io.File;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static com.ntak.pearlzip.archive.constants.ConfigurationConstants.KEY_FILE_PATH;
import static com.ntak.pearlzip.archive.constants.LoggingConstants.*;
import static com.ntak.pearlzip.archive.util.LoggingUtil.resolveTextKey;
import static com.ntak.pearlzip.ui.constants.ResourceConstants.PATTERN_FXID_NEW_OPTIONS;
import static com.ntak.pearlzip.ui.constants.ZipConstants.*;
import static com.ntak.pearlzip.ui.util.ArchiveUtil.launchMainStage;
import static com.ntak.pearlzip.ui.util.JFXUtil.executeBackgroundProcess;
import static com.ntak.pearlzip.ui.util.JFXUtil.raiseAlert;

/**
 *  Controller for the New Single File Compressor Archive dialog.
 *  @author Aashutos Kakshepati
*/
public class FrmNewSingleFileController {

    private static final Logger LOGGER =  LoggerContext.getContext().getLogger(FrmNewSingleFileController.class);

    @FXML
    private Button btnCreate;
    @FXML
    private Button btnCancel;
    @FXML
    private Button btnSelectFile;

    @FXML
    private TextField txtSelectFile;

    @FXML
    private Label lblSelectFile;

    @FXML
    private ComboBox<String> comboArchiveFormat;
    @FXML
    private TabPane tabsNew;

    private final Map<String,List<Tab>> CUSTOM_TAB_MAP = new ConcurrentHashMap<>();
    private final ArchiveInfo archiveInfo = new ArchiveInfo();
    private File selectedFile;

    @FXML
    public void initialize() {
        btnCreate.setDisable(true);

        lblSelectFile.setVisible(true);
        txtSelectFile.setVisible(true);
        txtSelectFile.setEditable(false);
        btnSelectFile.setVisible(true);

        Set<String> supportedWriteFormats =
                new HashSet<>(ZipState.getCompressorArchives()
                                      .stream()
                                      .filter(f->ZipState.supportedWriteArchives().contains(f))
                                      .collect(Collectors.toList())
                );

        for (ArchiveService service : ZipState.getWriteProviders()) {
            supportedWriteFormats.removeAll(service.getAliasFormats());
        }

        comboArchiveFormat.setItems(FXCollections.observableArrayList(supportedWriteFormats));
        comboArchiveFormat.getSelectionModel().selectFirst();

        for (ArchiveWriteService service : ZipState.getWriteProviders()) {
            if ((service.getCreateArchiveOptionsPane()).isPresent()) {
                Pair<String,Node> tab = service.getCreateArchiveOptionsPane()
                                               .get();

                Tab customTab = new Tab();
                customTab.setText(tab.getKey());
                customTab.setId(String.format(PATTERN_FXID_NEW_OPTIONS,
                                              service.getClass()
                                                     .getCanonicalName()));
                customTab.setContent(tab.getValue());
                tab.getValue().setUserData(archiveInfo);
                tabsNew.getTabs()
                       .add(customTab);

                for (String format : service.supportedWriteFormats()) {
                    List<Tab> tabs = CUSTOM_TAB_MAP.getOrDefault(format, new LinkedList<>());
                    tabs.add(customTab);
                    CUSTOM_TAB_MAP.put(format, tabs);
                }
            }
        }

        setTabVisibilityByFormat(comboArchiveFormat.getSelectionModel().getSelectedItem());
    }

    public void initData(Stage stage, AtomicBoolean isRendered) {
        btnCancel.setOnMouseClicked(e-> {
            try {
                stage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST));
            } finally {
                isRendered.getAndSet(false);
            }
        });

        archiveInfo.setArchiveFormat(comboArchiveFormat.getSelectionModel().getSelectedItem());
        comboArchiveFormat.setOnAction((a) -> {
                                           archiveInfo.setArchiveFormat(comboArchiveFormat.getSelectionModel()
                                                                                          .getSelectedItem());
                                           setTabVisibilityByFormat(comboArchiveFormat.getSelectionModel()
                                                                                      .getSelectedItem());
                                       }
        );

        btnSelectFile.setOnMouseClicked((e)->{
            FileChooser openDialog = new FileChooser();
            openDialog.setTitle(TITLE_SELECT_FILE_TO_COMPRESS);
            File selectedFile = openDialog.showOpenDialog(stage);
            if (Objects.nonNull(selectedFile) && selectedFile.exists()) {
                txtSelectFile.setText(selectedFile.getAbsolutePath());
                btnCreate.setDisable(false);
                this.selectedFile = selectedFile;
            } else {
                // File does not exist
                if (Objects.nonNull(selectedFile)) {
                    // LOG: File %s does not exist
                    // TITLE: File does not exist
                    // HEADER: File not selected or does not exist
                    // BODY: Please select a file. The file %s does not exist

                    LOGGER.warn(resolveTextKey(LOG_FILE_SELECTED_DOES_NOT_EXIST, selectedFile.getAbsolutePath()));

                    raiseAlert(Alert.AlertType.WARNING,
                               resolveTextKey(TITLE_FILE_SELECTED_DOES_NOT_EXIST),
                               resolveTextKey(HEADER_FILE_SELECTED_DOES_NOT_EXIST),
                               resolveTextKey(BODY_FILE_SELECTED_DOES_NOT_EXIST, selectedFile.getAbsolutePath()),
                               stage
                    );
                }

                this.selectedFile = null;
                btnCreate.setDisable(false);
            }
        });

        btnCreate.setOnMouseClicked((e) -> {
            File dir = null;
            try {
                long sessionId = System.currentTimeMillis();

                DirectoryChooser saveDialog = new DirectoryChooser();
                // TITLE: Save archive to location...
                saveDialog.setTitle(TITLE_TARGET_ARCHIVE_LOCATION);
                saveDialog.setTitle(resolveTextKey(TITLE_SAVE_ARCHIVE_PATTERN));
                dir = saveDialog.showDialog(stage);

                if (Objects.nonNull(dir)) {
                    final String archivePath = Paths.get(dir.getAbsolutePath(),
                                                         String.format("%s.%s", selectedFile.getName(),
                                                                       comboArchiveFormat.getSelectionModel()
                                                                                         .getSelectedItem())
                    )
                                                    .toAbsolutePath()
                                                    .toString();

                    ArchiveWriteService writeService =
                            ZipState.getWriteArchiveServiceForFile(archivePath)
                                    .orElse(null);

                    if (Objects.nonNull(writeService)) {
                        archiveInfo.setArchivePath(archivePath);
                        archiveInfo.setArchiveFormat(comboArchiveFormat.getSelectionModel()
                                                                       .getSelectedItem());
                        FileInfo singleFileInfo = new FileInfo(0, 0, selectedFile.getName(),
                                                               0, 0, 0,
                                                               null, null, null,
                                                               null, null, 0, null,
                                                               false, false,
                                                               Collections.singletonMap(KEY_FILE_PATH,
                                                                                        selectedFile.getAbsolutePath()));
                        executeBackgroundProcess(sessionId, stage,
                                                 () -> writeService.createArchive(sessionId,
                                                                                  archiveInfo,
                                                                                  singleFileInfo),
                                                 (s) -> {
                                                     FXArchiveInfo fxArchiveInfo = new FXArchiveInfo(archivePath,
                                                                                                     ZipState.getReadArchiveServiceForFile(
                                                                                                             archivePath)
                                                                                                             .get(),
                                                                                                     ZipState.getWriteArchiveServiceForFile(
                                                                                                             archivePath)
                                                                                                             .get());
                                                     launchMainStage(stage, fxArchiveInfo);
                                                 });
                    } else {
                        // LOG: No compatible Write Service found for the archive %s. Archive could not be created.
                        // TITLE: Issue creating archive
                        // HEADER: The archive %s could not be created
                        // BODY: No compatible Write Service found for the archive %s.

                        LOGGER.warn(resolveTextKey(LOG_ARCHIVE_SERVICE_CREATE_ISSUE, archivePath));
                        raiseAlert(Alert.AlertType.WARNING,
                                   resolveTextKey(TITLE_ARCHIVE_SERVICE_CREATE_ISSUE),
                                   resolveTextKey(HEADER_ARCHIVE_SERVICE_CREATE_ISSUE, archivePath),
                                   resolveTextKey(BODY_ARCHIVE_SERVICE_CREATE_ISSUE, archivePath),
                                   stage
                        );
                    }
                }
            } finally {
                if (Objects.nonNull(dir)) {
                    stage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST));
                }
            }
        });
    }

    private void setTabVisibilityByFormat(String format) {
        synchronized(tabsNew) {
            List<Tab> tabsToEnable = CUSTOM_TAB_MAP.getOrDefault(format, Collections.emptyList())
                                                   .stream()
                                                   .filter(Objects::nonNull)
                                                   .collect(Collectors.toList());
            tabsNew.getTabs()
                   .remove(1,
                           tabsNew.getTabs()
                                  .size());

            tabsNew.getTabs()
                   .addAll(tabsToEnable);
        }
    }
}
