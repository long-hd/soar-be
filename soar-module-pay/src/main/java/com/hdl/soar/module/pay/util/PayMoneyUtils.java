package com.hdl.soar.module.pay.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Money helpers for the pay module.
 */
public final class PayMoneyUtils {

    private PayMoneyUtils() {
    }

    /**
     * Compute a channel fee amount from an order amount and a percentage rate.
     * <p>
     * {@code fee = amount * feeRatePercent / 100}, rounded HALF_UP to 4 decimal places.
     *
     * @param amount         order amount
     * @param feeRatePercent fee rate as a percentage (e.g. 0.5 means 0.5%)
     * @return the fee amount, or {@code null} if either input is {@code null}
     */
    public static BigDecimal calculateFeePrice(BigDecimal amount, Double feeRatePercent) {
        if (amount == null || feeRatePercent == null) {
            return null;
        }
        return amount.multiply(BigDecimal.valueOf(feeRatePercent))
                .divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
    }

}
