/*
 * Copyright (c) ${YEAR} 92AK
 */
package com.ntak.pearlzip.ui.util;

import com.ntak.pearlzip.archive.pub.ProgressMessage;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import static com.ntak.pearlzip.archive.util.LoggingUtil.resolveTextKey;
import static com.ntak.pearlzip.ui.constants.ZipConstants.LOG_PROGRESS_MSG;

/**
 *  EventBus consumer, which persists process messages as trace log entries.
 *  @author Aashutos Kakshepati
*/
public class ProgressMessageTraceLogger {

    private static final ProgressMessageTraceLogger MESSAGE_TRACE_LOGGER = new ProgressMessageTraceLogger();

    private ProgressMessageTraceLogger(){};

    private static final Logger LOGGER = LoggerContext.getContext().getLogger(ProgressMessageTraceLogger.class);

    @Subscribe(threadMode=ThreadMode.BACKGROUND)
    public void logProgressMessage(ProgressMessage message) {
        // LOG: Progress Message Received: %s
        LOGGER.trace(resolveTextKey(LOG_PROGRESS_MSG, message));
    }

    public static ProgressMessageTraceLogger getMessageTraceLogger() {return MESSAGE_TRACE_LOGGER;};

}
