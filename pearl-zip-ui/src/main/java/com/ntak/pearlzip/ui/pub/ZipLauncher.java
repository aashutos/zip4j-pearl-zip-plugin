/*
 * Copyright (c) ${YEAR} 92AK
 */
package com.ntak.pearlzip.ui.pub;

import com.ntak.pearlzip.archive.constants.ConfigurationConstants;
import com.ntak.pearlzip.archive.pub.ArchiveReadService;
import com.ntak.pearlzip.archive.pub.ArchiveService;
import com.ntak.pearlzip.archive.pub.ArchiveWriteService;
import com.ntak.pearlzip.archive.util.LoggingUtil;
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
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.MenuBar;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.Configurator;

import java.awt.*;
import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;

import static com.ntak.pearlzip.archive.constants.ArchiveConstants.EVENTBUS_EXECUTOR_SERVICE;
import static com.ntak.pearlzip.archive.constants.ConfigurationConstants.CNS_RES_BUNDLE;
import static com.ntak.pearlzip.archive.constants.LoggingConstants.LOG_BUNDLE;
import static com.ntak.pearlzip.archive.util.LoggingUtil.genLocale;
import static com.ntak.pearlzip.archive.util.LoggingUtil.resolveTextKey;
import static com.ntak.pearlzip.ui.constants.ZipConstants.*;
import static com.ntak.pearlzip.ui.util.ArchiveUtil.addToRecentFile;
import static com.ntak.pearlzip.ui.util.ArchiveUtil.launchMainStage;

/**
 *  Loads the main UI screen for the Zip Application.
 *  @author Aashutos Kakshepati
*/
public class ZipLauncher extends Application {

    private static MenuToolkit MENU_TOOLKIT;

    static {
        if (Desktop.getDesktop().isSupported(Desktop.Action.APP_OPEN_FILE)) {
            Desktop.getDesktop().setOpenFileHandler((e)->{
                e.getFiles()
                 .stream()
                 .peek(ROOT_LOGGER::info)
                 .map(f -> f.toPath()
                            .toAbsolutePath()
                            .toString())
                 .forEach(f -> {
                     Stage stage = new Stage();
                     ArchiveReadService readService = ZipState.getReadArchiveServiceForFile(f).get();
                     ArchiveWriteService writeService = ZipState.getWriteArchiveServiceForFile(f).orElse(null);
                     final FXArchiveInfo fxArchiveInfo = new FXArchiveInfo(f, readService, writeService);
                     Platform.runLater(()->launchMainStage(stage, fxArchiveInfo));
                 });
                e.getFiles()
                 .stream()
                 .map(f -> f.toPath()
                            .toAbsolutePath()
                            .toString())
                 .forEach(l -> {
                     try {
                         Files.writeString(Paths.get("~", "tmp"), l, StandardOpenOption.APPEND);
                     } catch(IOException ioException) {
                     }
                 });
            });
        }
    }

