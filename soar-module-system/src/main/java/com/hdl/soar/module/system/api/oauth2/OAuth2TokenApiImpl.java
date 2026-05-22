package com.hdl.soar.module.system.api.oauth2;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import com.hdl.soar.framework.common.biz.system.oauth2.OAuth2TokenCommonApi;
import com.hdl.soar.framework.common.biz.system.oauth2.dto.OAuth2AccessTokenCheckRespDTO;
import com.hdl.soar.framework.common.biz.system.oauth2.dto.OAuth2AccessTokenCreateReqDTO;
import com.hdl.soar.framework.common.biz.system.oauth2.dto.OAuth2AccessTokenRespDTO;
import com.hdl.soar.framework.common.enums.UserTypeEnum;
import com.hdl.soar.framework.common.util.object.BeanUtils;
import com.hdl.soar.framework.security.core.LoginUser;
import com.hdl.soar.module.system.dal.entity.oauth2.OAuth2AccessTokenPO;
import com.hdl.soar.module.system.dal.entity.user.AdminUserPO;
import com.hdl.soar.module.system.service.oauth2.OAuth2TokenService;
import com.hdl.soar.module.system.service.user.AdminUserService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OAuth2TokenApiImpl implements OAuth2TokenCommonApi {

    OAuth2TokenService oauth2TokenService;

    @Override
    public OAuth2AccessTokenRespDTO createAccessToken(OAuth2AccessTokenCreateReqDTO reqDTO) {
        OAuth2AccessTokenPO accessTokenEntity = oauth2TokenService.createAccessToken(
                reqDTO.getUserId(), reqDTO.getUserType(),
                reqDTO.getClientId(), reqDTO.getScopes());
        return BeanUtils.toBean(accessTokenEntity, OAuth2AccessTokenRespDTO.class);
    }

    @Override
    public OAuth2AccessTokenCheckRespDTO checkAccessToken(String accessToken) {
        OAuth2AccessTokenPO accessTokenEntity = oauth2TokenService.checkAccessToken(accessToken);
        return BeanUtils.toBean(accessTokenEntity, OAuth2AccessTokenCheckRespDTO.class);
    }

    @Override
    public OAuth2AccessTokenRespDTO removeAccessToken(String accessToken) {
        OAuth2AccessTokenPO accessTokenEntity = oauth2TokenService.removeAccessToken(accessToken);
        return BeanUtils.toBean(accessTokenEntity, OAuth2AccessTokenRespDTO.class);
    }

    @Override
    public OAuth2AccessTokenRespDTO refreshAccessToken(String refreshToken, String clientId) {
        OAuth2AccessTokenPO accessTokenDO = oauth2TokenService.refreshAccessToken(refreshToken, clientId);
        return BeanUtils.toBean(accessTokenDO, OAuth2AccessTokenRespDTO.class);
    }

}
