/*
 * Copyright © 2021 92AK
 */
package com.ntak.pearlzip.archive.pub;

import javafx.scene.Node;
import javafx.util.Pair;

import java.util.Optional;
import java.util.ResourceBundle;
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

    static ArchiveInfo generateDefaultArchiveInfo(String archivePath) {
        ArchiveInfo archiveInfo = new ArchiveInfo();

        archiveInfo.setArchivePath(archivePath);
        archiveInfo.setArchiveFormat(archivePath.substring(archivePath.lastIndexOf(".") + 1));
        archiveInfo.setCompressionLevel(9);

        return archiveInfo;
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

    /**
     *   Declares a set of file extensions, which are alias of core formats. This list of formats will not be used in
     *   the creation of archives. It is anticipated that this field will contain shortened convenience extensions in
     *   which long explicit extensions would be preferable (e.g. tar.gz would be preferred to tgz). The shortened
     *   format can still be read and modified subject to the underlying {@link ArchiveService} implementation.
     *
     *   @return Set&lt;String&gt; - Set of alias file extensions
     */
    default Set<String> getAliasFormats() { return Set.of("tgz"); }

    default Optional<Pair<String,Node>> getOptionsPane() { return Optional.empty(); }

    default Optional<ResourceBundle> getResourceBundle() { return Optional.empty(); }
}
