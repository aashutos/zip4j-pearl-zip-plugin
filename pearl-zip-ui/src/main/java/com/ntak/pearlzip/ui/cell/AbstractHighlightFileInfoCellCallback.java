/*
 *  Copyright (c) 2021 92AK
 */
package com.ntak.pearlzip.ui.cell;

import com.ntak.pearlzip.archive.pub.FileInfo;
import com.ntak.pearlzip.ui.model.FXArchiveInfo;
import com.ntak.pearlzip.ui.model.FXMigrationInfo;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;

import static com.ntak.pearlzip.ui.constants.ResourceConstants.DEFAULT_HIGHLIGHT;
import static com.ntak.pearlzip.ui.util.JFXUtil.highlightCellIfMatch;

/**
 *  Abstract root class used by the file list to render the cells in a row entry.
 *  @author Aashutos Kakshepati
*/
public abstract class AbstractHighlightFileInfoCellCallback implements Callback<TableColumn<FileInfo,FileInfo>,TableCell<FileInfo,
        FileInfo>> {

    @Override
    public TableCell<FileInfo,FileInfo> call(TableColumn<FileInfo,FileInfo> param) {
        TableCell<FileInfo,FileInfo> cell = new TableCell<>() {
            @Override
            public void updateItem(FileInfo item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                        // Set value
                        setField(this, item);

                        // Highlight logic
                        if (getTableView().getScene().getWindow().getUserData() instanceof FXArchiveInfo) {
                            FXArchiveInfo fxArchiveInfo =
                                    (FXArchiveInfo) getTableView().getScene().getWindow().getUserData();
                            if (fxArchiveInfo.getMigrationInfo().getType() != FXMigrationInfo.MigrationType.NONE) {
                                final FileInfo rootMigrationFile = fxArchiveInfo.getMigrationInfo()
                                                                   .getFile();
                                highlightCellIfMatch(this, item, rootMigrationFile, DEFAULT_HIGHLIGHT);
                            }
                        }
                }
            }
        };
        return cell;
    }

    public abstract void setField(TableCell<FileInfo,FileInfo> cell, FileInfo fileInfo);
}
