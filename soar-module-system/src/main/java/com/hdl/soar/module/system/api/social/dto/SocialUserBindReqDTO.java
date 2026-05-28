package com.hdl.soar.module.system.api.social.dto;

import com.hdl.soar.framework.common.enums.UserTypeEnum;
import com.hdl.soar.framework.common.validation.InEnum;
import com.hdl.soar.module.system.enums.social.SocialTypeEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Unbind Social User Request DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SocialUserBindReqDTO {
    /**
     * User ID
     */
    @NotNull(message = "User ID cannot be null")
    private Long userId;

    /**
     * User type
     */
    @InEnum(UserTypeEnum.class)
    @NotNull(message = "User type cannot be null")
    private Integer userType;

    /**
     * Social platform type
     */
    @InEnum(SocialTypeEnum.class)
    @NotNull(message = "Social platform type cannot be null")
    private Integer socialType;

    /**
     * Authorization code
     */
    @NotBlank(message = "Authorization code cannot be empty")
    private String code;

    /**
     * State
     */
    @NotNull(message = "State cannot be null")
    private String state;
}
