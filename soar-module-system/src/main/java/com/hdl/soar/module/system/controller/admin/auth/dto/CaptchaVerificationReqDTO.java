package com.hdl.soar.module.system.controller.admin.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Admin Backend - Captcha Verification Request VO")
public class CaptchaVerificationReqDTO {

    // ========== Image captcha related ==========

    @Schema(
            description = "Captcha verification code. Required when captcha is enabled",
            requiredMode = Schema.RequiredMode.REQUIRED,
            example = "PfcH6mgr8tpXuMWFjvW6YVaqrswIuwmWI5dsVZSg7sGpWtDCUbHuDEXl3cFB1+VvCC/rAkSwK8Fad52FSuncVg=="
    )
    @NotBlank(message = "Captcha cannot be empty", groups = CodeEnableGroup.class)
    private String captchaVerification;

    /**
     * Validation group for enabling captcha
     */
    public interface CodeEnableGroup {
    }
}
