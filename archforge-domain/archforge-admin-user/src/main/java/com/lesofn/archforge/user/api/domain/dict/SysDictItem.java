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
@Table(name = "sys_dict_item")
@DynamicInsert
@DynamicUpdate
public class SysDictItem extends BaseEntity<SysDictItem> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long dictItemId;

    @Column(name = "dict_type_id", nullable = false)
    private Long dictTypeId;

    @Column(name = "item_code", length = 64, nullable = false)
    private String itemCode;

    @Column(name = "item_label", length = 128, nullable = false)
    private String itemLabel;

    @Column(name = "sort")
    private Integer sort;

    /** 状态：1 启用，0 禁用 */
    @Column(name = "status")
    private Integer status;
}
