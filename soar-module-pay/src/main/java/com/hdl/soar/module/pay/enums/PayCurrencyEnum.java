package com.hdl.soar.module.pay.enums;

import cn.hutool.core.util.ArrayUtil;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Supported settlement currencies (ISO 4217 alpha code).
 * <p>
 * Each order carries exactly one currency; there is no FX conversion. {@link #minorUnits} records the
 * number of fractional digits the currency uses (VND has none, USD has two) for rounding decisions.
 * Persisted as the 3-letter alpha code so the column stays human-readable.
 */
@Getter
@AllArgsConstructor
public enum PayCurrencyEnum {

    VND("VND", 0),
    USD("USD", 2);

    /** ISO 4217 alpha-3 code, stored in {@code currency}. */
    private final String code;
    /** Number of minor-unit (fractional) digits. */
    private final int minorUnits;

    public static PayCurrencyEnum of(String code) {
        return ArrayUtil.firstMatch(e -> e.getCode().equals(code), values());
    }

    public static boolean exists(String code) {
        return of(code) != null;
    }

    /** JPA converter: maps this enum to its ISO alpha code column. */
    @Converter(autoApply = true)
    public static class JpaConverter implements AttributeConverter<PayCurrencyEnum, String> {
        @Override
        public String convertToDatabaseColumn(PayCurrencyEnum attribute) {
            return attribute == null ? null : attribute.getCode();
        }

        @Override
        public PayCurrencyEnum convertToEntityAttribute(String dbData) {
            return dbData == null ? null : of(dbData);
        }
    }

}
