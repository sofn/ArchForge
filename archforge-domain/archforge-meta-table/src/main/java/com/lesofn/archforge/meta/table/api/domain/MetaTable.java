package com.lesofn.archforge.meta.table.api.domain;

import com.lesofn.archforge.common.persistence.BasePO;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

/**
 * 元表格定义。
 */
@Setter
@Getter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "sys_meta_table")
@DynamicInsert
@DynamicUpdate
public class MetaTable extends BasePO {

    private static final long serialVersionUID = 1L;

    @Column(name = "table_code", nullable = false)
    private String tableCode;

    @Column(name = "table_name", nullable = false)
    private String tableName;

    @Column(name = "description")
    private String description;

    @Column(name = "table_prefix")
    private String tablePrefix;

    @Column(name = "status")
    private Integer status;

    @Column(name = "schema_version")
    private Integer schemaVersion = 1;

    public String physicalTableName() {
        String prefix = tablePrefix == null ? "meta_" : tablePrefix;
        return prefix + tableCode;
    }

    public boolean isEnabled() { return status != null && status == 1; }
}
