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
public class ProgressMessage {
    private final long sessionId;
    private final String type;
    private final String message;
    private final double completed;
    private final double total;

    public ProgressMessage(long sessionId, String type, String message, double completed, double total) {
        assert Objects.nonNull(type) : "A valid key must be entered";

        if (total == 0) {
            completed = -1;
            total = 1;
        }

        this.sessionId = sessionId;
        this.type = type;
        this.message = message;
        this.completed = completed;
        this.total = total;
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

    public long sessionId() { return sessionId; }

    public String type() { return type; }

    public String message() { return message; }

    public double completed() { return completed; }

    public double total() { return total; }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (obj == null || obj.getClass() != this.getClass())
            return false;
        var that = (ProgressMessage) obj;
        return this.sessionId == that.sessionId &&
                Objects.equals(this.type, that.type) &&
                Objects.equals(this.message, that.message) &&
                Double.doubleToLongBits(this.completed) == Double.doubleToLongBits(that.completed) &&
                Double.doubleToLongBits(this.total) == Double.doubleToLongBits(that.total);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sessionId, type, message, completed, total);
    }

}
