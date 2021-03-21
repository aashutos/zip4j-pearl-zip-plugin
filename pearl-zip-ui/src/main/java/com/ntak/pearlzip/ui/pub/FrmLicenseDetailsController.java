/*
 * Copyright (c) ${YEAR} 92AK
 */
package com.ntak.pearlzip.ui.pub;

import javafx.fxml.FXML;
import javafx.scene.web.WebView;

/**
 *  Controller for the License Details dialog.
 *  @author Aashutos Kakshepati
*/
public class FrmLicenseDetailsController {

    @FXML
    private WebView webLicense;

    public void initData(String data) {
        try {
            webLicense.setContextMenuEnabled(false);
            webLicense.getEngine()
                      .loadContent(data);
        } catch (Exception e) {

        }
    }
}
