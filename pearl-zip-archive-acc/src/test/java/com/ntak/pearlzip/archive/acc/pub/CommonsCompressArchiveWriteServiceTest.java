/*
 * Copyright © 2021 92AK
 */
package com.ntak.pearlzip.archive.acc.pub;

import static com.ntak.pearlzip.archive.constants.ConfigurationConstants.CNS_COM_BUS_FACTORY;

public class CommonsCompressArchiveWriteServiceTest extends CommonsCompressArchiveWriteServiceTestCore {
    public CommonsCompressArchiveWriteServiceTest() {
        System.setProperty(CNS_COM_BUS_FACTORY, "com.ntak.testfx.MockCommunicationBusFactory");
    }
}
