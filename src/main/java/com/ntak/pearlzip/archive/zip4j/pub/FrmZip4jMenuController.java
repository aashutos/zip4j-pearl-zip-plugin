/*
 * Copyright © 2021 92AK
 */

package com.ntak.pearlzip.archive.zip4j.pub;

import com.ntak.pearlzip.archive.pub.ArchiveService;
import com.ntak.pearlzip.ui.constants.ResourceConstants;
import com.ntak.pearlzip.ui.constants.ZipConstants;
import com.ntak.pearlzip.ui.model.FXArchiveInfo;
import com.ntak.pearlzip.ui.util.JFXUtil;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.MenuItem;
import javafx.stage.Stage;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static com.ntak.pearlzip.archive.util.LoggingUtil.resolveTextKey;
import static com.ntak.pearlzip.archive.zip4j.constants.Zip4jConstants.*;

public class FrmZip4jMenuController {
    private static final Logger LOGGER = LoggerContext.getContext()
                                                      .getLogger(FrmZip4jMenuController.class);

    @FXML
    MenuItem mnuSplitArchive;
    @FXML
    MenuItem mnuEncrypt;

    @FXML
    public void initialize() {
        mnuSplitArchive.setOnAction((e) -> {

        });

        mnuEncrypt.setOnAction((e) -> {
            if (Objects.nonNull(ResourceConstants.WINDOW_MENU) && ResourceConstants.WINDOW_MENU.getItems().size() > 0) {
                Optional<MenuItem> optMenuItem =
                        ResourceConstants.WINDOW_MENU.getItems().stream().filter(f->f.getText().contains(ZipConstants.WINDOW_FOCUS_SYMBOL)).findFirst();
                if (optMenuItem.isPresent()) {
                    String archivePath = optMenuItem.get().getText().replace(ZipConstants.WINDOW_FOCUS_SYMBOL, "");
                    Zip4jArchiveWriteService writeService = new Zip4jArchiveWriteService();
                    Optional<FXArchiveInfo> optFXArchiveInfo = JFXUtil.lookupArchiveInfo(archivePath);

                    FXArchiveInfo fxArchiveInfo;
                    if (archivePath.endsWith("zip") && !archivePath.startsWith(ZipConstants.LOCAL_TEMP.toString())
                            && !archivePath.startsWith(ZipConstants.STORE_TEMP.toString())
                            && (fxArchiveInfo = optFXArchiveInfo.orElse(null)) != null && !fxArchiveInfo.getArchiveInfo()
                                                                                                        .<Boolean>getProperty(
                                                                                                                KEY_ENCRYPTION_ENABLE)
                                                                                                        .orElse(false)) {
                            // Prompt encryption of selected archive
                            Optional<ArchiveService.FXForm> encryptionPrompt =
                                    writeService.getFXFormByIdentifier(ENCRYPT_ARCHIVE_PROMPT, fxArchiveInfo);
                            AtomicReference<Stage> refStgEnc = new AtomicReference<>();
                            JFXUtil.runLater(() -> {
                                    Stage stgEncPrompt = new Stage();
                                    Node root = encryptionPrompt.get()
                                                                .getContent();
                                    Scene scene = new Scene((Parent) root);
                                    stgEncPrompt.setScene(scene);
                                    stgEncPrompt.setTitle(encryptionPrompt.get().getName());
                                    refStgEnc.set(stgEncPrompt);
                                    stgEncPrompt.setAlwaysOnTop(true);
                                    stgEncPrompt.showAndWait();
                            });
                    } else {
                        // LOG: Archive %s cannot be encrypted
                        // TITLE: Incompatible archive for encryption
                        // BODY: Current archive (%s) is not an archive that the Zip4J plugin can encrypt. This could
                        //       be because:\n• It is not a zip archive\n• It is a temporary archive
                        long sessionId = System.currentTimeMillis();
                        LOGGER.warn(resolveTextKey(LOG_ARCHIVE_Z4J_INCOMPATIBLE_ENCRYPT,e.getClass().getCanonicalName()));
                        JFXUtil.raiseAlert(Alert.AlertType.INFORMATION,
                                           resolveTextKey(TITLE_ARCHIVE_Z4J_INCOMPATIBLE_ENCRYPT),
                                           null,
                                           resolveTextKey(BODY_ARCHIVE_Z4J_INCOMPATIBLE_ENCRYPT,
                                                                         e.getClass().getCanonicalName()),
                                           null,
                                           null);
                    }
                }
            }
        });
    }
}
