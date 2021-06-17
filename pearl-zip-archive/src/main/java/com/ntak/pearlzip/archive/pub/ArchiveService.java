/*
 * Copyright © 2021 92AK
 */
package com.ntak.pearlzip.archive.pub;

import java.util.Set;

import static com.ntak.pearlzip.archive.constants.ConfigurationConstants.CNS_COM_BUS_FACTORY;
import static com.ntak.pearlzip.archive.constants.LoggingConstants.LOG_ARCHIVE_SERVICE_COM_BUS_INIT_ERROR;
import static com.ntak.pearlzip.archive.constants.LoggingConstants.ROOT_LOGGER;
import static com.ntak.pearlzip.archive.util.LoggingUtil.resolveTextKey;

/**
 *  Interface defining common functionality associated with an archive extracting/compression implementation.
 *  @author Aashutos Kakshepati
 */
public interface ArchiveService {

    CommunicationBus DEFAULT_BUS = initializeBus();

    static CommunicationBus initializeBus() {
        try {
            CommunicationBusFactory factory = (CommunicationBusFactory) Class.forName(
                                                System.getProperty(CNS_COM_BUS_FACTORY,
                                                                  "com.ntak.pearlzip.ui.util.EventBusFactory")
            )
            .getDeclaredConstructor()
            .newInstance();

            return factory.initializeCommunicationBus();
        } catch (Exception e) {
            // LOG: Exception raised on initialisation of Communication Bus. A critical issue has occurred, Pearl Zip
            // will now close.\n
            // Exception Type: %s\n
            // Exception message: %s\n
            // Stack trace:\n%s
            ROOT_LOGGER.error(resolveTextKey(LOG_ARCHIVE_SERVICE_COM_BUS_INIT_ERROR,
                                             e.getClass().getCanonicalName(), e.getMessage(), e.getStackTrace()));
            throw new ExceptionInInitializerError(resolveTextKey(LOG_ARCHIVE_SERVICE_COM_BUS_INIT_ERROR,
                                                                 e.getClass().getCanonicalName(), e.getMessage(), e.getStackTrace()));
        }
    }

    default boolean isEnabled() {
        return Boolean.parseBoolean(System.getProperty(
                String.format("configuration.ntak.pearl-zip.provider.priority.enabled.%s",
                               getClass().getCanonicalName()
                ),
                "true")
        );
    }

    default Set<String> getCompressorArchives() {
        return Set.of("gz", "xz", "bz2", "lz", "lz4", "lzma", "z", "sz");
    }
}
