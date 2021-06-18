/*
 * Copyright © 2021 92AK
 */
package com.ntak.testfx;

import com.ntak.pearlzip.archive.pub.CommunicationBus;
import com.ntak.pearlzip.archive.pub.CommunicationBusFactory;

import static org.mockito.Mockito.mock;

public class MockCommunicationBusFactory implements CommunicationBusFactory {
    @Override
    public CommunicationBus initializeCommunicationBus() {
        return mock(CommunicationBus.class);
    }
}
