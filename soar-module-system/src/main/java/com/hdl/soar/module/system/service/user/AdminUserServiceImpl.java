package com.hdl.soar.module.system.service.user;

import com.hdl.soar.module.system.dal.entity.user.AdminUserPO;
import com.hdl.soar.module.system.dal.postgres.user.AdminUserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;

/**
 * Backend User Service Implementation Class
 */
@Slf4j
@Service("adminUserService")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdminUserServiceImpl implements AdminUserService {

    AdminUserRepository adminUserRepository;

    PasswordEncoder passwordEncoder;

    @Override
    public void updateUserLogin(Long id, String loginIp) {
        adminUserRepository.findById(id).ifPresent(adminUser -> {
            adminUser.setLoginIp(loginIp);
            adminUser.setLoginDate(Instant.now());
            adminUserRepository.save(adminUser);
        });
    }

    @Override
    public AdminUserPO getUserByUsername(String username) {
        return adminUserRepository.findByUsername(username).orElse(null);
    }

    @Override
    public AdminUserPO getUser(Long id) {
        return adminUserRepository.findById(id).orElse(null);
    }

    @Override
    public boolean isPasswordMatch(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }
}
