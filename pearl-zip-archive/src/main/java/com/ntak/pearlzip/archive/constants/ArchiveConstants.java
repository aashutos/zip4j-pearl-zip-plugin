/*
 * Copyright © 2021 92AK
 */
package com.ntak.pearlzip.archive.constants;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *  Constant values utilised by the Archive service.
 *  @author Aashutos Kakshepati
 */
public class ArchiveConstants {
    public static final ThreadGroup EVENTBUS_THREAD_GROUP = new ThreadGroup("EVENTBUS-THREAD-GROUP");
    public static final ExecutorService EVENTBUS_EXECUTOR_SERVICE =
            Executors.newScheduledThreadPool(2*Runtime.getRuntime().availableProcessors(),
                                             (r)->new Thread(EVENTBUS_THREAD_GROUP,r));
}
