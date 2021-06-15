/*
 * Copyright © 2021 92AK
 */
package com.ntak.pearlzip;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.SuiteDisplayName;
import org.junit.runner.RunWith;

/***
 *   @author Aashutos Kakshepati
 */
@RunWith(JUnitPlatform.class)
@SelectPackages("com.ntak.pearlzip")
@SuiteDisplayName("PearlZip common archive module tests")
public class ArchiveTestSuite {
}
