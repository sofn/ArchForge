package com.lesofn.archforge.meta.table.internal.generator.handler;

import com.lesofn.archforge.meta.table.api.domain.MetaColumn;
import com.lesofn.archforge.meta.table.api.domain.MetaColumnType;
import com.lesofn.archforge.meta.table.internal.generator.CodeGenColumn;
import com.lesofn.archforge.meta.table.internal.generator.CodeGenTypeRegistry;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

class CodeGenTypeHandlerTest {

    private final DefaultCodeGenTypeHandler handler = new DefaultCodeGenTypeHandler();
    private final CodeGenTypeRegistry registry = new CodeGenTypeRegistry();

    @Test
    void shouldEnrichUuidColumn() {
        CodeGenColumn col = new CodeGenColumn();
        col.setFieldName("uuidCol");
        col.setColumnName("UUID列");
        col.setRequired(true);
        MetaColumn meta = new MetaColumn();
        meta.setDataType(MetaColumnType.UUID);
        handler.enrich(col, meta);

        assertThat(col.getJavaType()).isEqualTo("UUID");
        assertThat(col.getTsType()).isEqualTo("string");
        assertThat(col.getJpaAnnotations()).contains("@JdbcTypeCode(SqlTypes.UUID)");
        assertThat(col.getJpaAnnotations()).contains("@Column(nullable = false)");
        assertThat(col.getValidatorAnnotations()).contains("@NotNull(message = \"UUID列不能为空\")");
    }

    @Test
    void shouldEnrichArrayColumn() {
        CodeGenColumn col = new CodeGenColumn();
        col.setFieldName("tags");
        col.setColumnName("标签");
        col.setRequired(false);
        MetaColumn meta = new MetaColumn();
        meta.setDataType(MetaColumnType.ARRAY);
        meta.setArrayElementType("INTEGER");
        handler.enrich(col, meta);

        assertThat(col.getJavaType()).isEqualTo("Long[]");
        assertThat(col.getTsType()).isEqualTo("string[]");
        assertThat(col.getJpaAnnotations()).contains("@JdbcTypeCode(SqlTypes.ARRAY)");
    }

    @Test
    void shouldEnrichGeoColumn() {
        CodeGenColumn col = new CodeGenColumn();
        col.setFieldName("location");
        col.setColumnName("位置");
        col.setRequired(true);
        MetaColumn meta = new MetaColumn();
        meta.setDataType(MetaColumnType.GEO);
        handler.enrich(col, meta);

        assertThat(col.getJavaType()).isEqualTo("String");
        assertThat(col.getJpaAnnotations()).contains("@Type(JsonbStringUserType.class)");
        assertThat(col.getJpaAnnotations()).contains("@Column(nullable = false, columnDefinition = \"jsonb\")");
    }

    @Test
    void shouldResolveAllTypesViaRegistry() {
        for (MetaColumnType type : MetaColumnType.values()) {
            MetaColumn meta = new MetaColumn();
            meta.setDataType(type);
            CodeGenColumn col = new CodeGenColumn();
            col.setFieldName("col");
            col.setColumnName("列");
            registry.resolve(type).enrich(col, meta);
            assertThat(col.getJavaType()).isNotNull();
            assertThat(col.getTsType()).isNotNull();
        }
    }
}
