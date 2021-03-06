/*
 * Copyright (c) ${YEAR} 92AK
 */
package com.ntak.pearlzip.ui.pub;

import com.ntak.pearlzip.archive.util.LoggingUtil;
import com.ntak.pearlzip.license.model.LicenseInfo;
import com.ntak.pearlzip.ui.model.ZipState;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;

import java.net.URL;
import java.util.Objects;

import static com.ntak.pearlzip.archive.constants.LoggingConstants.LOG_BUNDLE;
import static com.ntak.pearlzip.archive.util.LoggingUtil.resolveTextKey;
import static com.ntak.pearlzip.ui.constants.ZipConstants.*;
import static com.ntak.pearlzip.ui.util.JFXUtil.raiseAlert;

/**
 *  Controller for the License Overview dialog.
 *  @author Aashutos Kakshepati
*/
public class FrmLicenseOverviewController {

    private static final Logger LOGGER = LoggerContext.getContext().getLogger(FrmLicenseOverviewController.class);

    @FXML
    private TableView<LicenseInfo> tblLicenseInfo;
    @FXML
    private TableColumn<LicenseInfo,String> canonicalName;
    @FXML
    private TableColumn<LicenseInfo,String> version;
    @FXML
    private TableColumn<LicenseInfo,String> licenseType;

    @FXML
    private Button btnOk;

    @FXML
    public void initialize() {
        canonicalName.setCellValueFactory((s)->new SimpleStringProperty(s.getValue()
                                                                         .canonicalName()));
        version.setCellValueFactory((s)->new SimpleStringProperty(s.getValue()
                                                                         .version()));
        licenseType.setCellValueFactory((s)->new SimpleStringProperty(s.getValue()
                                                                         .licenseType()));
        tblLicenseInfo.setItems(FXCollections.observableArrayList(ZipState.getLicenseDeclarations()));
    }

    public void initData(Stage parentStage, Stage stage) {
        btnOk.setOnAction((e)->{
            stage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST));
            parentStage.setAlwaysOnTop(true);
        });

        tblLicenseInfo.setOnMouseClicked((e) ->{
            if (e.getButton() == MouseButton.PRIMARY && e.getClickCount() == 2) {
                try {
                    LicenseInfo info = tblLicenseInfo.getSelectionModel()
                                                     .getSelectedItem();
                    String fileName = info.licenseFile();
                    if (Objects.nonNull(fileName)) {
                        URL licenseDetailsFile = new URL(String.format("%s/%s", FrmLicenseOverviewController.class
                                                                                    .getClassLoader()
                                                                                    .getResource("ref")
                                                                                    .toString(),
                                                                                    fileName)
                        );
                        Stage licDetailsStage = new Stage();

                        FXMLLoader loader = new FXMLLoader();
                        loader.setLocation(ZipLauncher.class.getClassLoader()
                                                            .getResource("frmLicenseDetails.fxml"));
                        loader.setResources(LOG_BUNDLE);
                        AnchorPane root = loader.load();

                        FrmLicenseDetailsController controller = loader.getController();
                        controller.initData(licenseDetailsFile);

                        Scene scene = new Scene(root);
                        // License Details : %s
                        licDetailsStage.setTitle(resolveTextKey(TITLE_LICENSE_DETAILS, info.licenseFile()));
                        licDetailsStage.setScene(scene);
                        licDetailsStage.setResizable(false);

                        licDetailsStage.show();
                        licDetailsStage.setAlwaysOnTop(true);
                        licDetailsStage.setAlwaysOnTop(false);
                    }
                } catch (Exception exc) {
                    // LOG: Issue creating stage.\nException type: %s\nMessage:%s\nStack trace:\n%s
                    LOGGER.warn(resolveTextKey(LOG_ISSUE_CREATING_STAGE, exc.getClass().getCanonicalName(),
                                               exc.getMessage(),
                                               LoggingUtil.getStackTraceFromException(exc)));
                    // TITLE: ERROR: Issue creating stage
                    // HEADER: There was an issue creating the required dialog
                    // BODY: Upon initiating function '%s', an issue occurred on attempting to create the dialog. This
                    // function will not proceed any further.
                    raiseAlert(Alert.AlertType.ERROR, resolveTextKey(TITLE_ISSUE_CREATING_STAGE),
                               resolveTextKey(HEADER_ISSUE_CREATING_STAGE),
                               resolveTextKey(BODY_ISSUE_CREATING_STAGE, this.getClass().getName()), exc,
                               parentStage);
                }
            }
        });
    }

}
