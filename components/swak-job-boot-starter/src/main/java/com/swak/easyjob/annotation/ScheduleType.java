package com.swak.easyjob.annotation;

import java.util.Objects;


/**
 * The enum Schedule type.
 *
 * @author colley
 */
public enum ScheduleType {

    /**
     * schedule by cron
     */
    CRON("cron"),

    /**
     * schedule by fixed rate (in seconds)
     */
    FIX_RATE("rate");

    private final String type;

    ScheduleType(String type) {
        this.type = type;
    }

    /**
     * Gets type.
     *
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * Match boolean.
     *
     * @param type the type
     * @return the boolean
     */
    public boolean match(String type) {
        return Objects.equals(getType(), type);
    }
}
