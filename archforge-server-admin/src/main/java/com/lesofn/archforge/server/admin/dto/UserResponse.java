package com.lesofn.archforge.server.admin.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.jspecify.annotations.Nullable;
import com.lesofn.archforge.user.api.domain.SysUser;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户信息DTO
 *
 * @author lesofn
 */
@Data
@NoArgsConstructor
@SuppressWarnings("NullAway.Init")
public class UserResponse {

    /** 用户ID */
    private @Nullable Long userId;

    /** 角色ID */
    private @Nullable Long roleId;

    /** 角色名称 */
    private @Nullable String roleName;

    /** 部门ID */
    private @Nullable Long deptId;

    /** 部门名称 */
    private @Nullable String deptName;

    /** 用户名 */
    private @Nullable String username;

    /** 用户昵称 */
    private @Nullable String nickname;

    /** 用户类型 */
    private @Nullable Integer userType;

    /** 邮件 */
    private @Nullable String email;

    /** 号码 */
    private @Nullable String phoneNumber;

    /** 性别 (0=女, 1=男, 2=未知) */
    private @Nullable Integer sex;

    /** 用户头像 */
    private @Nullable String avatar;

    /** 状态 (0=正常, 1=停用) */
    private @Nullable Integer status;

    /** 最后登录IP */
    private @Nullable String loginIp;

    /** 最后登录时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private @Nullable LocalDateTime loginDate;

    /** 创建者ID */
    private @Nullable Long creatorId;

    /** 创建者名称 */
    private @Nullable String creatorName;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private @Nullable LocalDateTime createTime;

    /** 修改者ID */
    private @Nullable Long updaterId;

    /** 修改者名称 */
    private @Nullable String updaterName;

    /** 修改时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private @Nullable LocalDateTime updateTime;

    /** 备注 */
    private @Nullable String remark;

    /** 从SysUser实体构建UserResponse */
    public UserResponse(@Nullable SysUser user) {
        if (user != null) {
            // 基本信息
            this.userId = user.getUserId();
            this.username = user.getUsername();
            this.nickname = user.getNickname();
            this.userType = user.getUserType();
            this.email = user.getEmail();
            this.phoneNumber = user.getPhoneNumber();
            this.sex = user.getSex().getValue();
            this.avatar = user.getAvatar();
            this.status = user.getStatus();
            this.loginIp = user.getLoginIp();
            this.loginDate = user.getLoginDate();
            this.createTime = user.getCreateTime();
            this.updateTime = user.getUpdateTime();
            this.remark = user.getRemark();

            // 部门ID
            this.deptId = user.getDeptId();

            // 角色ID
            this.roleId = user.getRoleId();

            // 创建者和修改者ID
            this.creatorId = user.getCreatorId();
            this.updaterId = user.getUpdaterId();

            // 注意：部门名称、角色名称、创建者名称、修改者名称需要额外查询获取
            // 这些字段暂时保留为null，需要在业务层通过额外查询填充
        }
    }

    /** 静态工厂方法，从SysUser创建UserResponse */
    public static UserResponse fromEntity(SysUser user) {
        return new UserResponse(user);
    }
}
