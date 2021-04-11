/*
 * Copyright © 2021 92AK
 */
package com.ntak.pearlzip.license.pub;

import com.ntak.pearlzip.license.model.LicenseInfo;

import java.util.Map;

/**
 *  Generic service, which provides functionality to derive and supply dependency license information.
 *  @author Aashutos Kakshepati
 */
public interface LicenseService {
    Map<String,LicenseInfo> retrieveDeclaredLicenses();
}
