package com.hdl.soar.module.pay.enums.notify;

import com.hdl.soar.framework.common.enums.converter.IntEnumConverter;
import jakarta.persistence.Converter;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * What kind of business object a notify task is about.
 * <p>
 * Only {@link #ORDER} exists today. The column is kept so refund/transfer notifies can be added
 * later without a schema change; they get their own values when those flows land.
 */
@Getter
@AllArgsConstructor
public enum PayNotifyTypeEnum {

    ORDER(1, "Order");

    /** JPA converter: maps this enum to its integer {@code type} column. */
    @Converter(autoApply = true)
    public static class JpaConverter extends IntEnumConverter<PayNotifyTypeEnum> {
        protected JpaConverter() {
            super(PayNotifyTypeEnum.class, PayNotifyTypeEnum::getType);
        }
    }

    private final Integer type;
    private final String name;

}
