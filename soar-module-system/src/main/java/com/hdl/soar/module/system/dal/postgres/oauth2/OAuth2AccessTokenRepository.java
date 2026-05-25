package com.hdl.soar.module.system.dal.postgres.oauth2;

import com.hdl.soar.module.system.dal.entity.oauth2.OAuth2AccessTokenPO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface OAuth2AccessTokenRepository extends JpaRepository<OAuth2AccessTokenPO,Long>, JpaSpecificationExecutor<OAuth2AccessTokenPO> {
    Optional<OAuth2AccessTokenPO> findByAccessToken(String accessToken);
    List<OAuth2AccessTokenPO> findByUserIdAndUserType(Long userId, Integer userType);
    List<OAuth2AccessTokenPO> findByRefreshToken(String refreshToken);

    /**
     * Physically delete access tokens that expired before the specified time.
     *
     * @param expiresTime the cutoff expiration time
     * @param deleteLimit maximum number of records to delete to prevent large batch deletion
     * @return number of records deleted
     */
    @Modifying
    @Query(value = """
        DELETE FROM system_oauth2_access_token
        WHERE id IN (
            SELECT id FROM system_oauth2_access_token
            WHERE expires_time < :expiresTime
            LIMIT :deleteLimit
        )
        """, nativeQuery = true)
    int deleteByExpiresTimeLt(@Param("expiresTime") Instant expiresTime, @Param("deleteLimit") Integer deleteLimit);
}