    @Override
    public void start(Stage stage) throws IOException {
        APP = this;

        CountDownLatch readyLatch = new CountDownLatch(1);

        ZipConstants.MESSAGE_TRACE_LOGGER = ProgressMessageTraceLogger.getMessageTraceLogger();

        ////////////////////////////////////////////
        ///// Create files and dir structure //////
        //////////////////////////////////////////

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
        readyLatch.countDown();
        FXArchiveInfo fxArchiveInfo;
        String archivePath;
        if (APP.getParameters().getRaw().size() > 0 && Files.exists(Paths.get(APP.getParameters()
                                                                                 .getRaw()
                                                                                 .get(0)))) {
            archivePath = APP.getParameters()
                                    .getRaw()
                                    .get(0);
            addToRecentFile(new File(archivePath));
        } else if (APP.getParameters().getRaw().size() > 0 && APP.getParameters().getRaw().get(0).startsWith("-psn_")) {
            // LOG: OS Trigger detected...
            ROOT_LOGGER.info(resolveTextKey(LOG_OS_TRIGGER_DETECTED));
            return;
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

    public static void main(String[] args) {
        try {
            ROOT_LOGGER.debug(Arrays.toString(args));

            // Load bootstrap properties
            Properties props = new Properties();
            props.load(ZipLauncher.class.getClassLoader()
                                        .getResourceAsStream("application.properties"));
            ZipConstants.STORE_ROOT = Paths.get(System.getProperty(CNS_STORE_ROOT, String.format("%s/.pz",
                                                                                                 System.getProperty(
                                                                                                         "user.home"))));
            ZipConstants.LOCAL_TEMP =
                    Paths.get(Optional.ofNullable(System.getenv("TMPDIR"))
                                      .orElse(STORE_ROOT.toString()));
            Path externalBootstrapFile = Paths.get(STORE_ROOT.toString(), "application.properties");

            // Overwrite with external properties file
            // Reserved properties are kept as per internal key definition
            Map<String,String> reservedKeyMap = new HashMap<>();
            Path tmpRK = Paths.get(STORE_ROOT.toString(), "rk");
            try (FileOutputStream fileOutputStream = new FileOutputStream(tmpRK.toString());
                    FileChannel channel = fileOutputStream.getChannel();
                    FileLock lock = channel.lock()) {
                    // Standard resource case
                    Path reservedKeys = Paths.get(ZipLauncher.class.getClassLoader()
                                                               .getResource("reserved-keys")
                                                               .getPath());
                    ROOT_LOGGER.info(reservedKeys);

                    // standard/jar resource case
                    if (Objects.nonNull(reservedKeys)) {

                        try (InputStream is = ZipLauncher.class.getClassLoader()
                                                               .getResourceAsStream("reserved-keys")) {
                            Files.copy(is, tmpRK, StandardCopyOption.REPLACE_EXISTING);
                        }
                    } else if (!Files.exists(reservedKeys)) {
                        reservedKeys = JRT_FILE_SYSTEM.getPath("modules", "com.ntak.pearlzip.ui", "reserved-keys");
                        Files.copy(reservedKeys, tmpRK, StandardCopyOption.REPLACE_EXISTING);
                    }

                    Files.lines(tmpRK)
                     .filter(k -> Objects.nonNull(k) && Objects.nonNull(props.getProperty(k)))
                     // LOG: Locking in key: %s with value: %s
                     .peek(k -> ROOT_LOGGER.info(resolveTextKey(LOG_LOCKING_IN_PROPERTY, k, props.getProperty(k))))
                     .forEach(k -> reservedKeyMap.put(k, props.getProperty(k)));
            }

            if (Files.exists(externalBootstrapFile)) {
                props.load(Files.newBufferedReader(externalBootstrapFile));
            }

            props.putAll(System.getProperties());
            props.putAll(reservedKeyMap);
            System.setProperties(props);
            ROOT_LOGGER.info(props);

            ////////////////////////////////////////////
            ///// Log4j Setup /////////////////////////
            //////////////////////////////////////////

            // Create root store
            Files.createDirectories(ZipConstants.STORE_ROOT);

            // Log4j configuration - handle fixed parameters when creating application image
            String log4jCfg = Paths.get(STORE_ROOT.toString(), "log4j2.xml")
                                   .toString();
            final Path log4jPath = Paths.get(log4jCfg);
            if (!Files.exists(log4jPath)) {
                Path logCfgFile = JRT_FILE_SYSTEM.getPath("modules", "com.ntak.pearlzip.archive", "log4j2.xml");
                try(InputStream is = Files.newInputStream(logCfgFile)) {
                    Files.copy(is, log4jPath);
                } catch(Exception e) {

                }
            }
            ConfigurationSource source = new ConfigurationSource(new FileInputStream(log4jCfg));
            Configurator.initialize(null, source);

            // Setting Locale
            Locale.setDefault(genLocale(props));
            ResourceBundle.getBundle(System.getProperty(ConfigurationConstants.CNS_CUSTOM_RES_BUNDLE, "custom"),
                                     Locale.getDefault());
            LOG_BUNDLE = ResourceBundle.getBundle(System.getProperty(CNS_RES_BUNDLE, "pearlzip"),
                                                  Locale.getDefault());
            MENU_TOOLKIT = MenuToolkit.toolkit(Locale.getDefault());

            // Load License Declarations
            LicenseService licenseService = new PearlZipLicenseService();
            licenseService.retrieveDeclaredLicenses()
                          .forEach(ZipState::addLicenseDeclaration);

            // Load Archive Services
            ServiceLoader<ArchiveReadService> serviceReadLoader = ServiceLoader.load(ArchiveReadService.class);
            serviceReadLoader.stream()
                             .map(ServiceLoader.Provider::get)
                             .filter(ArchiveService::isEnabled)
                             .forEach(ZipState::addArchiveProvider);

            ServiceLoader<ArchiveWriteService> serviceWriteLoader = ServiceLoader.load(ArchiveWriteService.class);
            serviceWriteLoader.stream()
                              .map(ServiceLoader.Provider::get)
                              .filter(ArchiveService::isEnabled)
                              .forEach(ZipState::addArchiveProvider);

            // Initialising Thread Pool
            String klassName;
            MetricProfile profile = MetricProfile.getDefaultProfile();
            if (Objects.nonNull(klassName = System.getProperty(CNS_METRIC_FACTORY))) {
                try {
                    MetricProfileFactory factory = (MetricProfileFactory) Class.forName(klassName)
                                                                               .getDeclaredConstructor()
                                                                               .newInstance();
                    profile = factory.getProfile();
                } catch(Exception e) {

                }
                PRIMARY_EXECUTOR_SERVICE =
                        Executors.newScheduledThreadPool(Math.max(Integer.parseInt(System.getProperty(
                                CNS_THREAD_POOL_SIZE,
                                "4")), 1),
                                                         MetricThreadFactory.create(profile));
            } else {
                PRIMARY_EXECUTOR_SERVICE =
                        Executors.newScheduledThreadPool(Math.max(Integer.parseInt(System.getProperty(
                                CNS_THREAD_POOL_SIZE,
                                "4")), 1),
                                                         MetricThreadFactory.create(MetricProfile.getDefaultProfile()));
            }

            launch(args);
        } catch(Exception e) {
            ROOT_LOGGER.error(e.getMessage());
            ROOT_LOGGER.error(LoggingUtil.getStackTraceFromException(e));
        }
    }
}
