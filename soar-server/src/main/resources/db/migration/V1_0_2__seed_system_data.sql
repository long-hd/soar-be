-- =====================================================================
-- Soar V2 — Minimal seed data for login flow
--
-- Default admin credentials:  admin / admin123
-- Default tenant:             id=1 (Soar)
-- Default OAuth2 client:      clientId=default, secret=admin123
-- =====================================================================

-- 1. Default tenant (package_id=0 means system tenant, no package restriction)
INSERT INTO system_tenant (id, name, contact_name, contact_mobile, status, package_id, expire_time, account_count,
                           creator, create_time, updater, update_time, deleted)
VALUES (1, 'Soar', 'Admin', '', 0, 0, '2099-12-31 23:59:59+00', 9999,
        1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, false);

-- 2. Root department
INSERT INTO system_dept (id, name, parent_id, sort, status,
                         creator, create_time, updater, update_time, deleted, tenant_id)
VALUES (100, 'Soar', 0, 0, 0,
        1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, false, 1);

-- 3. Admin user (password: admin123, BCrypt strength=4)
INSERT INTO system_users (id, username, password, nickname, dept_id, status,
                          creator, create_time, updater, update_time, deleted, tenant_id)
VALUES (1, 'admin', '$2a$04$.vd8nPeLwxt6hnSzmAoAyul8BOLX7Cib6QhcxRe30rfvrIPQHH1OG', 'Admin',
        100, 0,
        1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, false, 1);

-- 4. Super admin role (type=1 SYSTEM, data_scope=1 ALL)
INSERT INTO system_role (id, name, code, sort, status, type, data_scope, remark,
                         creator, create_time, updater, update_time, deleted, tenant_id)
VALUES (1, 'Super Admin', 'super_admin', 1, 0, 1, 1, 'Built-in super admin role',
        1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, false, 1);

-- 5. Bind admin user to super_admin role
INSERT INTO system_user_role (id, user_id, role_id,
                              creator, create_time, updater, update_time, deleted, tenant_id)
VALUES (1, 1, 1,
        1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, false, 1);

-- 6. Default OAuth2 client (for login, no tenant — global config)
INSERT INTO system_oauth2_client (id, client_id, secret, name, logo, status,
                                  access_token_validity_seconds, refresh_token_validity_seconds,
                                  redirect_uris, authorized_grant_types,
                                  scopes, auto_approve_scopes, authorities, resource_ids,
                                  creator, create_time, updater, update_time, deleted)
VALUES (1, 'default', 'admin123', 'Soar', '', 0,
        1800, 2592000,
        '["http://localhost:5173"]', '["password","authorization_code","implicit","refresh_token"]',
        '[]', '[]', '[]', '[]',
        1, CURRENT_TIMESTAMP, 1, CURRENT_TIMESTAMP, false);

-- =====================================================================
-- Reset sequences after explicit ID inserts
-- =====================================================================
SELECT setval('system_tenant_id_seq',         (SELECT COALESCE(MAX(id), 1) FROM system_tenant));
SELECT setval('system_dept_id_seq',           (SELECT COALESCE(MAX(id), 1) FROM system_dept));
SELECT setval('system_users_id_seq',          (SELECT COALESCE(MAX(id), 1) FROM system_users));
SELECT setval('system_role_id_seq',           (SELECT COALESCE(MAX(id), 1) FROM system_role));
SELECT setval('system_user_role_id_seq',      (SELECT COALESCE(MAX(id), 1) FROM system_user_role));
SELECT setval('system_oauth2_client_id_seq',  (SELECT COALESCE(MAX(id), 1) FROM system_oauth2_client));