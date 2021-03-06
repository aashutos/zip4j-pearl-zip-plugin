/*
 *  Copyright (c) 2021 92AK
 */
package com.ntak.pearlzip.ui.pub;

import com.ntak.pearlzip.archive.constants.ConfigurationConstants;
import com.ntak.pearlzip.archive.pub.ArchiveReadService;
import com.ntak.pearlzip.archive.pub.ArchiveWriteService;
import com.ntak.pearlzip.license.pub.LicenseService;
import com.ntak.pearlzip.license.pub.PearlZipLicenseService;
import com.ntak.pearlzip.ui.constants.ZipConstants;
import com.ntak.pearlzip.ui.model.FXArchiveInfo;
import com.ntak.pearlzip.ui.model.ZipState;
import com.ntak.pearlzip.ui.util.MetricProfile;
import com.ntak.pearlzip.ui.util.MetricProfileFactory;
import com.ntak.pearlzip.ui.util.MetricThreadFactory;
import com.ntak.pearlzip.ui.util.ProgressMessageTraceLogger;
import de.jangassen.MenuToolkit;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.MenuBar;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.Executors;

import static com.ntak.pearlzip.archive.constants.ArchiveConstants.EVENTBUS_EXECUTOR_SERVICE;
import static com.ntak.pearlzip.archive.constants.ConfigurationConstants.CNS_RES_BUNDLE;
import static com.ntak.pearlzip.archive.constants.LoggingConstants.LOG_BUNDLE;
import static com.ntak.pearlzip.archive.util.LoggingUtil.genLocale;
import static com.ntak.pearlzip.ui.constants.ZipConstants.*;
import static com.ntak.pearlzip.ui.util.ArchiveUtil.addToRecentFile;
import static com.ntak.pearlzip.ui.util.ArchiveUtil.launchMainStage;

/**
 *  Loads the main UI screen for the Zip Application.
 *  @author Aashutos Kakshepati
*/
public class ZipLauncher extends Application {
    private static MenuToolkit MENU_TOOLKIT;

    @Override
    public void start(Stage stage) throws IOException {
        APP = this;

        ZipConstants.MESSAGE_TRACE_LOGGER = ProgressMessageTraceLogger.getMessageTraceLogger();

        ////////////////////////////////////////////
        ///// Create files and dir structure //////
        //////////////////////////////////////////

        // Create root store
        ZipConstants.STORE_ROOT = Paths.get(System.getProperty(CNS_STORE_ROOT, String.format("%s/.pz",
                                                                                             System.getProperty("user.home"))));
        ZipConstants.LOCAL_TEMP =
                Paths.get(Optional.ofNullable(System.getenv("TMPDIR")).orElse(STORE_ROOT.toString()));

        // Create temporary store folder
        ZipConstants.STORE_TEMP = Paths.get(STORE_ROOT.toAbsolutePath().toString(), "temp");
        if (!Files.exists(STORE_TEMP)) {
            Files.createDirectories(STORE_TEMP);
        }

        // Providers
        Path providerPath = Paths.get(STORE_ROOT.toAbsolutePath().toString(), "providers");
        Files.createDirectories(providerPath);

        // Recent files
        RECENT_FILE = Paths.get(STORE_ROOT.toAbsolutePath().toString(), "rf");
        if (!Files.exists(RECENT_FILE)) {
            Files.createFile(RECENT_FILE);
        }

        ////////////////////////////////////////////
        ///// Create System Menu //////////////////
        //////////////////////////////////////////

        // Create a new System Menu
        // TODO: Set CFBundleName in Info.plist
        String appName = System.getProperty(CNS_NTAK_PEARL_ZIP_APP_NAME, "PearlZip");
        MenuBar sysMenu = new MenuBar();

        // Setting about form...
        FXMLLoader aboutLoader = new FXMLLoader();
        aboutLoader.setLocation(ZipLauncher.class.getClassLoader().getResource("frmAbout.fxml"));
        aboutLoader.setResources(LOG_BUNDLE);
        VBox abtRoot = aboutLoader.load();
        FrmAboutController abtController = aboutLoader.getController();
        Scene abtScene = new Scene(abtRoot);
        Stage aboutStage = new Stage();
        abtController.initData(aboutStage);
        aboutStage.setScene(abtScene);
        aboutStage.initStyle(StageStyle.UNDECORATED);

        sysMenu.setUseSystemMenuBar(true);
        sysMenu.getMenus().add(MENU_TOOLKIT.createDefaultApplicationMenu(appName, aboutStage));

        // Add some more Menus...
        FXMLLoader menuLoader = new FXMLLoader();
        menuLoader.setLocation(ZipLauncher.class.getClassLoader().getResource("sysmenu.fxml"));
        menuLoader.setResources(LOG_BUNDLE);
        MenuBar additionalMenu = menuLoader.load();
        SysMenuController menuController = menuLoader.getController();
        menuController.initData();
        sysMenu.getMenus().addAll(additionalMenu.getMenus());

        // Use the menu sysMenu for all stages including new ones
        MENU_TOOLKIT.setGlobalMenuBar(sysMenu);

        // Initialise archive information
        FXArchiveInfo fxArchiveInfo;
        String archivePath;
        if (APP.getParameters().getRaw().size() > 0) {
            archivePath = APP.getParameters()
                                    .getRaw()
                                    .get(0);
            addToRecentFile(new File(archivePath));
        } else {
            archivePath = Paths.get(STORE_TEMP.toString(),
                                    String.format("a%s.zip", System.currentTimeMillis())).toAbsolutePath().toString();
            ZipState.getWriteArchiveServiceForFile(archivePath).get().createArchive(System.currentTimeMillis(), archivePath);
        }

        ArchiveReadService readService = ZipState.getReadArchiveServiceForFile(archivePath).get();
        ArchiveWriteService writeService = ZipState.getWriteArchiveServiceForFile(archivePath).orElse(null);
                fxArchiveInfo = new FXArchiveInfo(archivePath,
                                                  readService, writeService);

        launchMainStage(stage, fxArchiveInfo);
    }

