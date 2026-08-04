package com.lesofn.archforge.meta.table.internal.generator;

import com.lesofn.archforge.meta.table.api.domain.MetaColumn;
import com.lesofn.archforge.meta.table.api.domain.MetaColumnType;

public interface CodeGenTypeHandler {

    boolean supports(MetaColumnType type);

    void enrich(CodeGenColumn column, MetaColumn metaColumn);
}
