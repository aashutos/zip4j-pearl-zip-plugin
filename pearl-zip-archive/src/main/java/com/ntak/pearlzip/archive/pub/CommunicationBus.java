/*
 * Copyright © 2021 92AK
 */
package com.ntak.pearlzip.archive.pub;

/**
 *  Interface facade over the UI communication bus implementation.
 */
public interface CommunicationBus {

    /**
     *  Message to be posted onto the communication bus.
     *
     *  @param message Typically a message POJO. Implementation can unwrap as necessary
     */
    void post(Object message);

    /**
     *  Register a callback/consumer of messages. Error handling is implementation specific.
     *
     *  @param subscriber Listener to the communication bus queue
     */
    void register(Object subscriber);

    /**
     *  Unregister a callback/consumer of messages. Does nothing if not registered already.
     *
     *  @param subscriber Listener to the communication bus queue
     */
    void unregister(Object subscriber);

    /**
     *  Retrieves underlying event bus implementation to provide full feature specification, if required.
     *
     *  @return Communication bus implementation
     */
    Object implementation();
}
