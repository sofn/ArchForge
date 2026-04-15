package com.lesofn.archsmith.user.domain;

import com.lesofn.archsmith.common.enums.common.GenderEnum;
import com.lesofn.archsmith.common.repository.BaseEntity;
import com.lesofn.archsmith.common.repository.converter.JpaValueEnumType;
import com.lesofn.archsmith.common.sensitive.Sensitive;
import com.lesofn.archsmith.common.sensitive.SensitiveType;
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

    private Long postId;

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
}
