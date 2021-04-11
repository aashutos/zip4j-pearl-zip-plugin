/*
 * Copyright © 2021 92AK
 */
package com.ntak.pearlzip.archive.constants;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 *  Logging keys for Resource Bundles.
 *  @author Aashutos Kakshepati
 */
public class LoggingConstants {

    public static final String LOG_ARCHIVE_SERVICE_FORMAT = "logging.ntak.pearl-zip.archive-service.format";
    public static final String LOG_ARCHIVE_SERVICE_NUMBER_ITEMS = "logging.ntak.pearl-zip.archive-service.no-items";
    public static final String LOG_ARCHIVE_READ_ZIP_PROPERTY = "logging.ntak.pearl-zip.archive-service.read-zip-property";
    public static final String LOG_ARCHIVE_SERVICE_ZIP_ENTRY = "logging.ntak.pearl-zip.archive-service.read-zip-entry";

    public static final String LOG_ARCHIVE_SERVICE_LISTING_EXCEPTION = "logging.ntak.pearl-zip.archive-service.listing.exception";
    public static final String LOG_ARCHIVE_SERVICE_CREATE_EXCEPTION = "logging.ntak.pearl-zip.archive-service.create.exception";
    public static final String LOG_ARCHIVE_SERVICE_ADD_EXCEPTION = "logging.ntak.pearl-zip.archive-service.add.exception";
    public static final String LOG_TRANSFORM_EXCEPTION = "logging.ntak.pearl-zip.transform.exception";
    public static final String LOG_SKIP_SYMLINK = "logging.ntak.pearl-zip.skip-symlink";

    public static final String LOG_ARCHIVE_INFO_ASSERT_PATH = "logging.ntak.pearl-zip.archive-info.assert.path";
    public static final String LOG_ARCHIVE_INFO_ASSERT_READ_SERVICE = "logging.ntak.pearl-zip.archive-info.assert.read-service";

    public static final String LBL_PROGRESS_CLEAR_UP = "label.ntak.pearl-zip.progress.clear-up";
    public static final String LBL_PROGRESS_DELETED_ENTRIES = "label.ntak.pearl-zip.progress.deleted-entries";
    public static final String LBL_PROGRESS_DELETING_ENTRIES = "label.ntak.pearl-zip.progress.deleting-entries";
    public static final String LBL_PROGRESS_LOADED_ENTRY = "label.ntak.pearl-zip.progress.loaded-entry";
    public static final String LBL_PROGRESS_EXTRACT_ENTRY = "label.ntak.pearl-zip.progress.extract-entry";
    public static final String LBL_PROGRESS_COMPLETION = "label.ntak.pearl-zip.progress.completion";
    public static final String LBL_PROGRESS_LOADING = "label.ntak.pearl-zip.progress.loading";

    public static ResourceBundle CUSTOM_BUNDLE =
            ResourceBundle.getBundle(System.getProperty(ConfigurationConstants.CNS_CUSTOM_RES_BUNDLE, "custom"),
                                                                       Locale.getDefault());
    public static ResourceBundle LOG_BUNDLE =
            ResourceBundle.getBundle(System.getProperty(ConfigurationConstants.CNS_RES_BUNDLE, "pearlzip"),
                                                                       Locale.getDefault());

    // QUEUE KEYS
    public static final String PROGRESS = "PROGRESS";
    public static final String COMPLETED = "COMPLETED";
}
