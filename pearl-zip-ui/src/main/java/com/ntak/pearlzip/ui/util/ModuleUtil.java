/*
 * Copyright © 2021 92AK
 */
package com.ntak.pearlzip.ui.util;

import com.ntak.pearlzip.archive.pub.ArchiveReadService;
import com.ntak.pearlzip.archive.pub.ArchiveService;
import com.ntak.pearlzip.archive.pub.ArchiveWriteService;
import com.ntak.pearlzip.ui.model.ZipState;

import java.lang.module.Configuration;
import java.lang.module.ModuleFinder;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.List;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

import static com.ntak.pearlzip.archive.util.LoggingUtil.resolveTextKey;
import static com.ntak.pearlzip.ui.constants.ZipConstants.*;

public class ModuleUtil {
    public static void loadModulesStatic() {
    ServiceLoader<ArchiveWriteService> serviceWriteLoader = ServiceLoader.load(ArchiveWriteService.class);
    ServiceLoader<ArchiveReadService> serviceReadLoader = ServiceLoader.load(ArchiveReadService.class);

    // LOG: ArchiveReadService implementation identified: %s
    ROOT_LOGGER.info(resolveTextKey(LOG_READ_SERVICES_IDENTIFIED,
                                    serviceReadLoader.stream().collect(Collectors.toList())));

    // LOG: ArchiveWriteService implementation identified: %s
    ROOT_LOGGER.info(resolveTextKey(LOG_WRITE_SERVICES_IDENTIFIED,
                                    serviceWriteLoader.stream().collect(Collectors.toList())));

    // Load Archive Services
    serviceReadLoader.stream()
                     .map(ServiceLoader.Provider::get)
                     .filter(ArchiveService::isEnabled)
                     .forEach(ZipState::addArchiveProvider);

    serviceWriteLoader.stream()
                      .map(ServiceLoader.Provider::get)
                      .filter(ArchiveService::isEnabled)
                      .forEach(ZipState::addArchiveProvider);
}

    public static void loadModulesDynamic(Path modulePath, URLClassLoader urlClassLoader) {
        ModuleFinder moduleFinder = ModuleFinder.of(modulePath);
        Configuration moduleConfig = Configuration.resolveAndBind(moduleFinder,
                                                                  List.of(ModuleLayer.boot()
                                                                                     .configuration()),
                                                                  moduleFinder,
                                                                  moduleFinder.findAll()
                                                                              .stream()
                                                                              .map(m -> m.descriptor()
                                                                                         .name())
                                                                              .collect(
                                                                                      Collectors.toSet()));

        ModuleLayer moduleLayer =
                ModuleLayer.defineModulesWithOneLoader(moduleConfig, List.of(ModuleLayer.boot()), urlClassLoader).layer();
        ServiceLoader<ArchiveWriteService> serviceWriteLoader = ServiceLoader.load(moduleLayer,
                                                                                   ArchiveWriteService.class);
        ServiceLoader<ArchiveReadService> serviceReadLoader = ServiceLoader.load(moduleLayer, ArchiveReadService.class);

        // LOG: ArchiveReadService implementation identified: %s
        ROOT_LOGGER.info(resolveTextKey(LOG_READ_SERVICES_IDENTIFIED,
                                        serviceReadLoader.stream().collect(Collectors.toList())));

        // LOG: ArchiveWriteService implementation identified: %s
        ROOT_LOGGER.info(resolveTextKey(LOG_WRITE_SERVICES_IDENTIFIED,
                                        serviceWriteLoader.stream().collect(Collectors.toList())));

        // Load Archive Services
        serviceReadLoader.stream()
                         .map(ServiceLoader.Provider::get)
                         .filter(ArchiveService::isEnabled)
                         .forEach(ZipState::addArchiveProvider);

        serviceWriteLoader.stream()
                          .map(ServiceLoader.Provider::get)
                          .filter(ArchiveService::isEnabled)
                          .forEach(ZipState::addArchiveProvider);
    }
}
