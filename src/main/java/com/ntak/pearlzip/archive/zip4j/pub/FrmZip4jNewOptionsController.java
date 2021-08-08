/*
 * Copyright © 2021 92AK
 */

package com.ntak.pearlzip.archive.zip4j.pub;

import com.ntak.pearlzip.archive.constants.ArchiveConstants;
import com.ntak.pearlzip.archive.pub.ArchiveInfo;
import com.ntak.pearlzip.archive.pub.ErrorMessage;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.AnchorPane;
import net.lingala.zip4j.model.enums.AesKeyStrength;
import net.lingala.zip4j.model.enums.CompressionMethod;
import net.lingala.zip4j.model.enums.EncryptionMethod;

import java.util.Objects;

import static com.ntak.pearlzip.archive.constants.ArchiveConstants.CURRENT_SETTINGS;
import static com.ntak.pearlzip.archive.pub.ArchiveService.DEFAULT_BUS;
import static com.ntak.pearlzip.archive.util.LoggingUtil.resolveTextKey;
import static com.ntak.pearlzip.archive.zip4j.constants.Zip4jConstants.*;

public class FrmZip4jNewOptionsController {

    private static final String[] ENCRYPTION_ALGORITHM = {"AES"};
    private static final String[] ENCRYPTION_STRENGTH = {"128-bit","256-bit"};

    @FXML
    private AnchorPane paneZip4jOptions;

    @FXML
    private ComboBox<String> comboCompressionMethod;
    @FXML
    private ComboBox<Integer> comboCompressionLevel;

    @FXML
    private CheckBox checkEnableEncryption;
    @FXML
    private PasswordField textEncryptionPassword;
    @FXML
    private ComboBox<String> comboEncryptionAlgorithm;
    @FXML
    private ComboBox<String> comboEncryptionStrength;

