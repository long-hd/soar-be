package com.hdl.soar.module.system.dal.redis.oauth2;

import cn.hutool.core.date.TemporalUtil;
import com.hdl.soar.framework.common.util.collection.CollectionUtils;
import com.hdl.soar.framework.common.util.json.JsonUtils;
import com.hdl.soar.module.system.dal.entity.oauth2.OAuth2AccessTokenPO;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.hdl.soar.module.system.dal.redis.RedisKeyConstants.OAUTH2_ACCESS_TOKEN;

/**
 * Redis DAO for {@link OAuth2AccessTokenPO}
 */
@Repository
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OAuth2AccessTokenRedisDAO {

    StringRedisTemplate stringRedisTemplate;

    public OAuth2AccessTokenPO get(String accessToken) {
        String redisKey = formatKey(accessToken);
        return JsonUtils.parseObject(stringRedisTemplate.opsForValue().get(redisKey), OAuth2AccessTokenPO.class);
    }

    public void set(OAuth2AccessTokenPO accessTokenPO) {
        String redisKey = formatKey(accessTokenPO.getAccessToken());
        // Remove redundant fields to avoid caching unnecessary data
        accessTokenPO.setUpdater(null).setUpdateTime(null).setCreateTime(null).setCreator(null).setDeleted(null);
        long time = TemporalUtil.between(Instant.now(), accessTokenPO.getExpiresTime(), ChronoUnit.SECONDS);
        if (time > 0) {
            stringRedisTemplate.opsForValue().set(redisKey, JsonUtils.toJsonString(accessTokenPO), time, TimeUnit.SECONDS);
        }
    }

    public void delete(String accessToken) {
        String redisKey = formatKey(accessToken);
        stringRedisTemplate.delete(redisKey);
    }

    public void deleteList(Collection<String> accessTokens) {
        List<String> redisKeys = CollectionUtils.convertList(accessTokens, OAuth2AccessTokenRedisDAO::formatKey);
        stringRedisTemplate.delete(redisKeys);
    }

    private static String formatKey(String accessToken) {
        return String.format(OAUTH2_ACCESS_TOKEN, accessToken);
    }

}
