/*
 * Copyright © 2021 92AK
 */

package com.ntak.pearlzip.archive.zip4j.pub;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import net.lingala.zip4j.model.enums.EncryptionMethod;

import java.util.Objects;

import static com.ntak.pearlzip.archive.constants.ArchiveConstants.CURRENT_SETTINGS;
import static com.ntak.pearlzip.archive.constants.ArchiveConstants.WORKING_SETTINGS;
import static com.ntak.pearlzip.archive.zip4j.constants.Zip4jConstants.*;

public class FrmZip4jOptionsController {

    @FXML
    private ComboBox<String> comboDefaultCompressionMethod;
    @FXML
    private ComboBox<Integer> comboDefaultCompressionLevel;

    @FXML
    private ComboBox<EncryptionMethod> comboDefaultEncryptionMethod;
    @FXML
    private ComboBox<String> comboDefaultEncryptionStrength;

    @FXML
    public void initialize() {
        comboDefaultCompressionLevel.getItems().addAll(COMPRESSION_LEVEL);
        comboDefaultCompressionLevel.getSelectionModel().select(Integer.valueOf(CURRENT_SETTINGS.getProperty(CNS_DEFAULT_COMPRESSION_LEVEL,"9")));
        comboDefaultCompressionLevel.setOnAction((e) -> WORKING_SETTINGS.setProperty(CNS_DEFAULT_COMPRESSION_LEVEL,
                                                                             String.valueOf(comboDefaultCompressionLevel.getValue())));
        comboDefaultCompressionLevel.setOnShowing((e) -> {
            if (Objects.isNull(comboDefaultCompressionLevel.getValue())) {
                comboDefaultCompressionLevel.setValue(Integer.valueOf(CURRENT_SETTINGS.getProperty(
                        CNS_DEFAULT_COMPRESSION_LEVEL,
                        "9")));
            }
        });

        comboDefaultCompressionMethod.getItems().addAll(COMPRESSION_METHOD);
        comboDefaultCompressionMethod.getSelectionModel().select(CURRENT_SETTINGS.getProperty(CNS_DEFAULT_COMPRESSION_METHOD,"DEFLATE"));
        comboDefaultCompressionMethod.setOnAction((e) -> WORKING_SETTINGS.setProperty(CNS_DEFAULT_COMPRESSION_METHOD,
                                                                              comboDefaultCompressionMethod.getValue()));
        comboDefaultCompressionMethod.setOnShowing((e) -> {
            if (Objects.isNull(comboDefaultCompressionMethod.getValue())) {
                comboDefaultCompressionMethod.setValue(CURRENT_SETTINGS.getProperty(CNS_DEFAULT_COMPRESSION_METHOD,
                                                                                    "DEFLATE"));
            }
        });

        comboDefaultEncryptionMethod.getItems().add(EncryptionMethod.AES);
        comboDefaultEncryptionMethod.getSelectionModel().select(EncryptionMethod.valueOf(CURRENT_SETTINGS.getProperty(CNS_DEFAULT_ENCRYPTION_METHOD, "AES")));
        comboDefaultEncryptionMethod.setOnShowing((e) -> {
            if (Objects.isNull(comboDefaultEncryptionMethod.getValue())) {
                comboDefaultEncryptionMethod.setValue(EncryptionMethod.AES);
            }
        });
        comboDefaultEncryptionMethod.setOnAction((e) -> WORKING_SETTINGS.setProperty(CNS_DEFAULT_ENCRYPTION_METHOD,
                                                                                     comboDefaultEncryptionMethod.getValue()
                                                                                                                 .name()));

        comboDefaultEncryptionStrength.getItems().addAll(ENCRYPTION_STRENGTH);
        comboDefaultEncryptionStrength.getSelectionModel().select(CURRENT_SETTINGS.getProperty(CNS_DEFAULT_ENCRYPTION_STRENGTH, "256-bit"));
        comboDefaultEncryptionStrength.setOnShowing((e) -> {
            if (Objects.isNull(comboDefaultEncryptionStrength.getValue())) {
                comboDefaultEncryptionStrength.setValue(ENCRYPTION_STRENGTH[1]);
            }
        });
        comboDefaultEncryptionStrength.setOnAction((e) -> WORKING_SETTINGS.setProperty(CNS_DEFAULT_ENCRYPTION_STRENGTH,
                                                                                       comboDefaultEncryptionStrength.getValue()));
    }
}
