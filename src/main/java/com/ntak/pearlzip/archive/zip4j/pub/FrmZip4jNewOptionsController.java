/*
 * Copyright © 2021 92AK
 */

package com.ntak.pearlzip.archive.zip4j.pub;

import com.ntak.pearlzip.archive.constants.ArchiveConstants;
import com.ntak.pearlzip.archive.pub.ArchiveInfo;
import com.ntak.pearlzip.archive.zip4j.util.Zip4jUtil;
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
import static com.ntak.pearlzip.archive.zip4j.constants.Zip4jConstants.*;

public class FrmZip4jNewOptionsController {

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
                AesKeyStrength keyStrength = Zip4jUtil.getKeyStrength(encryptionStrength);
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
        comboEncryptionAlgorithm.getSelectionModel().select(CURRENT_SETTINGS.getProperty(CNS_DEFAULT_ENCRYPTION_METHOD, "AES"));
        comboEncryptionStrength.getSelectionModel().select(CURRENT_SETTINGS.getProperty(CNS_DEFAULT_ENCRYPTION_STRENGTH, "256-bit"));

        if (!ArchiveConstants.NEW_ARCHIVE_VALIDATORS.contains(ZIP_4J_VALIDATOR)) {
            ArchiveConstants.NEW_ARCHIVE_VALIDATORS.add(ZIP_4J_VALIDATOR);
        }
    }
}
