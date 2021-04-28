/*
 * Copyright © 2021 92AK
 */
package com.ntak.pearlzip.ui.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;

public class MetricThreadFactoryTest {

    /*
        Test cases:
        + Create logging metric thread
        + Run metric thread -> Successfully calls the expected functional interface implementations
        + Run metric thread -> Handle exception with ThreadGroup
     */

    private AtomicBoolean atoBefore = new AtomicBoolean(false);
    private AtomicBoolean atoAfter = new AtomicBoolean(false);
    private AtomicBoolean atoRunnable = new AtomicBoolean(false);
    private AtomicBoolean atoUncaught = new AtomicBoolean(false);
    private MetricProfile profile = new MetricProfile((t)->atoBefore.set(true), (t)->atoAfter.set(true));
    private ThreadFactory factory;

    public void setUp() {
        atoBefore.set(false);
        atoRunnable.set(false);
        atoAfter.set(false);
        atoUncaught.set(false);
    }

    @Test
    @DisplayName("Test: Create Logging Metric Thread using the generated factory")
    public Thread testCreateLoggingMetricThread_Success() {
        factory = MetricThreadFactory.create(profile);
        Assertions.assertNotNull(factory, "No factory was returned");
        Assertions.assertTrue(factory instanceof MetricThreadFactory, "The factory was not a MetricThreadFactory");

        Thread t = factory.newThread(()->atoRunnable.set(true));
        Assertions.assertNotNull(t, "No thread was returned");
        Assertions.assertTrue(t instanceof MetricThreadFactory.LoggingMetricThread, "A LoggingMetricThread was not " +
                "returned");

        return t;
    }


    @Test
    @DisplayName("Test: Run a generated Metric Thread successfully")
    public void testRunMetricThread_Success() {
        Assertions.assertFalse(atoBefore.get(), "atoBefore not initialised");
        Assertions.assertFalse(atoRunnable.get(), "atoRunnable not initialised");
        Assertions.assertFalse(atoAfter.get(), "atoAfter not initialised");

        Thread t = testCreateLoggingMetricThread_Success();
        t.run();

        Assertions.assertTrue(atoBefore.get(), "atoBefore did not run");
        Assertions.assertTrue(atoRunnable.get(), "atoRunnable did not run");
        Assertions.assertTrue(atoAfter.get(), "atoAfter did not run");
    }

    @Test
    @DisplayName("Test: Runnable throws exception in metric thread and is processed by exception handler")
    public void testRunMetricThread_RaiseException_MatchExpectations() throws InterruptedException {
        Assertions.assertFalse(atoBefore.get(), "atoBefore not initialised");
        Assertions.assertFalse(atoRunnable.get(), "atoRunnable not initialised");
        Assertions.assertFalse(atoAfter.get(), "atoAfter not initialised");
        Assertions.assertFalse(atoUncaught.get(), "atoAfter not initialised");
        CountDownLatch latch = new CountDownLatch(1);

        MetricProfile metricProfile = new MetricProfile((t)->atoBefore.set(true),
                                                        (t)->{
                                                            atoAfter.set(true);
                                                            latch.countDown();
                                                        });

        factory = MetricThreadFactory.create(metricProfile);
        Assertions.assertNotNull(factory, "No factory was returned");
        Assertions.assertTrue(factory instanceof MetricThreadFactory, "The factory was not a MetricThreadFactory");

        Thread t = factory.newThread(()->{
            atoRunnable.set(true);
            throw new RuntimeException("A runtime exception");
        });
        t.setUncaughtExceptionHandler((th,e)->atoUncaught.set(true));
        Assertions.assertNotNull(t, "No thread was returned");
        Assertions.assertTrue(t instanceof MetricThreadFactory.LoggingMetricThread, "A LoggingMetricThread was not " +
                "returned");

        t.start();
        latch.await();

        Assertions.assertTrue(atoBefore.get(), "atoBefore did not run");
        Assertions.assertTrue(atoRunnable.get(), "atoRunnable did not run");
        Assertions.assertTrue(atoUncaught.get(), "atoUncaught did not run");
        Assertions.assertTrue(atoAfter.get(), "atoAfter did not run");
    }
}
