package com.hdl.soar.module.system.service.social;

import com.hdl.soar.module.system.api.social.dto.SocialUserBindReqDTO;
import jakarta.validation.Valid;

public interface SocialUserService {

    /**
     * Bind a social user
     *
     * @param reqDTO binding information
     * @return social user openid
     */
    String bindSocialUser(@Valid SocialUserBindReqDTO reqDTO);

}