    @FXML
    public void initialize() {
        comboCompressionMethod.setItems(FXCollections.observableArrayList(COMPRESSION_METHOD));
        comboCompressionLevel.setItems(FXCollections.observableArrayList(COMPRESSION_LEVEL));

        comboEncryptionAlgorithm.setItems(FXCollections.observableArrayList(ENCRYPTION_ALGORITHM));
        comboEncryptionStrength.setItems(FXCollections.observableArrayList(ENCRYPTION_STRENGTH));

        comboCompressionLevel.setOnAction((e)-> {
            if (paneZip4jOptions.getUserData() instanceof ArchiveInfo archiveInfo) {
                archiveInfo.setCompressionLevel(comboCompressionLevel.getSelectionModel().getSelectedItem());
            }
        });

        comboCompressionMethod.setOnShowing((e)->{
            if (Objects.isNull(comboCompressionMethod.getValue())) {
                comboCompressionMethod.getSelectionModel()
                                     .select(Integer.parseInt(CURRENT_SETTINGS.getProperty(CNS_DEFAULT_COMPRESSION_METHOD,
                                                                                           "DEFLATE")));
            }
        });

        comboCompressionLevel.setOnShowing((e)->{
            if (Objects.isNull(comboCompressionLevel.getValue())) {
                comboCompressionLevel.getSelectionModel()
                                     .select(Integer.valueOf(CURRENT_SETTINGS.getProperty(CNS_DEFAULT_COMPRESSION_LEVEL,
                                                                                          "9")));
            }
        });

        comboCompressionMethod.setOnAction((e) -> {
            if (paneZip4jOptions.getUserData() instanceof ArchiveInfo archiveInfo) {
                archiveInfo.addProperty(KEY_COMPRESSION_METHOD,
                                        CompressionMethod.valueOf(comboCompressionMethod.getValue()));
            }
        });

        checkEnableEncryption.setOnAction((e)->{
            final boolean isEncrypted = this.checkEnableEncryption.isSelected();
            comboEncryptionAlgorithm.setDisable(!isEncrypted);
            comboEncryptionStrength.setDisable(!isEncrypted);
            textEncryptionPassword.setDisable(!isEncrypted);
            if (paneZip4jOptions.getUserData() instanceof ArchiveInfo archiveInfo) {
                if (isEncrypted) {
                    archiveInfo.addProperty(KEY_ENCRYPTION_ENABLE, true);
                } else {
                    archiveInfo.addProperty(KEY_ENCRYPTION_ENABLE, false);
                }
            }
        });

        comboEncryptionAlgorithm.setOnAction((e)->{
            if (!comboEncryptionAlgorithm.isDisabled() && paneZip4jOptions.getUserData() instanceof ArchiveInfo archiveInfo) {
                EncryptionMethod method = switch (comboEncryptionAlgorithm.getValue()) {
                    default -> EncryptionMethod.AES;
                };

                archiveInfo.addProperty(KEY_ENCRYPTION_METHOD, method);
            }
            if (comboEncryptionAlgorithm.isDisabled() && paneZip4jOptions.getUserData() instanceof ArchiveInfo archiveInfo) {
                archiveInfo.addProperty(KEY_ENCRYPTION_METHOD, null);
            }
        });

        comboEncryptionStrength.setOnAction((e)->{
            if (!comboEncryptionStrength.isDisabled() && paneZip4jOptions.getUserData() instanceof ArchiveInfo archiveInfo) {
                String encryptionStrength = comboEncryptionStrength.getValue();
                AesKeyStrength keyStrength = switch (encryptionStrength) {
                    case "128-bit" -> AesKeyStrength.KEY_STRENGTH_128;
                    case "192-bit" -> AesKeyStrength.KEY_STRENGTH_192;
                    default -> AesKeyStrength.KEY_STRENGTH_256;
                };
                archiveInfo.addProperty(KEY_ENCRYPTION_STRENGTH, keyStrength);
            }
            if (comboEncryptionStrength.isDisabled() && paneZip4jOptions.getUserData() instanceof ArchiveInfo archiveInfo) {
                archiveInfo.addProperty(KEY_ENCRYPTION_STRENGTH, null);
            }
        });

        textEncryptionPassword.setOnKeyReleased((e)->{
            if (!textEncryptionPassword.isDisabled() && paneZip4jOptions.getUserData() instanceof ArchiveInfo archiveInfo) {
                archiveInfo.addProperty(KEY_ENCRYPTION_PW, textEncryptionPassword.getText().toCharArray());
            }
            if (textEncryptionPassword.isDisabled() && paneZip4jOptions.getUserData() instanceof ArchiveInfo archiveInfo) {
                archiveInfo.addProperty(KEY_ENCRYPTION_PW, null);
            }
        });

        comboCompressionMethod.getSelectionModel().select(CURRENT_SETTINGS.getProperty(CNS_DEFAULT_COMPRESSION_METHOD, "DEFLATE"));
        comboCompressionLevel.getSelectionModel().select(Integer.valueOf(CURRENT_SETTINGS.getProperty(CNS_DEFAULT_COMPRESSION_LEVEL,"9")));
        comboEncryptionAlgorithm.getSelectionModel().select(ENCRYPTION_ALGORITHM.length-1);
        comboEncryptionStrength.getSelectionModel().select(ENCRYPTION_STRENGTH.length-1);

        ArchiveConstants.NEW_ARCHIVE_VALIDATORS.add((a)->{
            try {
                if (Objects.nonNull(a)) {
                    if (a.<Boolean>getProperty(KEY_ENCRYPTION_ENABLE).orElse(Boolean.FALSE).equals(Boolean.TRUE) && a.<char[]>getProperty(KEY_ENCRYPTION_PW)
                         .orElse(new char[0])
                         .length == 0) {
                        throw new IllegalStateException(resolveTextKey(LOG_Z4J_PW_LENGTH));
                    }

                    return true;
                }
            } catch (IllegalStateException e) {
                DEFAULT_BUS.post(new ErrorMessage(System.currentTimeMillis(),
                                                  resolveTextKey(TITLE_Z4J_VALIDATION_ISSUE),
                                                  null,
                                                  resolveTextKey(BODY_Z4J_VALIDATION_ISSUE, e.getMessage()),
                                                  e,
                                                  a));
            }
            return false;
        });
    }
}
