/*
 * Copyright © 2021 92AK
 */
package com.ntak.pearlzip.archive.pub;

/**
 *  Generates implementation of an communication bus to be used by the PearlZip application components.
 */
public interface CommunicationBusFactory {
    CommunicationBus initializeCommunicationBus();
}
