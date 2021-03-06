/*
 * Copyright (c) ${YEAR} 92AK
 */
package com.ntak.pearlzip.ui.pub;

import com.ntak.pearlzip.archive.pub.FileInfo;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Pair;

import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 *  Controller for the File Information dialog.
 *  @author Aashutos Kakshepati
*/
public class FrmFileInfoController {

    // General accordion page
    @FXML
    private Label lblIndexValue;
    @FXML
    private Label lblLevelValue;
    @FXML
    private Label lblFilenameValue;
    @FXML
    private Label lblHashValue;
    @FXML
    private Label lblRawSizeValue;
    @FXML
    private Label lblPackedSizeValue;
    @FXML
    private Label lblFolderValue;
    @FXML
    private Label lblEncryptValue;
    @FXML
    private Label lblCommentsValue;

    // Timestamps accordion page
    @FXML
    private Label lblLastWriteTimeValue;
    @FXML
    private Label lblLastAccessTimeValue;
    @FXML
    private Label lblCreateTimeValue;

    // Ownerships accordion page
    @FXML
    private Label lblUserValue;
    @FXML
    private Label lblGroupValue;

    // Other accordion page
    @FXML
    private TableView<Pair<String,String>> tblOtherInfo;
    @FXML
    private TableColumn<Pair<String,String>, String> key;
    @FXML
    private TableColumn<Pair<String,String>, String> value;

    @FXML
    private Button btnClose;

    @FXML
    public void initialize() {
        key.setCellValueFactory((p)->new SimpleStringProperty(p.getValue().getKey()));
        value.setCellValueFactory((p)->new SimpleStringProperty(p.getValue().getValue()));
    }

    public void initData(Stage stage, FileInfo fileInfo, AtomicBoolean isRendered) {
        try {
            btnClose.setOnMouseClicked((e) -> stage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST)));
            stage.setOnCloseRequest((e) -> isRendered.set(false));

            // Set data on page
            lblIndexValue.setText(String.valueOf(fileInfo.getIndex()));
            lblLevelValue.setText(String.valueOf(fileInfo.getLevel()));
            lblFilenameValue.setText(Paths.get(fileInfo.getFileName()).getFileName().toString());
            lblHashValue.setText(String.format("0x%s",
                                               Long.toHexString(fileInfo.getCrcHash())
                                                   .toUpperCase()));
            lblRawSizeValue.setText(String.valueOf(fileInfo.getRawSize()));
            lblPackedSizeValue.setText(String.valueOf(fileInfo.getPackedSize()));
            lblFolderValue.setText(fileInfo.isFolder() ? "folder" : "file");
            lblEncryptValue.setText(fileInfo.isEncrypted() ? "encrypted" : "plaintext");
            lblCommentsValue.setText(fileInfo.getComments());

            if (Objects.nonNull(fileInfo.getLastWriteTime())) {
                lblLastWriteTimeValue.setText(fileInfo.getLastWriteTime()
                                                      .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            }
            if (Objects.nonNull(fileInfo.getLastAccessTime())) {
                lblLastAccessTimeValue.setText(fileInfo.getLastAccessTime()
                                                       .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            }
            if (Objects.nonNull(fileInfo.getCreationTime())) {
                lblCreateTimeValue.setText(fileInfo.getCreationTime()
                                                   .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            }

            lblUserValue.setText(fileInfo.getUser());
            lblGroupValue.setText(fileInfo.getGroup());

            if (Objects.nonNull(fileInfo.getAdditionalInfoMap())) {
                final List<Pair<String,String>> additionalInfo = new ArrayList<>();
                fileInfo.getAdditionalInfoMap()
                        .forEach((k, v) -> additionalInfo.add(new Pair<>(k, (String) v)));
                tblOtherInfo.setItems(FXCollections.observableArrayList(additionalInfo));
            }
        } catch (Exception e) {
            stage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST));
        }
    }
}
