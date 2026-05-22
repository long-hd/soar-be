package com.hdl.soar.module.system.service.oauth2;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.hdl.soar.framework.common.enums.UserTypeEnum;
import com.hdl.soar.framework.common.exception.ServiceException;
import com.hdl.soar.framework.common.exception.enums.GlobalErrorCodeConstants;
import com.hdl.soar.framework.common.pojo.PageResult;
import com.hdl.soar.framework.common.util.date.InstantUtils;
import com.hdl.soar.framework.common.util.object.BeanUtils;
import com.hdl.soar.framework.jpa.core.util.PageUtils;
import com.hdl.soar.framework.security.core.LoginUser;
import com.hdl.soar.framework.tenant.core.context.TenantContextHolder;
import com.hdl.soar.framework.tenant.core.util.TenantUtils;
import com.hdl.soar.module.system.controller.admin.oauth2.dto.token.OAuth2AccessTokenPageReqDTO;
import com.hdl.soar.module.system.dal.entity.oauth2.OAuth2AccessTokenPO;
import com.hdl.soar.module.system.dal.entity.oauth2.OAuth2AccessTokenPO_;
import com.hdl.soar.module.system.dal.entity.oauth2.OAuth2ClientPO;
import com.hdl.soar.module.system.dal.entity.oauth2.OAuth2RefreshTokenPO;
import com.hdl.soar.module.system.dal.entity.user.AdminUserPO;
import com.hdl.soar.module.system.dal.postgres.oauth2.OAuth2AccessTokenRepository;
import com.hdl.soar.module.system.dal.postgres.oauth2.OAuth2RefreshTokenRepository;
import com.hdl.soar.module.system.dal.postgres.user.AdminUserRepository;
import com.hdl.soar.module.system.dal.redis.oauth2.OAuth2AccessTokenRedisDAO;
import com.hdl.soar.module.system.enums.ErrorCodeConstants;
import jakarta.persistence.criteria.Predicate;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.hdl.soar.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.hdl.soar.framework.common.exception.util.ServiceExceptionUtil.exception0;
import static com.hdl.soar.framework.common.util.collection.CollectionUtils.convertSet;
import static com.hdl.soar.framework.jpa.core.util.SpecUtils.*;

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

    AdminUserRepository adminUserRepository;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OAuth2AccessTokenPO createAccessToken(Long userId, Integer userType,
                                                 String clientId, List<String> scopes) {
        OAuth2ClientPO clientPO = oauth2ClientService.validOAuthClientFromCache(clientId);
        // Create refresh token
        OAuth2RefreshTokenPO refreshTokenPO = createOAuth2RefreshToken(userId, userType, clientPO, scopes);
        // Create access token
        return createOAuth2AccessToken(refreshTokenPO, clientPO);
    }

    @Override
    public OAuth2AccessTokenPO checkAccessToken(String accessToken) {
        OAuth2AccessTokenPO accessTokenPO = getAccessToken(accessToken);
        if (accessTokenPO == null) {
            throw exception0(GlobalErrorCodeConstants.UNAUTHORIZED.getCode(), "Access token does not exist");
        }
        if (Instant.now().isAfter(accessTokenPO.getExpiresTime())) {
            throw exception0(GlobalErrorCodeConstants.UNAUTHORIZED.getCode(), "Access token has expired");
        }
        return accessTokenPO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OAuth2AccessTokenPO removeAccessToken(String accessToken) {
        // Delete access token
        OAuth2AccessTokenPO accessTokenPO = oAuth2AccessTokenRepository.findByAccessToken(accessToken)
                .orElse(null);
        if (accessTokenPO == null) {
            return null;
        }
        oAuth2AccessTokenRepository.deleteById(accessTokenPO.getId());
        oauth2AccessTokenRedisDAO.delete(accessToken);

        // Delete refresh token
        oAuth2RefreshTokenRepository.findByRefreshToken(accessTokenPO.getRefreshToken())
                .ifPresent(oAuth2RefreshTokenRepository::delete);
        oauth2AccessTokenRedisDAO.delete(accessTokenPO.getRefreshToken());

        return accessTokenPO;
    }

    @Override
    public void removeAccessToken(Long userId, Integer userType) {
        List<OAuth2AccessTokenPO> accessTokens = oAuth2AccessTokenRepository.findByUserIdAndUserType(userId, userType);
        if (CollUtil.isEmpty(accessTokens)) {
            return;
        }

        accessTokens.forEach(accessToken -> {
            // Delete access token
            oAuth2AccessTokenRepository.deleteById(accessToken.getId());
            oauth2AccessTokenRedisDAO.delete(accessToken.getAccessToken());

            // Delete refresh token
            oAuth2RefreshTokenRepository.findByRefreshToken(accessToken.getRefreshToken())
                    .ifPresent(oAuth2RefreshTokenRepository::delete);
            oauth2AccessTokenRedisDAO.delete(accessToken.getRefreshToken());
        });
    }

    @Override
    @Transactional(noRollbackFor = ServiceException.class)
    public OAuth2AccessTokenPO refreshAccessToken(String refreshToken, String clientId) {
        // Query refresh token
        OAuth2RefreshTokenPO refreshTokenPO = oAuth2RefreshTokenRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> exception0(GlobalErrorCodeConstants.BAD_REQUEST.getCode(), "Invalid refresh token"));

        // Validate client match
        OAuth2ClientPO clientPO = oauth2ClientService.validOAuthClientFromCache(clientId);
        if (ObjectUtil.notEqual(clientId, refreshTokenPO.getClientId())) {
            throw exception0(GlobalErrorCodeConstants.BAD_REQUEST.getCode(), "The client ID of the refresh token is incorrect");
        }

        // Remove related access tokens
        List<OAuth2AccessTokenPO> accessTokenDOs = oAuth2AccessTokenRepository.findByRefreshToken(refreshToken);
        if (CollUtil.isNotEmpty(accessTokenDOs)) {
            oAuth2AccessTokenRepository.deleteAllById(convertSet(accessTokenDOs, OAuth2AccessTokenPO::getId));
            oauth2AccessTokenRedisDAO.deleteList(convertSet(accessTokenDOs, OAuth2AccessTokenPO::getAccessToken));
        }

        // If expired, delete the refresh token
        if (Instant.now().isAfter(refreshTokenPO.getExpiresTime())) {
            oAuth2RefreshTokenRepository.deleteById(refreshTokenPO.getId());
            throw exception0(GlobalErrorCodeConstants.UNAUTHORIZED.getCode(), "Refresh token has expired");
        }

        // Create access token
        return createOAuth2AccessToken(refreshTokenPO, clientPO);
    }

    @Override
    public OAuth2AccessTokenPO getAccessToken(String accessToken) {
        // Prefer fetching from Redis first
        OAuth2AccessTokenPO accessTokenPO = oauth2AccessTokenRedisDAO.get(accessToken);
        if (accessTokenPO != null) {
            return accessTokenPO;
        }

        // If not found, fetch the access token from Database
        accessTokenPO = oAuth2AccessTokenRepository.findByAccessToken(accessToken).orElse(null);
        if (accessTokenPO == null) {
            // Special case: fetch refresh token from Database.
            // Reason: solve scenarios where refreshing the access token is inconvenient.
            // For example, Jimu Report only allows passing a token and does not allow passing a refresh_token,
            // making it impossible to refresh the access token.
            // Another example is when the frontend WebSocket token is directly appended to the URL,
            // where passing a refresh_token is not possible.
            OAuth2RefreshTokenPO refreshTokenPO = oAuth2RefreshTokenRepository.findByRefreshToken(accessToken).orElse(null);
            if (refreshTokenPO != null && !InstantUtils.isExpired(refreshTokenPO.getExpiresTime())) {
                accessTokenPO = convertToAccessToken(refreshTokenPO);
            }
        }

        // If it exists in Database, write it back to Redis
        if (accessTokenPO != null && !InstantUtils.isExpired(accessTokenPO.getExpiresTime())) {
            oauth2AccessTokenRedisDAO.set(accessTokenPO);
        }

        return accessTokenPO;
    }

    @Override
    public PageResult<OAuth2AccessTokenPO> getAccessTokenPage(OAuth2AccessTokenPageReqDTO reqDTO) {
        Specification<OAuth2AccessTokenPO> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            eqIfPresent(predicates, cb, root, OAuth2AccessTokenPO_.userId, reqDTO.getUserId());
            eqIfPresent(predicates, cb, root, OAuth2AccessTokenPO_.userType, reqDTO.getUserType());
            likeIfPresent(predicates, cb, root, OAuth2AccessTokenPO_.clientId, reqDTO.getClientId());
            gt(predicates, cb, root, OAuth2AccessTokenPO_.expiresTime, Instant.now());
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        Page<OAuth2AccessTokenPO> page = oAuth2AccessTokenRepository.findAll(spec,
                PageUtils.toPageable(reqDTO, Sort.by(Sort.Direction.DESC, OAuth2AccessTokenPO_.ID)));
        return PageUtils.toPageResult(page);
    }

    @Override
    public Integer cleanAccessToken(Integer exceedDay, Integer deleteLimit) {
        int count = 0;
        Instant expireTime = Instant.now().minus(exceedDay, ChronoUnit.DAYS);

        // Loop deletion until no more matching data
        for (int i = 0; i < Short.MAX_VALUE; i++) {
            int deleteCount = oAuth2AccessTokenRepository.deleteByExpiresTimeLt(expireTime, deleteLimit);
            count += deleteCount;

            // If deleted fewer than the limit, it means we've reached the end
            if (deleteCount < deleteLimit) {
                break;
            }
        }

        return count;
    }

    @Override
    public Integer cleanRefreshToken(Integer exceedDay, Integer deleteLimit) {
        int count = 0;
        Instant expireTime = Instant.now().minus(exceedDay, ChronoUnit.DAYS);

        // Repeatedly delete until there is no more data matching the condition
        for (int i = 0; i < Short.MAX_VALUE; i++) {
            int deleteCount = oAuth2RefreshTokenRepository.deleteByExpiresTimeLt(expireTime, deleteLimit);
            count += deleteCount;

            // If the number of deleted records is less than the expected limit,
            // it means all matching data has been deleted
            if (deleteCount < deleteLimit) {
                break;
            }
        }

        return count;
    }

    private OAuth2AccessTokenPO createOAuth2AccessToken(OAuth2RefreshTokenPO refreshTokenPO, OAuth2ClientPO clientPO) {
        OAuth2AccessTokenPO accessTokenPO = OAuth2AccessTokenPO.builder()
                .accessToken(generateAccessToken())
                .userId(refreshTokenPO.getUserId()).userType(refreshTokenPO.getUserType())
                .userInfo(buildUserInfo(refreshTokenPO.getUserId(), refreshTokenPO.getUserType()))
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

    private OAuth2AccessTokenPO convertToAccessToken(OAuth2RefreshTokenPO refreshTokenPO) {
        OAuth2AccessTokenPO accessTokenPO = BeanUtils.toBean(refreshTokenPO, OAuth2AccessTokenPO.class)
                .setAccessToken(refreshTokenPO.getRefreshToken());
        TenantUtils.execute(refreshTokenPO.getTenantId(),
                () -> accessTokenPO.setUserInfo(buildUserInfo(refreshTokenPO.getUserId(), refreshTokenPO.getUserType())));
        return accessTokenPO;
    }

    private static String generateAccessToken() {
        return IdUtil.fastSimpleUUID();
    }

    private static String generateRefreshToken() {
        return IdUtil.fastSimpleUUID();
    }

    /**
     * Load user information to help {@link LoginUser}
     * obtain nickname, department, and other related information.
     *
     * @param userId   User ID
     * @param userType User type
     * @return User information
     */
    private Map<String, String> buildUserInfo(Long userId, Integer userType) {
        if (userId == null || userId <= 0) {
            return Collections.emptyMap();
        }

        if (userType.equals(UserTypeEnum.ADMIN.getValue())) {
            AdminUserPO user = adminUserRepository.findById(userId)
                    .orElseThrow(() -> exception(ErrorCodeConstants.USER_NOT_EXISTS));
            return MapUtil.builder(LoginUser.INFO_KEY_NICKNAME, user.getNickname())
                    .put(LoginUser.INFO_KEY_DEPT_ID, StrUtil.toStringOrNull(user.getDeptId()))
                    .build();

        } else if (userType.equals(UserTypeEnum.MEMBER.getValue())) {
            // Note: Member information is not loaded for now and can be implemented as needed
            return Collections.emptyMap();
        }

        throw new IllegalArgumentException("Unknown user type: " + userType);
    }

}
