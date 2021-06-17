/*
 * Copyright © 2021 92AK
 */
package com.ntak.pearlzip.archive.szjb.pub;

import static com.ntak.pearlzip.archive.constants.ConfigurationConstants.CNS_COM_BUS_FACTORY;

public class SevenZipArchiveServiceTest extends SevenZipArchiveServiceTestCore {
    public SevenZipArchiveServiceTest() {
        System.setProperty(CNS_COM_BUS_FACTORY, "com.ntak.testfx.MockCommunicationBusFactory");
    }
}
