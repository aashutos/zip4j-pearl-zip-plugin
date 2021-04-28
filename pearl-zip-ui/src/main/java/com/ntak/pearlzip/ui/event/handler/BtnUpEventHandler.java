/*
 * Copyright © 2021 92AK
 */
package com.ntak.pearlzip.ui.event.handler;

import com.ntak.pearlzip.archive.pub.FileInfo;
import com.ntak.pearlzip.ui.model.FXArchiveInfo;
import javafx.collections.FXCollections;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseEvent;

import java.nio.file.Paths;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.ntak.pearlzip.ui.util.JFXUtil.isFileInArchiveLevel;

/**
 *  Event Handler for Parent Archive Directory Navigation functionality.
 *  @author Aashutos Kakshepati
*/
public class BtnUpEventHandler implements EventHandler<MouseEvent> {
    private final TableView<FileInfo> fileContentsView;
    private final FXArchiveInfo fxArchiveInfo;
    private final Button btnUp;

    public BtnUpEventHandler(TableView<FileInfo> fileContentsView, FXArchiveInfo fxArchiveInfo, Button btnUp) {
        this.fileContentsView = fileContentsView;
        this.fxArchiveInfo = fxArchiveInfo;
        this.btnUp = btnUp;
    }

    @Override
    public void handle(MouseEvent event) {
        if (fxArchiveInfo.getDepth().decrementAndGet() == 0) {
            btnUp.setVisible(false);
        }
        fxArchiveInfo.setPrefix(Optional.ofNullable(Paths.get(fxArchiveInfo.getPrefix()).getParent()).orElse(Paths.get("")).toString());
        fileContentsView.setItems(FXCollections.observableArrayList(fxArchiveInfo.getFiles()
                                                                            .stream()
                                                                            .filter(isFileInArchiveLevel(fxArchiveInfo))
                                                                            .collect(
                                                                                    Collectors.toList())));
        fileContentsView.refresh();
    }
}
