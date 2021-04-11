/*
 * Copyright © 2021 92AK
 */
package com.ntak.pearlzip.ui.cell;

import com.ntak.pearlzip.archive.pub.FileInfo;
import javafx.scene.control.TableCell;

/**
 *  Implementation of cell renderer for the Comments field.
 *  @author Aashutos Kakshepati
*/
public class CommentsHighlightFileInfoCellCallback extends AbstractHighlightFileInfoCellCallback {
    @Override
    public void setField(TableCell<FileInfo,FileInfo> cell, FileInfo info) {
        String comments = info.getComments();
        cell.setText(comments);
    }
}
