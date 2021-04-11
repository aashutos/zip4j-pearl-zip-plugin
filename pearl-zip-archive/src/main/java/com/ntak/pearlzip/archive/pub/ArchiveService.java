/*
 * Copyright © 2021 92AK
 */
package com.ntak.pearlzip.archive.pub;

import org.greenrobot.eventbus.EventBus;

import java.util.Set;

import static com.ntak.pearlzip.archive.constants.ArchiveConstants.EVENTBUS_EXECUTOR_SERVICE;

/**
 *  Interface defining common functionality associated with an archive extracting/compression implementation.
 *  @author Aashutos Kakshepati
 */
public interface ArchiveService {
    EventBus DEFAULT_BUS = EventBus.builder()
                                   .executorService(EVENTBUS_EXECUTOR_SERVICE)
                                   .build();

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
