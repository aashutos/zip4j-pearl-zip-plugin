/*
 * Copyright (c) ${YEAR} 92AK
 */
package com.ntak.pearlzip.ui.pub;

import javafx.fxml.FXML;
import javafx.scene.web.WebView;

import java.net.URL;

/**
 *  Controller for the License Details dialog.
 *  @author Aashutos Kakshepati
*/
public class FrmLicenseDetailsController {

    @FXML
    private WebView webLicense;

    public void initData(URL licenseDetailsFile) {
        webLicense.setContextMenuEnabled(false);
        webLicense.getEngine().load(licenseDetailsFile.toString());
    }
}