    @Override
    public void stop() {
        EVENTBUS_EXECUTOR_SERVICE.shutdown();
        PRIMARY_EXECUTOR_SERVICE.shutdown();
    }

    public static void main(String[] args) throws IOException {
        // Load bootstrap properties
        Properties props = new Properties();
        props.load(ZipLauncher.class.getClassLoader().getResourceAsStream("application.properties"));
        props.putAll(System.getProperties());
        System.setProperties(props);

        // Setting Locale
        Locale.setDefault(genLocale(props));
        ResourceBundle.getBundle(System.getProperty(ConfigurationConstants.CNS_CUSTOM_RES_BUNDLE, "custom"),
                                 Locale.getDefault());
        LOG_BUNDLE = ResourceBundle.getBundle(System.getProperty(CNS_RES_BUNDLE, "pearlzip"),
                                              Locale.getDefault());
        MENU_TOOLKIT = MenuToolkit.toolkit(Locale.getDefault());

        // Load License Declarations
        LicenseService licenseService = new PearlZipLicenseService();
        licenseService.retrieveDeclaredLicenses().forEach((k,v)->ZipState.addLicenseDeclaration(k, v));

        // Load Archive Services
        ServiceLoader<ArchiveReadService> serviceReadLoader = ServiceLoader.load(ArchiveReadService.class);
        serviceReadLoader.stream().map(p->p.get()).filter(p->p.isEnabled()).forEach(ZipState::addArchiveProvider);

        ServiceLoader<ArchiveWriteService> serviceWriteLoader = ServiceLoader.load(ArchiveWriteService.class);
        serviceWriteLoader.stream().map(p->p.get()).filter(p->p.isEnabled()).forEach(ZipState::addArchiveProvider);

        // Initialising Thread Pool
        String klassName;
        MetricProfile profile = MetricProfile.getDefaultProfile();
        if (Objects.nonNull(klassName = System.getProperty(CNS_METRIC_FACTORY))) {
            try {
                MetricProfileFactory factory = (MetricProfileFactory) Class.forName(klassName).getDeclaredConstructor().newInstance();
                profile = factory.getProfile();
            } catch(Exception e) {

            }
            PRIMARY_EXECUTOR_SERVICE =
                    Executors.newScheduledThreadPool(Math.max(Integer.parseInt(System.getProperty(CNS_THREAD_POOL_SIZE,
                                                                                                  "4")), 1),
                                                     MetricThreadFactory.create(profile));
        } else {
            PRIMARY_EXECUTOR_SERVICE =
                    Executors.newScheduledThreadPool(Math.max(Integer.parseInt(System.getProperty(CNS_THREAD_POOL_SIZE,
                                                                                                  "4")), 1),
                                                     MetricThreadFactory.create(MetricProfile.getDefaultProfile()));
        }
        launch(args);
    }
}
