package com.lesofn.archsmith.user.domain;

import com.lesofn.archsmith.common.repository.BaseEntity;
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
@Table(name = "sys_config")
@DynamicInsert
@DynamicUpdate
public class SysConfig extends BaseEntity<SysConfig> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long configId;

    @Column(length = 128)
    private String configName;

    @Column(length = 128)
    private String configKey;

    @Column(length = 512)
    private String configValue;

    /** 系统内置（1是 0否） */
    private Integer configType;

    @Column(length = 512)
    private String remark;
}
