/*
 * Copyright © 2021 92AK
 */
package com.ntak.pearlzip.archive.zip4j.pub;

import static com.ntak.pearlzip.archive.constants.ConfigurationConstants.CNS_COM_BUS_FACTORY;

public class Zip4jArchiveWriteServiceTest extends Zip4jArchiveWriteServiceTestCore {
    public Zip4jArchiveWriteServiceTest() {
        System.setProperty(CNS_COM_BUS_FACTORY, "com.ntak.testfx.MockCommunicationBusFactory");
    }
}
