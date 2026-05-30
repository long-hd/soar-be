package com.hdl.soar.module.system.controller.admin.user.dto.profile;

import com.hdl.soar.module.system.controller.admin.dept.dto.dept.DeptSimpleRespDTO;
import com.hdl.soar.module.system.controller.admin.dept.dto.post.PostSimpleRespDTO;
import com.hdl.soar.module.system.controller.admin.permission.dto.role.RoleSimpleRespDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
@Schema(description = "Admin Backend - User Profile Response DTO")
public class UserProfileRespDTO {

    @Schema(description = "User ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "Username", requiredMode = Schema.RequiredMode.REQUIRED, example = "admin")
    private String username;

    @Schema(description = "Nickname", example = "Long")
    private String nickname;

    @Schema(description = "Email", example = "user@example.com")
    private String email;

    @Schema(description = "Mobile", example = "0912345678")
    private String mobile;

    @Schema(description = "Sex: 1=Male, 2=Female, 3=Unknown", example = "1")
    private Integer sex;

    @Schema(description = "Avatar URL")
    private String avatar;

    @Schema(description = "Last login IP", example = "192.168.1.1")
    private String loginIp;

    @Schema(description = "Last login time")
    private Instant loginDate;

    @Schema(description = "Creation time")
    private Instant createTime;

    @Schema(description = "Roles assigned to this user")
    private List<RoleSimpleRespDTO> roles;

    @Schema(description = "Department this user belongs to")
    private DeptSimpleRespDTO dept;

    @Schema(description = "Posts assigned to this user")
    private List<PostSimpleRespDTO> posts;

}
