package com.hdl.soar.module.system.dal.entity.dept;

import com.hdl.soar.framework.jpa.core.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import com.hdl.soar.module.system.dal.entity.user.AdminUserEntity;

@Entity
@Table(name = "system_user_post")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPostEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * User ID
     * <p>
     * References {@link AdminUserEntity#getId()}</p>
     */
    @Column(name = "user_id")
    private Long userId;

    /**
     * Position ID
     * <p>
     * References {@link PostEntity#getId()}</p>
     */
    @Column(name = "post_id")
    private Long postId;

}
