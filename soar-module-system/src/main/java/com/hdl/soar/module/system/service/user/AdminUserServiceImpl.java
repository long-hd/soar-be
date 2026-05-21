package com.hdl.soar.module.system.service.user;

import com.hdl.soar.module.system.dal.entity.user.AdminUserPO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Backend User Service Implementation Class
 */
@Slf4j
@Service("adminUserService")
public class AdminUserServiceImpl implements AdminUserService {


    @Override
    public AdminUserPO getUser(Long id) {
        return null;
    }
}
