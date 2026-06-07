package com.hdl.soar.module.system.dal.postgres.oauth2;

import com.hdl.soar.module.system.dal.entity.oauth2.OAuth2RefreshTokenPO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface OAuth2RefreshTokenRepository extends JpaRepository<OAuth2RefreshTokenPO, Long> {
    Optional<OAuth2RefreshTokenPO> findByRefreshToken(String refreshToken);

    @Modifying
    @Query(value = """
                DELETE FROM system_oauth2_refresh_token
                WHERE id IN (
                    SELECT id FROM system_oauth2_refresh_token
                    WHERE expires_time < :expiresTime
                    LIMIT :deleteLimit
                )
            """, nativeQuery = true)
    int deleteByExpiresTimeLt(Instant expireTime, Integer deleteLimit);
}
