package com.lesofn.archforge.meta.table.internal.schema;

import com.lesofn.archforge.meta.table.api.domain.MetaColumn;
import java.util.List;
import lombok.Data;

/**
 * 一次 Schema 变更。
 */
@Data
public class SchemaChange {

    private SchemaChangeType type;

    private MetaColumn oldColumn;

    private MetaColumn newColumn;

    private String oldType;

    private String newType;

    private String oldDefault;

    private String newDefault;

    private Boolean oldRequired;

    private Boolean newRequired;

    private Boolean oldIndex;

    private Boolean newIndex;

    private Boolean oldUnique;

    private Boolean newUnique;

    private String oldIndexType;

    private String newIndexType;

    private String oldIndexGroup;

    private String newIndexGroup;

    private List<MetaColumn> oldGroupColumns;

    private List<MetaColumn> newGroupColumns;
}
