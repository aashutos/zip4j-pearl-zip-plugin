/*
 * Copyright (c) ${YEAR} 92AK
 */
package com.ntak.pearlzip.ui.pub;

import com.ntak.pearlzip.archive.pub.FileInfo;
import com.ntak.pearlzip.ui.event.handler.*;
import com.ntak.pearlzip.ui.model.FXArchiveInfo;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableRow;
import javafx.stage.Stage;

import static com.ntak.pearlzip.archive.util.LoggingUtil.resolveTextKey;
import static com.ntak.pearlzip.ui.constants.ZipConstants.*;
import static com.ntak.pearlzip.ui.util.ArchiveUtil.openExternally;

/**
 *  Controller for the Context menu.
 *  @author Aashutos Kakshepati
*/
public class ContextMenuController {

    @FXML
    private ContextMenu ctxMenu;
    @FXML
    private MenuItem mnuOpen;
    @FXML
    private MenuItem mnuExtract;
    @FXML
    private MenuItem mnuCopy;
    @FXML
    private MenuItem mnuMove;
    @FXML
    private MenuItem mnuDelete;
    @FXML
    private MenuItem mnuFileInfo;

    public void initData(FXArchiveInfo archiveInfo, TableRow<FileInfo> row) {
        FileInfo fileInfo = row.getItem();
        ctxMenu.setOnShowing((e)->{
            switch(archiveInfo.getMigrationInfo().getType()) {
                case COPY:      mnuMove.setDisable(true);
                                mnuDelete.setDisable(true);
                                mnuMove.setText(resolveTextKey(LBL_BUTTON_MOVE));
                                mnuCopy.setText(resolveTextKey(LBL_BUTTON_PASTE));
                                break;
                case MOVE:      mnuCopy.setDisable(true);
                                mnuDelete.setDisable(true);
                                mnuCopy.setText(resolveTextKey(LBL_BUTTON_COPY));
                                mnuMove.setText(resolveTextKey(LBL_BUTTON_DROP));
                                break;
                case DELETE:    mnuMove.setDisable(true);
                                mnuCopy.setDisable(true);
                                mnuCopy.setText(resolveTextKey(LBL_BUTTON_COPY));
                                mnuMove.setText(resolveTextKey(LBL_BUTTON_MOVE));
                                break;
                default:        mnuMove.setDisable(false);
                                mnuCopy.setDisable(false);
                                mnuCopy.setText(resolveTextKey(LBL_BUTTON_COPY));
                                mnuMove.setText(resolveTextKey(LBL_BUTTON_MOVE));
            }
        });
        FrmMainController controller;
        if (archiveInfo.getController().isPresent()) {
            controller = archiveInfo.getController().get();
            mnuMove.setOnAction((e)->Platform.runLater(()->new BtnMoveSelectedEventHandler(row.getTableView(),
                                                                               controller.getBtnCopy(),
                                                                               controller.getBtnMove(),
                                                                               controller.getBtnDelete(),
                                                                               archiveInfo).handle(e))
            );
            mnuCopy.setOnAction((e)->Platform.runLater(()->new BtnCopySelectedEventHandler(row.getTableView(),
                                                                              controller.getBtnCopy(),
                                                                     controller.getBtnMove(),
                                                                     controller.getBtnDelete(),
                                                                     archiveInfo).handle(e))
            );
            mnuDelete.setOnAction((e)->Platform.runLater(()->new BtnDeleteEventHandler(row.getTableView(),
                                                                                      archiveInfo).handle(null)));
        } else {
            mnuCopy.setDisable(true);
            mnuMove.setDisable(true);
            mnuDelete.setDisable(true);
        }
        mnuOpen.setOnAction((e)->openExternally(System.currentTimeMillis(), (Stage) row.getScene().getWindow(), archiveInfo,
                                                 fileInfo));
        mnuExtract.setOnAction((e)->new BtnExtractFileEventHandler(row.getTableView(), archiveInfo).handle(e));
        mnuFileInfo.setOnAction((e)->new BtnFileInfoEventHandler(row.getTableView(),archiveInfo).handle(null));
    }
}
