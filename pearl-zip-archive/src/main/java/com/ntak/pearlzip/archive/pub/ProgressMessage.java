/*
 * Copyright © 2021 92AK
 */
package com.ntak.pearlzip.archive.pub;

import java.util.Objects;

/**
 *  Event Bus message java record representing progress of a specific zip archive function (uniquely defined by
 *  sessionId). The value is the delta to be added/subtracted from current accumulation of progress. Setting to a
 *  negative number will return an indeterminate state inline with JavaFX Progress Bar. Message will be displayed on
 *  the progress bar UI. Currently, type can be accepted as either PROGRESS or COMPLETE.
 *  @author Aashutos Kakshepati
 */
public record ProgressMessage(long sessionId, String type, String message, double completed, double total) {
    public ProgressMessage {
        assert Objects.nonNull(type) : "A valid key must be entered";

        if (total == 0) {
            completed = -1;
            total = 1;
        }
    }

    @Override
    public String toString() {
        return "ProgressMessage{" +
                "sessionId=" + sessionId +
                ", type='" + type + '\'' +
                ", message='" + message + '\'' +
                ", completed=" + completed +
                ", total=" + total +
                '}';
    }
}
