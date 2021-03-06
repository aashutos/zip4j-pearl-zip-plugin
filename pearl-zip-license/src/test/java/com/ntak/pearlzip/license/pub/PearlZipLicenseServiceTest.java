/*
 * Copyright (c) ${YEAR} 92AK
 */
package com.ntak.pearlzip.license.pub;

import com.ntak.pearlzip.license.model.LicenseInfo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

public class PearlZipLicenseServiceTest {

    private PearlZipLicenseService licenseService = new PearlZipLicenseService();

    @BeforeAll
    public static void setup() {

    }

    @Test
    @DisplayName("Test: Successfully retrieve license descriptors for dependencies")
    public void testRetrieveLicense_ValidSetup_Success() {
        Map<String,LicenseInfo> licenses = licenseService.retrieveDeclaredLicenses();
        Assertions.assertNotNull(licenses, "Licenses was unexpectedly null");
    }
}
