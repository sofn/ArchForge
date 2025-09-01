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
@Table(name = "sys_role")
@DynamicInsert
@DynamicUpdate
public class SysRole {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "role_id")
    private Long roleId;

    @Column(name = "role_name", nullable = false, length = 32)
    private String roleName;

    @Column(name = "role_key", nullable = false, length = 128)
    private String roleKey;

    @Column(name = "role_sort", nullable = false)
    private Integer roleSort;

    @Column(name = "data_scope")
    private Short dataScope;

    @Column(name = "dept_id_set", length = 1024)
    private String deptIdSet;

    @Column(name = "status", nullable = false)
    private Short status;

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