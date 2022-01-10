/*
 * Copyright © 2021 92AK
 */

package com.ntak.pearlzip.archive.zip4j.pub;

import com.ntak.pearlzip.archive.pub.ErrorMessage;
import com.ntak.pearlzip.archive.zip4j.util.Zip4jUtil;
import com.ntak.pearlzip.ui.constants.ZipConstants;
import com.ntak.pearlzip.ui.model.FXArchiveInfo;
import com.ntak.pearlzip.ui.util.ArchiveUtil;
import com.ntak.pearlzip.ui.util.JFXUtil;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Duration;
import net.lingala.zip4j.model.enums.AesKeyStrength;
import net.lingala.zip4j.model.enums.EncryptionMethod;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static com.ntak.pearlzip.archive.constants.ArchiveConstants.CURRENT_SETTINGS;
import static com.ntak.pearlzip.archive.pub.ArchiveService.DEFAULT_BUS;
import static com.ntak.pearlzip.archive.util.LoggingUtil.resolveTextKey;
import static com.ntak.pearlzip.archive.zip4j.constants.Zip4jConstants.*;
import static com.ntak.pearlzip.ui.util.ArchiveUtil.*;
import static com.ntak.pearlzip.ui.util.JFXUtil.executeBackgroundProcess;

public class FrmZip4jEncryptController {
    private static final Logger LOGGER = LoggerContext.getContext()
                                                      .getLogger(FrmZip4jEncryptController.class);

    private final FXArchiveInfo fxArchiveInfo;
    private final Zip4jArchiveWriteService writeService;

    @FXML
    private Label lblPasswordDescription;

    @FXML
    private AnchorPane panePassword;

    @FXML
    private PasswordField textPassword;

    @FXML
    private Button btnContinue;

    @FXML
    private Button btnCancel;

    public FrmZip4jEncryptController(FXArchiveInfo fxArchiveInfo) {
        this.fxArchiveInfo = fxArchiveInfo;
        this.writeService = new Zip4jArchiveWriteService();
    }

