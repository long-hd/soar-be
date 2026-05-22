package com.hdl.soar.framework.common.util.date;

import java.time.Instant;

public class InstantUtils {

    public static boolean isExpired(Instant instant){
        Instant now = Instant.now();
        return now.isAfter(instant);
    }

}
