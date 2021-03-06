/*
 * Copyright (c) ${YEAR} 92AK
 */
package com.ntak.pearlzip.ui.cell;

import com.ntak.pearlzip.archive.pub.FileInfo;
import javafx.scene.control.TableCell;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 *  Implementation of cell renderer for the Created field.
 *  @author Aashutos Kakshepati
*/
public class CreatedHighlightFileInfoCellCallback extends AbstractHighlightFileInfoCellCallback {
    @Override
    public void setField(TableCell<FileInfo,FileInfo> cell, FileInfo info) {
        LocalDateTime localDateTime = info.getCreationTime();

        if (Objects.nonNull(localDateTime)) {
            cell.setText(localDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        }
    }
}
