package com.hdl.soar.framework.quartz.core.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class CronUtilsTest {

    @Test
    @DisplayName("valid CRON is accepted, invalid is rejected")
    void isValid() {
        assertThat(CronUtils.isValid("0 0 2 * * ?")).isTrue();  // 02:00 daily
        assertThat(CronUtils.isValid("0/10 * * * * ?")).isTrue(); // every 10s
        assertThat(CronUtils.isValid("not a cron")).isFalse();
        assertThat(CronUtils.isValid("60 * * * * ?")).isFalse(); // 60 is out of range for seconds
    }

    @Test
    @DisplayName("getNextTimes returns the requested count, strictly increasing")
    void getNextTimes_countAndOrder() {
        List<Instant> times = CronUtils.getNextTimes("0 0 2 * * ?", 5); // daily -> always 5 ahead

        assertThat(times).hasSize(5);
        // each occurrence is strictly after the previous one
        for (int i = 1; i < times.size(); i++) {
            assertThat(times.get(i)).isAfter(times.get(i - 1));
        }
        // and all are in the future
        assertThat(times.get(0)).isAfter(Instant.now());
    }

    @Test
    @DisplayName("invalid CRON in getNextTimes throws IllegalArgumentException")
    void getNextTimes_invalid() {
        try {
            CronUtils.getNextTimes("not a cron", 3);
            assertThat(false).as("expected IllegalArgumentException").isTrue();
        } catch (IllegalArgumentException expected) {
            // ok
        }
    }

}
