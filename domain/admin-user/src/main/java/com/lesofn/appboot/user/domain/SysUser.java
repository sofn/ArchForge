package com.lesofn.appboot.user.domain;

import com.lesofn.appboot.common.repository.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.time.LocalDateTime;

@Setter
@Getter
@Accessors(chain = true)
@Entity
@Table(name = "sys_user")
@DynamicInsert
@DynamicUpdate
public class SysUser extends BaseEntity<SysUser> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    private Long postId;

    private Long roleId;

    private Long deptId;

    private String username;

    private String nickname;

    private Integer userType;

    private String email;

    private String phoneNumber;

    private Integer sex;

    private String avatar;

    private String password;

    private Integer status;

    private String loginIp;

    private LocalDateTime loginDate;

    @Column(columnDefinition = "TINYINT(1)")
    private Boolean isAdmin;

    private String remark;
}