package com.lesofn.appforge.user.domain;

import com.lesofn.appforge.common.repository.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

@Setter
@Getter
@Accessors(chain = true)
@Entity
@Table(name = "sys_role")
@DynamicInsert
@DynamicUpdate
public class SysRole extends BaseEntity<SysRole> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long roleId;

    private String roleName;

    private String roleKey;

    private Integer roleSort;

    private Short dataScope;

    private String deptIdSet;

    private Short status;

    private String remark;
}