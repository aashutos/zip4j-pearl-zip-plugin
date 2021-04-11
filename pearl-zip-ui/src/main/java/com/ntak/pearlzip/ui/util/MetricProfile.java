/*
 * Copyright © 2021 92AK
 */
package com.ntak.pearlzip.ui.util;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.function.Consumer;

/**
 *  Bean representation of pre-execution and post-execution routines upon execution of a MetricLoggingThread.
 *  @author Aashutos Kakshepati
*/
public record MetricProfile(Consumer<Thread> beforeMetric, Consumer<Thread> afterMetric) {

    private static final Logger LOGGER = LoggerContext.getContext().getLogger(MetricProfile.class);

    public MetricProfile {
        assert Objects.nonNull(beforeMetric) : "Before Metric Consumer should not be null";
        assert Objects.nonNull(afterMetric) : "After Metric Consumer should not be null";
    }

    public static MetricProfile getDefaultProfile() {
        Consumer<Thread> loggerStart = (t)-> LOGGER.info(String.format("Thread %s : started at: %s", t.getName(), LocalDateTime.now()));
        Consumer<Thread> loggerComplete = (t)-> LOGGER.info(String.format("Thread %s : completed at: %s", t.getName(), LocalDateTime.now()));
        return new MetricProfile(loggerStart, loggerComplete);
    }
}

