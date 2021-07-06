/*
 * Copyright © 2021 92AK
 */

package com.ntak.pearlzip.archive.zip4j.pub;

import com.ntak.pearlzip.archive.pub.ArchiveInfo;
import com.ntak.pearlzip.archive.pub.FileInfo;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.WindowEvent;
import javafx.util.Pair;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.ntak.pearlzip.archive.util.LoggingUtil.resolveTextKey;
import static com.ntak.pearlzip.archive.zip4j.constants.Zip4jConstants.*;

public class FrmZip4jPasswordController {

    final AtomicBoolean isValid = new AtomicBoolean(false);
    private final ArchiveInfo archiveInfo;
    private final Zip4jArchiveReadService service;

    @FXML
    private AnchorPane panePassword;

    @FXML
    private PasswordField textPassword;

    @FXML
    private Button btnContinue;

    public FrmZip4jPasswordController(Zip4jArchiveReadService service, ArchiveInfo archiveInfo) {
        this.archiveInfo = archiveInfo;
        this.service = service;
    }

    @FXML
    public void initialize() {
        textPassword.setOnKeyReleased((e)->{
            archiveInfo.addProperty(KEY_ENCRYPTION_PW, textPassword.getText().toCharArray());
        });

        btnContinue.setOnAction(e->{
            long sessionId = System.currentTimeMillis();
            // Assumption: All files are encrypted uniformly in archive. So an arbitrary file can be taken to test
            // password...
            Optional<FileInfo> encFile = service.listFiles(sessionId, archiveInfo)
                                              .stream()
                                              .filter(f -> !f.isFolder())
                                              .findFirst();
            if (encFile.isPresent()) {
                try {
                    Path path = Files.createTempFile("tmp", "");
                    isValid.set(service.extractFile(sessionId, path, archiveInfo, encFile.get()));
                    Files.deleteIfExists(path);

                    if (isValid.get()) {
                        panePassword.setUserData(new Pair<>(isValid, resolveTextKey(LOG_ARCHIVE_Z4J_PASSWORD_SUCCESS)));
                    } else {
                        panePassword.setUserData(new Pair<>(isValid, resolveTextKey(LOG_ARCHIVE_Z4J_PASSWORD_FAIL)));
                    }
                } catch (IOException exc) {
                    panePassword.setUserData(new Pair<>(isValid, resolveTextKey(LOG_ARCHIVE_Z4J_PASSWORD_FAIL)));
                } finally {
                    btnContinue.getScene().getWindow().fireEvent(new WindowEvent(btnContinue.getScene().getWindow(),
                                                                                 WindowEvent.WINDOW_CLOSE_REQUEST));
                }
            }
        });
    }
}
