package com.hdl.soar.module.system.dal.postgres.oauth2;

import com.hdl.soar.module.system.dal.entity.oauth2.OAuth2RefreshTokenPO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OAuth2RefreshTokenRepository extends JpaRepository<OAuth2RefreshTokenPO, Long> {
    Optional<OAuth2RefreshTokenPO> findByRefreshToken(String refreshToken);
}
