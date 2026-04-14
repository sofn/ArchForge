package com.lesofn.appforge.user.domain;

import com.lesofn.appforge.common.repository.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

/**
 * 部门实体
 *
 * @author sofn
 */
@Setter
@Getter
@Accessors(chain = true)
@Entity
@Table(name = "sys_dept")
@DynamicInsert
@DynamicUpdate
public class SysDept extends BaseEntity<SysDept> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long deptId;

    private Long parentId;

    @Column(length = 64)
    private String name;

    @Column(length = 64)
    private String principal;

    @Column(length = 18)
    private String phone;

    @Column(length = 128)
    private String email;

    private Integer sort;

    private Integer status;

    private Integer type;

    @Column(length = 512)
    private String remark;
}
