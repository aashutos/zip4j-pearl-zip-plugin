/*
 * Copyright © 2021 92AK
 */
package com.ntak.pearlzip.archive.acc;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.SuiteDisplayName;
import org.junit.runner.RunWith;

/***
 *   @author akakshepati
 */
@RunWith(JUnitPlatform.class)
@SelectPackages("com.ntak.pearlzip")
@SuiteDisplayName("PearlZip Apache Commons Compress archive module tests")
public class ApacheCommonsCompressTestSuite {
}