/*
 * Copyright © 2021 92AK
 */
package com.ntak.pearlzip.ui.util;

import com.ntak.pearlzip.ui.model.FXArchiveInfo;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Window;

/**
 *  AlertException is a checked exception, which is utilised by PearlZip to raise an alert dialog as necessary.
 *
 *  @author Aashutos Kakshepati
 */
public class AlertException extends Exception {

    private final Alert.AlertType type;
    private final String title;
    private final String header;
    private final String body;
    private final Window stage;
    private final ButtonType[] buttons;
    private final FXArchiveInfo archiveInfo;

    public AlertException(FXArchiveInfo archiveInfo, String message, Alert.AlertType type, String title, String header,
            String body,
            Exception nested, Window stage, ButtonType... buttons) {
        super(message, nested);

        this.archiveInfo = archiveInfo;
        this.type = type;
        this.title = title;
        this.header = header;
        this.body = body;
        this.stage = stage;
        this.buttons = buttons;
    }

    public FXArchiveInfo getArchiveInfo() {
        return archiveInfo;
    }

    public Alert.AlertType getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    public String getHeader() {
        return header;
    }

    public String getBody() {
        return body;
    }

    public Window getStage() {
        return stage;
    }

    public ButtonType[] getButtons() {
        return buttons;
    }
}
