/*
 * Copyright (c) ${YEAR} 92AK
 */
package com.ntak.pearlzip.ui.pub;

import com.ntak.pearlzip.archive.pub.ArchiveInfo;
import com.ntak.pearlzip.ui.event.handler.BtnCreateEventHandler;
import com.ntak.pearlzip.ui.model.ZipState;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 *  Controller for the New Archive dialog.
 *  @author Aashutos Kakshepati
*/
public class FrmNewController {

    @FXML
    private Button btnCreate;
    @FXML
    private Button btnCancel;

    @FXML
    private ComboBox<String> comboArchiveFormat;
    @FXML
    private ComboBox<Integer> comboCmpLevel;

    @FXML
    public void initialize() {
        comboArchiveFormat.setItems(FXCollections.observableArrayList(ZipState.supportedWriteArchives()));
        comboArchiveFormat.getSelectionModel().selectFirst();
        // TODO: Functionality to be added later
        comboCmpLevel.setItems(FXCollections.observableArrayList(9));
        comboCmpLevel.getSelectionModel().selectFirst();
        comboCmpLevel.setDisable(true);
    }

    public void initData(Stage stage, AtomicBoolean isRendered) {
        btnCancel.setOnMouseClicked(e-> {
            try {
                stage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST));
            } finally {
                isRendered.getAndSet(false);
            }
        });

        ArchiveInfo archiveInfo = new ArchiveInfo();
        archiveInfo.setArchiveFormat(comboArchiveFormat.getSelectionModel().getSelectedItem());
        archiveInfo.setCompressionLevel(comboCmpLevel.getSelectionModel().getSelectedItem());
        comboArchiveFormat.setOnAction((a) -> archiveInfo.setArchiveFormat(comboArchiveFormat.getSelectionModel().getSelectedItem()));
        comboCmpLevel.setOnAction((a) -> archiveInfo.setCompressionLevel(comboCmpLevel.getSelectionModel().getSelectedItem()));

        btnCreate.setOnMouseClicked(new BtnCreateEventHandler(stage, isRendered, archiveInfo));
    }
}
