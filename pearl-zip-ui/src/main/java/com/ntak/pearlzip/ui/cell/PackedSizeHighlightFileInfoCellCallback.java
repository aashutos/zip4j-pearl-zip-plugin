/*
 * Copyright (c) ${YEAR} 92AK
 */
package com.ntak.pearlzip.ui.cell;

import com.ntak.pearlzip.archive.pub.FileInfo;
import javafx.scene.control.TableCell;

/**
 *  Implementation of cell renderer for the Packed Size field.
 *  @author Aashutos Kakshepati
*/
public class PackedSizeHighlightFileInfoCellCallback extends AbstractHighlightFileInfoCellCallback {
    @Override
    public void setField(TableCell<FileInfo,FileInfo> cell, FileInfo info) {
        long packedSize = info.getPackedSize();
        cell.setText(String.format("%d", packedSize));
    }
}
