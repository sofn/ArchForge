package com.lesofn.archforge.meta.table.api.domain;

import com.lesofn.archforge.common.persistence.BasePO;
import org.jspecify.annotations.Nullable;
import com.lesofn.archforge.meta.table.api.domain.convert.OptionListConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

/**
 * 元表格字段定义。
 */
@Setter
@Getter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "sys_meta_table_column")
@DynamicInsert
@DynamicUpdate
public class MetaColumn extends BasePO {

    private static final long serialVersionUID = 1L;

    @Column(name = "table_id", nullable = false)
    private @Nullable Long tableId;

    @Column(name = "column_code", nullable = false)
    private String columnCode;

    @Column(name = "column_name", nullable = false)
    private String columnName;

    @Enumerated(EnumType.STRING)
    @Column(name = "data_type", nullable = false, columnDefinition = "VARCHAR(32)")
    private MetaColumnType dataType;

    @Column(name = "length")
    private @Nullable Integer length;

    @Column(name = "precision")
    private Integer precision;

    @Column(name = "scale")
    private Integer scale;

    @Column(name = "nullable")
    private Boolean nullable;

    @Column(name = "default_value")
    private @Nullable String defaultValue;

    @Column(name = "is_unique")
    private Boolean unique;

    @Column(name = "is_required")
    private Boolean required;

    @Column(name = "is_searchable")
    private Boolean searchable;

    @Column(name = "is_list_visible")
    private Boolean listVisible;

    @Column(name = "is_index")
    private Boolean index;

    @Column(name = "sort")
    private Integer sort;

    @Convert(converter = OptionListConverter.class)
    @Column(name = "options")
    private List<OptionItem> options;

    @Column(name = "reference_table")
    private String referenceTable;

    @Column(name = "reference_column")
    private String referenceColumn;

    @Column(name = "display_expression")
    private String displayExpression;

    @Column(name = "tenant_column")
    private Boolean tenantColumn;

    @Column(name = "owner_column")
    private Boolean ownerColumn;

    @Column(name = "index_type")
    private String indexType;

    @Column(name = "index_group")
    private String indexGroup;

    @Column(name = "array_element_type")
    private String arrayElementType;

    @Column(name = "search_type")
    private String searchType;

    @Column(name = "dict_code")
    private String dictCode;

    public boolean isUniqueColumn() { return Boolean.TRUE.equals(unique); }

    public boolean isIndexedColumn() { return Boolean.TRUE.equals(index); }

    /**
     * 物理列可空性（DDL 真源）：显式配置了 nullable 就用它；
     * 未配置时回退为 !required（存量行兼容，必填即 NOT NULL）。
     * required 只承担表单级必填校验，不再直接决定 DDL。
     */
    public boolean isNullableColumn() {
        return nullable == null ? !Boolean.TRUE.equals(required) : Boolean.TRUE.equals(nullable);
    }

    public boolean isListVisibleColumn() { return Boolean.TRUE.equals(listVisible); }

    public boolean isSearchableColumn() { return Boolean.TRUE.equals(searchable); }

    public boolean isTenantColumn() { return Boolean.TRUE.equals(tenantColumn); }

    public boolean isOwnerColumn() { return Boolean.TRUE.equals(ownerColumn); }
}
