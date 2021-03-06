/*
 * Copyright (c) ${YEAR} 92AK
 */
package com.ntak.pearlzip.ui.util;

import static com.ntak.pearlzip.archive.util.LoggingUtil.getStackTraceFromException;
import static com.ntak.pearlzip.archive.util.LoggingUtil.resolveTextKey;
import static com.ntak.pearlzip.ui.constants.ZipConstants.LOG_THREAD_EXECUTION_ISSUE;
import static com.ntak.pearlzip.ui.constants.ZipConstants.ROOT_LOGGER;

/**
 * An interface which is a Runnable that automatically handles all Exceptions by rerouting it to the
 * UncaughtExceptionHandler or prints out to stdout.
 * <p>
 * It can be implemented as a functional interface.
 *  @author Aashutos Kakshepati
*/
public interface CaughtRunnable extends Runnable {

    @Override
    default void run() {
        try {
            execute();
        } catch(Exception e) {
            errorHandler(e);
        }
    }

    default void errorHandler(Exception e) {
        final Thread.UncaughtExceptionHandler exceptionHandler = Thread.currentThread()
                                                                       .getUncaughtExceptionHandler();
        if (!(exceptionHandler instanceof ThreadGroup)) {
            exceptionHandler
                    .uncaughtException(Thread.currentThread(), e);
        } else {
            // Exception occurred on Thread: %s with exception message: %s\nStack trace: %s
            ROOT_LOGGER.error(resolveTextKey(LOG_THREAD_EXECUTION_ISSUE, Thread.currentThread()
                                                                          .getName(),
                                        e.getLocalizedMessage(),
                                        getStackTraceFromException(e))
            );
        }
    }

    void execute() throws Exception;
}
