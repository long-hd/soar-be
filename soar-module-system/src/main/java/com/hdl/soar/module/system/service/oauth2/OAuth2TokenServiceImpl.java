package com.hdl.soar.module.system.service.oauth2;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.hdl.soar.framework.common.enums.UserTypeEnum;
import com.hdl.soar.framework.common.pojo.PageResult;
import com.hdl.soar.framework.security.core.LoginUser;
import com.hdl.soar.framework.tenant.core.context.TenantContextHolder;
import com.hdl.soar.module.system.controller.admin.oauth2.dto.token.OAuth2AccessTokenPageReqDTO;
import com.hdl.soar.module.system.dal.entity.oauth2.OAuth2AccessTokenPO;
import com.hdl.soar.module.system.dal.entity.oauth2.OAuth2ClientPO;
import com.hdl.soar.module.system.dal.entity.oauth2.OAuth2RefreshTokenPO;
import com.hdl.soar.module.system.dal.entity.user.AdminUserPO;
import com.hdl.soar.module.system.dal.postgres.oauth2.OAuth2AccessTokenRepository;
import com.hdl.soar.module.system.dal.postgres.oauth2.OAuth2RefreshTokenRepository;
import com.hdl.soar.module.system.dal.redis.oauth2.OAuth2AccessTokenRedisDAO;
import com.hdl.soar.module.system.service.user.AdminUserService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Implementation of OAuth2.0 Token Service.
 */
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OAuth2TokenServiceImpl implements OAuth2TokenService {

    OAuth2AccessTokenRepository oAuth2AccessTokenRepository;
    OAuth2RefreshTokenRepository oAuth2RefreshTokenRepository;

    OAuth2AccessTokenRedisDAO oauth2AccessTokenRedisDAO;

    OAuth2ClientService oauth2ClientService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OAuth2AccessTokenPO createAccessToken(Long userId, Integer userType,
                                                 String clientId, List<String> scopes,
                                                 Map<String, String> userInfo) {
        OAuth2ClientPO clientPO = oauth2ClientService.validOAuthClientFromCache(clientId);
        // Create refresh token
        OAuth2RefreshTokenPO refreshTokenPO = createOAuth2RefreshToken(userId, userType, clientPO, scopes);
        // Create access token
        return createOAuth2AccessToken(refreshTokenPO, clientPO, userInfo);
    }

    @Override
    public OAuth2AccessTokenPO checkAccessToken(String accessToken) {
        return null;
    }

    @Override
    public OAuth2AccessTokenPO removeAccessToken(String accessToken) {
        return null;
    }

    @Override
    public void removeAccessToken(Long userId, Integer userType) {

    }

    @Override
    public OAuth2AccessTokenPO refreshAccessToken(String refreshToken, String clientId) {
        return null;
    }

    @Override
    public OAuth2AccessTokenPO getAccessToken(String accessToken) {
        return null;
    }

    @Override
    public PageResult<OAuth2AccessTokenPO> getAccessTokenPage(OAuth2AccessTokenPageReqDTO reqDTO) {
        return null;
    }

    @Override
    public Integer cleanAccessToken(Integer exceedDay, Integer deleteLimit) {
        return 0;
    }

    @Override
    public Integer cleanRefreshToken(Integer exceedDay, Integer deleteLimit) {
        return 0;
    }

    private OAuth2AccessTokenPO createOAuth2AccessToken(OAuth2RefreshTokenPO refreshTokenPO, OAuth2ClientPO clientPO,
                                                        Map<String, String> userInfo) {
        OAuth2AccessTokenPO accessTokenPO = OAuth2AccessTokenPO.builder()
                .accessToken(generateAccessToken())
                .userId(refreshTokenPO.getUserId()).userType(refreshTokenPO.getUserType())
                .userInfo(userInfo)
                .clientId(clientPO.getClientId()).scopes(clientPO.getScopes())
                .refreshToken(refreshTokenPO.getRefreshToken())
                .expiresTime(Instant.now().plusSeconds(clientPO.getAccessTokenValiditySeconds()))
                .build();
        // Prefer to obtain tenant ID from refreshToken first, to avoid tenantId being null
        // when ThreadLocal is polluted
        // Related issue: https://t.zsxq.com/JIi5G
        Long tenantId = refreshTokenPO.getTenantId();
        if (tenantId == null) {
            tenantId = TenantContextHolder.getTenantId();
        }
        accessTokenPO.setTenantId(tenantId);
        oAuth2AccessTokenRepository.save(accessTokenPO);
        // Save to Redis
        oauth2AccessTokenRedisDAO.set(accessTokenPO);
        return accessTokenPO;
    }

    private OAuth2RefreshTokenPO createOAuth2RefreshToken(Long userId, Integer userType, OAuth2ClientPO clientEntity, List<String> scopes) {
        OAuth2RefreshTokenPO refreshToken = OAuth2RefreshTokenPO.builder()
                .refreshToken(generateRefreshToken())
                .userId(userId).userType(userType)
                .clientId(clientEntity.getClientId()).scopes(scopes)
                .expiresTime(Instant.now().plusSeconds(clientEntity.getRefreshTokenValiditySeconds()))
                .build();
        oAuth2RefreshTokenRepository.save(refreshToken);
        return refreshToken;
    }

    private static String generateAccessToken() {
        return IdUtil.fastSimpleUUID();
    }

    private static String generateRefreshToken() {
        return IdUtil.fastSimpleUUID();
    }

}
