package com.lesofn.archforge.user.api.domain;

import com.lesofn.archforge.common.enums.common.GenderEnum;
import com.lesofn.archforge.common.enums.common.UserStatusEnum;
import com.lesofn.archforge.common.repository.BaseEntity;
import com.lesofn.archforge.common.repository.converter.JpaValueEnumType;
import com.lesofn.archforge.common.sensitive.Sensitive;
import com.lesofn.archforge.common.sensitive.SensitiveType;
import com.lesofn.archforge.user.api.errors.AdminUserErrorCode;
import com.lesofn.archforge.user.api.errors.AdminUserException;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Type;

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

    private Long roleId;

    private Long deptId;

    private String username;

    private String nickname;

    private Integer userType;

    @Sensitive(SensitiveType.EMAIL)
    private String email;

    @Sensitive(SensitiveType.PHONE)
    private String phoneNumber;

    @Type(JpaValueEnumType.class)
    private GenderEnum sex;

    private String avatar;

    @Sensitive(SensitiveType.PASSWORD)
    private String password;

    private Integer status;

    @Sensitive(SensitiveType.IP_ADDRESS)
    private String loginIp;

    private LocalDateTime loginDate;

    @Column(name = "is_admin")
    private Boolean isAdmin;

    private String remark;

    /** 用户是否激活 */
    public boolean isActive() { return this.status != null && this.status == 1; }

    /** 用户是否已删除 */
    public boolean isDeleted() { return Boolean.TRUE.equals(this.getDeleted()); }

    /** 用户是否可以登录 */
    public boolean canLogin() {
        return isActive() && !isDeleted();
    }

    /** 校验用户是否可以登录，否则抛出业务异常 */
    public void validateCanLogin() {
        if (!canLogin()) {
            throw new AdminUserException(AdminUserErrorCode.USER_IS_DISABLE, this.username);
        }
    }

    /** 初始化新用户默认状态 */
    public void prepareForCreate(String encodedPassword) {
        changePassword(encodedPassword);
        if (this.status == null) {
            this.status = UserStatusEnum.NORMAL.getValue();
        }
        if (this.isAdmin == null) {
            this.isAdmin = false;
        }
    }

    /** 更新基础资料 */
    public void updateProfile(
            String nickname,
            String phoneNumber,
            String email,
            GenderEnum sex,
            String remark,
            Long deptId) {
        if (nickname != null) {
            this.nickname = nickname;
        }
        if (phoneNumber != null) {
            this.phoneNumber = phoneNumber;
        }
        if (email != null) {
            this.email = email;
        }
        if (sex != null) {
            this.sex = sex;
        }
        if (remark != null) {
            this.remark = remark;
        }
        if (deptId != null) {
            this.deptId = deptId;
        }
    }

    /** 修改密码（传入的必须是已加密的密码） */
    public void changePassword(String encodedPassword) {
        this.password = encodedPassword;
    }

    /** 分配角色 */
    public void assignRole(Long roleId) {
        this.roleId = roleId;
    }

    /** 分配部门 */
    public void assignDept(Long deptId) {
        this.deptId = deptId;
    }

    /** 更新用户状态 */
    public void updateStatus(Integer status) {
        this.status = status;
    }

    /** 启用账户 */
    public void activate() {
        this.status = UserStatusEnum.NORMAL.getValue();
    }

    /** 禁用账户 */
    public void disable() {
        this.status = UserStatusEnum.DISABLED.getValue();
    }

    /** 记录登录信息 */
    public void recordLogin(String loginIp) {
        this.loginIp = loginIp;
        this.loginDate = LocalDateTime.now();
    }

    /** 标记删除（逻辑删除） */
    public void markDeleted() {
        setDeleted(true);
    }
}
