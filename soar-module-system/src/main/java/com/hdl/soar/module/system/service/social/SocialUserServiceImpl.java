package com.hdl.soar.module.system.service.social;

import com.hdl.soar.module.system.api.social.dto.SocialUserBindReqDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

/**
 * Social User Service Implementation
 */
@Slf4j
@Service
@Validated
public class SocialUserServiceImpl implements SocialUserService{
    @Override
    public String bindSocialUser(SocialUserBindReqDTO reqDTO) {
        return "";
    }
}
