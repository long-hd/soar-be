package com.hdl.soar.module.system.framework.captcha.config;

import com.anji.captcha.config.AjCaptchaAutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.context.annotation.Configuration;

/**
 * CAPTCHA configuration class
 *
 * @author Yudao Source Code
 */
@Configuration(proxyBeanMethods = false)
@ImportAutoConfiguration(AjCaptchaAutoConfiguration.class) // Purpose: fix the issue where aj-captcha auto-configuration does not work in Spring Boot 3.x
public class SoarCaptchaConfiguration {
}
