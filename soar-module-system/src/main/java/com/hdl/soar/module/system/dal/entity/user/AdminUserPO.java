package com.hdl.soar.module.system.dal.entity.user;

import com.hdl.soar.framework.jpa.core.converter.JsonLongSetConverter;
import com.hdl.soar.framework.tenant.core.db.TenantBasePO;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import com.hdl.soar.framework.common.enums.CommonStatusEnum;
import com.hdl.soar.module.system.enums.common.SexEnum;

import java.time.Instant;
import java.util.Set;

/**
 * Admin user entity.
 *
 * <p>Table name uses {@code system_users} (not {@code system_user})
 * because {@code system_user} is a reserved keyword in SQL Server.
 */
@Entity
@Table(name = "system_users")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserPO extends TenantBasePO {

    /**
     * User ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Username
     */
    @Column(name = "username", nullable = false)
    private String username;

    /**
     * Encrypted password
     *
     * Uses {@link BCryptPasswordEncoder}, so salt handling is not required.
     */
    @Column(name = "password", nullable = false)
    private String password;

    /**
     * User nickname
     */
    @Column(name = "nickname")
    private String nickname;


    /**
     * Remark / note
     */
    @Column(name = "remark")
    private String remark;

    /**
     * Department ID
     */
    @Column(name = "dept_id")
    private Long deptId;

    /**
     * Post/role IDs stored as JSON array in DB (e.g., {@code [1, 2, 3]}).
     * Converted by {@link com.hdl.soar.framework.jpa.core.converter.JsonLongSetConverter}.
     */
    @Convert(converter = JsonLongSetConverter.class)
    @Column(name = "post_ids")
    private Set<Long> postIds;

    /**
     * Email address
     */
    @Column(name = "email")
    private String email;

    /**
     * Mobile number
     */
    @Column(name = "mobile")
    private String mobile;

    /**
     * User gender
     * <p></p>
     * See {@link SexEnum}
     */
    @Column(name = "sex")
    private Integer sex;

    /**
     * Avatar URL
     */
    @Column(name = "avatar")
    private String avatar;

    /**
     * Account status
     *
     * <p>See {@link CommonStatusEnum}
     */
    @Column(name = "status", nullable = false)
    private Integer status;

    /**
     * Last login IP
     */
    @Column(name = "login_ip")
    private String loginIp;

    /**
     * Last login time
     */
    @Column(name = "login_date")
    private Instant loginDate;

}
