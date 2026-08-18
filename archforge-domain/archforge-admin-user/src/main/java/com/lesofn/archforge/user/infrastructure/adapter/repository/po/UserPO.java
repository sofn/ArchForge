package com.lesofn.archforge.user.infrastructure.adapter.repository.po;

import com.lesofn.archforge.common.persistence.BasePO;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.io.Serial;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * 用户持久化对象。
 *
 * <p>
 * 映射 {@code sys_user} 表，字段均为基本类型，不含领域值对象。
 */
@Setter
@Getter
@Entity
@Table(name = "sys_user")
@AttributeOverride(name = "id", column = @Column(name = "user_id"))
public class UserPO extends BasePO {

    @Serial
    private static final long serialVersionUID = 1L;

    @Column(name = "role_id")
    private Long roleId;

    @Column(name = "dept_id")
    private Long deptId;

    @Column(name = "username", length = 64, nullable = false)
    private String username;

    @Column(name = "nickname", length = 32)
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

    @Column(name = "password", length = 128, nullable = false)
    private String password;

    @Column(name = "status", nullable = false)
    private Integer status;

    @Column(name = "login_ip", length = 128)
    private String loginIp;

    @Column(name = "login_date")
    private LocalDateTime loginDate;

    @Column(name = "is_admin")
    private Boolean isAdmin;

    @Column(name = "remark", length = 512)
    private String remark;
}
