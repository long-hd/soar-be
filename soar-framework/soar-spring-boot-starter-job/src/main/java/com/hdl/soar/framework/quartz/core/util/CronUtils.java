package com.hdl.soar.framework.quartz.core.util;

import org.quartz.CronExpression;

import java.text.ParseException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Utilities for Quartz CRON expressions.
 */
public class CronUtils {

    private CronUtils() {
    }

    /**
     * Checks whether a CRON expression is valid.
     *
     * @param cronExpression the CRON expression
     * @return {@code true} if the CRON expression is valid; {@code false} otherwise
     */
    public static boolean isValid(String cronExpression) {
        return CronExpression.isValidExpression(cronExpression);
    }

    /**
     * Returns the next n execution times for a CRON expression.
     *
     * @param cronExpression the CRON expression
     * @param n the number of upcoming execution times to return
     * @return a list of the next execution times that match the CRON expression
     */
    public static List<Instant> getNextTimes(String cronExpression, int n) {
        // 1. Create the CronExpression object
        CronExpression cron;
        try {
            cron = new CronExpression(cronExpression);
        } catch (ParseException e) {
            throw new IllegalArgumentException(e.getMessage());
        }

        // 2. Calculate the next n execution times starting from the current time
        Date now = new Date();
        List<Instant> nextTimes = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            Date nextTime = cron.getNextValidTimeAfter(now);

            // 2.1 If nextTime is null, there are no more valid execution times
            if (nextTime == null) {
                break;
            }
            nextTimes.add(nextTime.toInstant());

            // 2.2 Use the current execution time as the starting point for the next calculation
            now = nextTime;
        }
        return nextTimes;
    }

}
