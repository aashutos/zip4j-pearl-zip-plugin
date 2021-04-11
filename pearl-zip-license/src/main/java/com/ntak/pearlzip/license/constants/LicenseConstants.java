/*
 * Copyright © 2021 92AK
 */
package com.ntak.pearlzip.license.constants;

import javax.xml.parsers.DocumentBuilderFactory;

/**
 *  Configuration and logging keys for use in the dependency license data delivery system.
 *  @author Aashutos Kakshepati
 */
public class LicenseConstants {
    public static final String CNS_LICENSE_LOCATION = "configuration.ntak.pearl-zip.license-location";
    public static final DocumentBuilderFactory DOCUMENT_BUILDER_FACTORY = DocumentBuilderFactory.newInstance();
    public static final String CNS_LICENSE_OVERRIDE_LOCATION = "configuration.ntak.pearl-zip.license-override-location";

    public static final String TAG_DEPENDENCY = "dependency";
    public static final String TAG_GROUP_ID = "groupId";
    public static final String TAG_ARTIFACT_ID = "artifactId";
    public static final String TAG_VERSION = "version";
    public static final String TAG_LICENSE = "license";
    public static final String TAG_NAME = "name";
    public static final String TAG_URL = "url";
    public static final String TAG_FILE = "file";

    public static final String LOG_ISSUE_PARSE_LICENSE_FILE = "logging.com.ntak.pearl-zip.license.issue-parse-license-file";
    public static final String LOG_SKIPPING_DEPENDENCY_LICENSE_RETRIEVAL = "logging.com.ntak.pearl-zip.license.skipping-dependency-license-retrieval";
    public static final String LOG_ADDED_LICENSE_DECLARATION = "logging.com.ntak.pearl-zip.license.added-license-declaration";
}
