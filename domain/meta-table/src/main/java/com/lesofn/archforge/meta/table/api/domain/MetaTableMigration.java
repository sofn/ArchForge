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
 * 元表格 Schema 迁移记录。
 */
@Setter
@Getter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "sys_meta_table_migration")
@DynamicInsert
@DynamicUpdate
public class MetaTableMigration extends BasePO {

    private static final long serialVersionUID = 1L;

    @Column(name = "table_id", nullable = false)
    private Long tableId;

    @Column(name = "version", nullable = false)
    private Integer version;

    @Column(name = "change_type", nullable = false, length = 32)
    private String changeType;

    @Column(name = "column_code", length = 64)
    private String columnCode;

    @Column(name = "old_column_code", length = 64)
    private String oldColumnCode;

    @Column(name = "old_type", length = 64)
    private String oldType;

    @Column(name = "new_type", length = 64)
    private String newType;

    @Column(name = "old_default", length = 255)
    private String oldDefault;

    @Column(name = "new_default", length = 255)
    private String newDefault;

    @Column(name = "ddl_sql", nullable = false, columnDefinition = "TEXT")
    private String ddlSql;

    @Column(name = "status", nullable = false, length = 16)
    private String status = "PENDING";

    @Column(name = "executed_at")
    private java.time.LocalDateTime executedAt;
}
