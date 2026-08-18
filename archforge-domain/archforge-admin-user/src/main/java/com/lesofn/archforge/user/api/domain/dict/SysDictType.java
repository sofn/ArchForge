package com.lesofn.archforge.user.api.domain.dict;

import com.lesofn.archforge.common.repository.BaseEntity;
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
@Table(name = "sys_dict_type")
@DynamicInsert
@DynamicUpdate
public class SysDictType extends BaseEntity<SysDictType> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long dictTypeId;

    @Column(name = "dict_code", length = 64, nullable = false, unique = true)
    private String dictCode;

    @Column(name = "dict_name", length = 128, nullable = false)
    private String dictName;

    @Column(name = "description", length = 255)
    private String description;

    /** 状态：1 启用，0 禁用 */
    @Column(name = "status")
    private Integer status;

    @Column(name = "sort")
    private Integer sort;
}