    @FXML
    public void initialize() {
        // BODY: The archive %s can be encrypted by Zip4J. \nPlease enter a password below to encrypt the archive with:
        lblPasswordDescription.setText(resolveTextKey(BODY_ENCRYPT_PROMPT, fxArchiveInfo.getArchivePath()));

        textPassword.setOnKeyReleased((e) -> fxArchiveInfo.getArchiveInfo().addProperty(KEY_ENCRYPTION_PW, textPassword.getText().toCharArray()));

        btnCancel.setVisible(true);
        btnCancel.setOnAction(e -> btnCancel.getScene().getWindow().fireEvent(new WindowEvent(btnCancel.getScene().getWindow(), WindowEvent.WINDOW_CLOSE_REQUEST)));

        btnContinue.setOnAction(e -> {
            fxArchiveInfo.getArchiveInfo()
                         .addProperty(KEY_ENCRYPTION_ENABLE, true);
            if (ZIP_4J_VALIDATOR.test(fxArchiveInfo.getArchiveInfo())) {
                Path backupArchive = null;
                Optional<Stage> optStage = JFXUtil.getMainStageByArchivePath(fxArchiveInfo.getArchivePath());
                try {
                    // Extract archive
                    long sessionId = System.currentTimeMillis();
                    Path tempDir = Paths.get(ZipConstants.LOCAL_TEMP.toAbsolutePath()
                                                                    .toString(),
                                             String.format("pz%d", sessionId));
                    Files.createDirectories(tempDir);
                    extractToDirectory(sessionId, fxArchiveInfo, tempDir.toFile());

                    // Backup (Move) unencrypted archive
                    sessionId = System.currentTimeMillis();
                    Path backupDir = Paths.get(ZipConstants.LOCAL_TEMP.toAbsolutePath()
                                                                      .toString(),
                                               String.format("pz%d", sessionId));
                    Files.createDirectories(backupDir);
                    backupArchive = createBackupArchive(fxArchiveInfo, backupDir);

                    // Delete archive and create new encrypted archive
                    Files.deleteIfExists(Paths.get(fxArchiveInfo.getArchivePath()));
                    synchronized(CURRENT_SETTINGS) {
                        try {
                            fxArchiveInfo.getArchiveInfo()
                                         .addProperty(KEY_ENCRYPTION_METHOD,
                                                      EncryptionMethod.valueOf(CURRENT_SETTINGS.getProperty(
                                                              CNS_DEFAULT_ENCRYPTION_METHOD,
                                                              "AES")));
                            fxArchiveInfo.getArchiveInfo()
                                         .addProperty(KEY_ENCRYPTION_STRENGTH,
                                                      Zip4jUtil.getKeyStrength(CURRENT_SETTINGS.getProperty(
                                                              CNS_DEFAULT_ENCRYPTION_STRENGTH,
                                                              "256-bit")));
                        } catch (Exception exc) {
                            fxArchiveInfo.getArchiveInfo()
                                         .addProperty(KEY_ENCRYPTION_METHOD,
                                                      EncryptionMethod.AES);
                            fxArchiveInfo.getArchiveInfo()
                                         .addProperty(KEY_ENCRYPTION_STRENGTH,
                                                      AesKeyStrength.KEY_STRENGTH_256);
                        }
                    }
                    writeService.createArchive(sessionId, fxArchiveInfo.getArchiveInfo());
                    Map<Boolean,List<Path>> mapContents =
                            Files.list(tempDir)
                                 .collect(Collectors.partitioningBy(Files::isDirectory));
                    AtomicReference<Exception> capturedException = new AtomicReference<>();

                    Stage stage = (Stage)btnContinue.getScene().getWindow();

                    for (Path d : mapContents.get(true)) {
                        long dirSessionId = System.currentTimeMillis();
                        final CountDownLatch addDirLatch = new CountDownLatch(1);

                        executeBackgroundProcess(dirSessionId,
                                                 stage,
                                                 ()-> {
                                                     try {
                                                         addDirectory(dirSessionId,
                                                                      fxArchiveInfo,
                                                                      d.toFile());
                                                     } catch(Exception ex) {
                                                         capturedException.set(ex);
                                                     } finally {
                                                         addDirLatch.countDown();
                                                     }
                                                 },
                                                 (s)->{}
                                             );
                        addDirLatch.await();
                    }

                    for (Path f : mapContents.get(false)) {
                        long fileSessionId = System.currentTimeMillis();
                        final CountDownLatch addFileLatch = new CountDownLatch(1);
                        executeBackgroundProcess(fileSessionId,
                                                 stage,
                                                 () -> {
                                                     try {
                                                         addFile(fileSessionId,
                                                                 fxArchiveInfo,
                                                                 f.toFile(),
                                                                 f.getFileName()
                                                                  .toString());
                                                     } catch(Exception ex) {
                                                         capturedException.set(ex);
                                                     } finally {
                                                         addFileLatch.countDown();
                                                     }
                                                 },
                                                 (s) -> {}
                        );
                        addFileLatch.await();
                    }

                    // Rethrow Exception
                    if (Objects.nonNull(capturedException.get())) {
                        throw capturedException.get();
                    }

                    // Launch archive...
                    JFXUtil.runLater(() -> {
                        // Close archive UI instance
                        ((FXArchiveInfo) optStage.get()
                                                 .getUserData()).getCloseBypass()
                                                                .set(true);
                        optStage.ifPresent(s -> s.fireEvent(new WindowEvent(s, WindowEvent.WINDOW_CLOSE_REQUEST)));
                        launchMainStage(fxArchiveInfo);
                    });

                    // Wait till archive successfully opened...
                    while (JFXUtil.getMainStageInstances().size() == 0) {
                        Thread.sleep(100L);
                    }
                } catch(IOException ioe) {
                    // LOG: Issue backing up archive %s
                    LOGGER.error(resolveTextKey(LOG_ARCHIVE_Z4J_ISSUE_BACK_UP, fxArchiveInfo.getArchivePath()));
                    // TITLE: Issue backing up archive %s
                    // HEADER: Archive could not be backed up
                    // BODY: The encryption process could not complete due to an issue on the pre-requisite back up step.
                    // Please see stack trace for details.
                    long sessionId = System.currentTimeMillis();
                    DEFAULT_BUS.post(new ErrorMessage(sessionId,
                                                      resolveTextKey(TITLE_ARCHIVE_Z4J_ISSUE_BACK_UP),
                                                      resolveTextKey(HEADER_ARCHIVE_Z4J_ISSUE_BACK_UP,
                                                                     fxArchiveInfo.getArchivePath()),
                                                      resolveTextKey(BODY_ARCHIVE_Z4J_ISSUE_BACK_UP,
                                                                     e.getClass().getCanonicalName()),
                                                      ioe,
                                                      fxArchiveInfo.getArchiveInfo()));
                    ArchiveUtil.restoreBackupArchive(backupArchive, Paths.get(fxArchiveInfo.getArchivePath()));
                } catch (Exception exc) {
                    // Ignore interrupted exception
                    ArchiveUtil.restoreBackupArchive(backupArchive, Paths.get(fxArchiveInfo.getArchivePath()));
                }finally {
                    if (Objects.nonNull(backupArchive)) {
                        try {
                            Files.deleteIfExists(backupArchive);
                        } catch(IOException ex) {
                        }
                    }

                    PauseTransition delay = new PauseTransition(Duration.millis(300));
                    delay.setOnFinished((ev) -> btnCancel.getOnAction()
                                                         .handle(null));
                    delay.play();
                }
            }
        });
    }
}
