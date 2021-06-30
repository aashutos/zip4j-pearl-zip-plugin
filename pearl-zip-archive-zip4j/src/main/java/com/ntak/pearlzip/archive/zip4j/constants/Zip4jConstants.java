/*
 * Copyright © 2021 92AK
 */

package com.ntak.pearlzip.archive.zip4j.constants;

import java.util.ResourceBundle;
import java.util.regex.Pattern;

/**
 *  Logging, Configuration keys and other constants that are used in the Zip4j archiving processes.
 *  @author Aashutos Kakshepati
 */
public class Zip4jConstants {

    /////////////////////////////
    ///// ArchiveInfo Keys //////
    /////////////////////////////

    public static final String KEY_COMPRESSION_METHOD = "COMPRESSION_METHOD";

    public static final String KEY_ENCRYPTION_ENABLE = "ENCRYPTION_ENABLE";
    public static final String KEY_ENCRYPTION_METHOD = "ENCRYPTION_METHOD";
    public static final String KEY_ENCRYPTION_STRENGTH = "ENCRYPTION_STRENGTH";

    public static final String KEY_ENCRYPTION_PW = "ENCRYPTION_PW";

    public static final String KEY_SPLIT_ARCHIVE_ENABLE = "SPLIT_ARCHIVE_ENABLE";
    public static final String KEY_SPLIT_ARCHIVE_SIZE = "SPLIT_ARCHIVE_SIZE";

    /////////////////////////////
    ///// Fixed values //////////
    /////////////////////////////

    public static final ResourceBundle RES_BUNDLE = ResourceBundle.getBundle("zip4j_plugin");
    public static final long MIN_SPLIT_ARCHIVE_SIZE = 65536;
    public static final String PATTERN_FOLDER = "%s/";
    public static final Pattern SSV = Pattern.compile(Pattern.quote("/"));

    // Logging keys...
    public static final String LOG_ARCHIVE_Z4J_ISSUE_CREATING_ARCHIVE = "logging.ntak.pearl-zip.zip4j.issue-creating-archive";
    public static final String LOG_ARCHIVE_Z4J_DELETING_FILE = "logging.ntak.pearl-zip.zip4j.deleting-file";
    public static final String LOG_ARCHIVE_Z4J_ISSUE_DELETING_FILE = "logging.ntak.pearl-zip.zip4j.issue-deleting-file";
    public static final String LOG_ARCHIVE_Z4J_ADDING_FILE = "logging.ntak.pearl-zip.zip4j.adding-file";
    public static final String LOG_ARCHIVE_Z4J_ISSUE_ADDING_FILE = "logging.ntak.pearl-zip.zip4j.issue-adding-file";
    public static final String LOG_ARCHIVE_Z4J_ISSUE_GENERATING_METADATA =
            "logging.ntak.pearl-zip.zip4j.issue-generating-metadata";
}
