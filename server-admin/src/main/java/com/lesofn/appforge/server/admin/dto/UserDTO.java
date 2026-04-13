package com.lesofn.appforge.server.admin.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.lesofn.appforge.user.domain.SysUser;
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
public class UserDTO {

    /** 用户ID */
    private Long userId;

    /** 职位ID */
    private Long postId;

    /** 职位名称 */
    private String postName;

    /** 角色ID */
    private Long roleId;

    /** 角色名称 */
    private String roleName;

    /** 部门ID */
    private Long deptId;

    /** 部门名称 */
    private String deptName;

    /** 用户名 */
    private String username;

    /** 用户昵称 */
    private String nickname;

    /** 用户类型 */
    private Integer userType;

    /** 邮件 */
    private String email;

    /** 号码 */
    private String phoneNumber;

    /** 性别 (0=女, 1=男, 2=未知) */
    private Integer sex;

    /** 用户头像 */
    private String avatar;

    /** 状态 (0=正常, 1=停用) */
    private Integer status;

    /** 最后登录IP */
    private String loginIp;

    /** 最后登录时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime loginDate;

    /** 创建者ID */
    private Long creatorId;

    /** 创建者名称 */
    private String creatorName;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /** 修改者ID */
    private Long updaterId;

    /** 修改者名称 */
    private String updaterName;

    /** 修改时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    /** 备注 */
    private String remark;

    /** 从SysUser实体构建UserDTO */
    public UserDTO(SysUser user) {
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

            // 职位ID
            this.postId = user.getPostId();

            // 部门ID
            this.deptId = user.getDeptId();

            // 角色ID
            this.roleId = user.getRoleId();

            // 创建者和修改者ID
            this.creatorId = user.getCreatorId();
            this.updaterId = user.getUpdaterId();

            // 注意：部门名称、角色名称、职位名称、创建者名称、修改者名称需要额外查询获取
            // 这些字段暂时保留为null，需要在业务层通过额外查询填充
        }
    }

    /** 静态工厂方法，从SysUser创建UserDTO */
    public static UserDTO fromEntity(SysUser user) {
        return new UserDTO(user);
    }
}
