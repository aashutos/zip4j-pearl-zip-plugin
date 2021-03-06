/*
 * Copyright (c) ${YEAR} 92AK
 */
package com.ntak.pearlzip.ui.cell;

import com.ntak.pearlzip.archive.pub.FileInfo;
import javafx.scene.control.TableCell;

/**
 *  Implementation of cell renderer for the Hash field.
 *  @author Aashutos Kakshepati
*/
public class HashHighlightFileInfoCellCallback extends AbstractHighlightFileInfoCellCallback {
    @Override
    public void setField(TableCell<FileInfo,FileInfo> cell, FileInfo info) {
        cell.setText(String.format("0x%s", Long.toHexString(info.getCrcHash()).toUpperCase()));
    }
}
