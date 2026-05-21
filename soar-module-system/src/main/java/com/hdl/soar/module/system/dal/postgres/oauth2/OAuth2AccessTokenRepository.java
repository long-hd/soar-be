package com.hdl.soar.module.system.dal.postgres.oauth2;

import com.hdl.soar.module.system.dal.entity.oauth2.OAuth2AccessTokenPO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OAuth2AccessTokenRepository extends JpaRepository<OAuth2AccessTokenPO,Long> {
}
