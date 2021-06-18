/*
 * Copyright © 2021 92AK
 */
package com.ntak.pearlzip.ui.util;

import com.ntak.pearlzip.archive.pub.CommunicationBus;
import com.ntak.pearlzip.archive.pub.CommunicationBusFactory;
import org.greenrobot.eventbus.EventBus;

import static com.ntak.pearlzip.archive.constants.ArchiveConstants.COM_BUS_EXECUTOR_SERVICE;

/**
 *  Generates an instance of Greenrobot's EventBus for use by the PearlZip application.
 *
 */
public class EventBusFactory implements CommunicationBusFactory {
    @Override
    public CommunicationBus initializeCommunicationBus() {
        return new EventCommunicationBus();
    }

    /**
     *  Implementation of CommunicationBus, which utilises Greenrobot's EventBus.
     */
    private class EventCommunicationBus implements CommunicationBus {

        private final EventBus BUS;

        public EventCommunicationBus() {
            BUS = EventBus.builder()
                          .executorService(COM_BUS_EXECUTOR_SERVICE)
                          .build();
        }

        @Override
        public void post(Object message) {
            BUS.post(message);
        }

        @Override
        public void register(Object subscriber) {
            BUS.register(subscriber);
        }

        @Override
        public void unregister(Object subscriber) {
            BUS.unregister(subscriber);
        }

        @Override
        public Object implementation() {
            return BUS;
        }
    }
}
