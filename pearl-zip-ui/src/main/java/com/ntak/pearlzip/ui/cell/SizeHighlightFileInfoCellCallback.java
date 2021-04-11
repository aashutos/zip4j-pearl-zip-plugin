/*
 * Copyright © 2021 92AK
 */
package com.ntak.pearlzip.ui.cell;

import com.ntak.pearlzip.archive.pub.FileInfo;
import javafx.scene.control.TableCell;

/**
 *  Implementation of cell renderer for the Raw Size field.
 *  @author Aashutos Kakshepati
*/
public class SizeHighlightFileInfoCellCallback extends AbstractHighlightFileInfoCellCallback {
    @Override
    public void setField(TableCell<FileInfo,FileInfo> cell, FileInfo info) {
        long rawSize = info.getRawSize();
        cell.setText(String.format("%d", rawSize));
    }
}
