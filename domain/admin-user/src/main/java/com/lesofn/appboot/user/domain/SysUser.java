package com.lesofn.appboot.user.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
@Entity
@Table(name = "sys_user")
@DynamicInsert
@DynamicUpdate
public class SysUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "post_id")
    private Long postId;

    @Column(name = "role_id")
    private Long roleId;

    @Column(name = "dept_id")
    private Long deptId;

    @Column(name = "username", nullable = false, length = 64)
    private String username;

    @Column(name = "nickname", nullable = false, length = 32)
    private String nickname;

    @Column(name = "user_type")
    private Integer userType;

    @Column(name = "email", length = 128)
    private String email;

    @Column(name = "phone_number", length = 18)
    private String phoneNumber;

    @Column(name = "sex")
    private Integer sex;

    @Column(name = "avatar", length = 512)
    private String avatar;

    @Column(name = "password", nullable = false, length = 128)
    private String password;

    @Column(name = "status", nullable = false)
    private Integer status;

    @Column(name = "login_ip", length = 128)
    private String loginIp;

    @Column(name = "login_date")
    private LocalDateTime loginDate;

    @Column(name = "is_admin", nullable = false)
    private Boolean isAdmin;

    @Column(name = "creator_id")
    private Long creatorId;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @Column(name = "updater_id")
    private Long updaterId;

    @Column(name = "update_time")
    private LocalDateTime updateTime;

    @Column(name = "remark", length = 512)
    private String remark;

    @Column(name = "deleted", nullable = false)
    private Boolean deleted;
}