package com.hdl.soar.framework.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Documentation URLs.
 */
@Getter
@AllArgsConstructor
public enum DocumentEnum {

    REDIS_INSTALL("https://gitee.com/zhijiantianya/ruoyi-vue-pro/issues/I4VCSJ", "Redis Installation Documentation"),
    TENANT("https://doc.iocoder.cn", "SaaS Multi-Tenant Documentation");

    private final String url;
    private final String memo;

}
