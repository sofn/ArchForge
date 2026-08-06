package com.lesofn.archforge.meta.table.internal.service;

import com.lesofn.archforge.meta.table.api.domain.MetaColumn;
import com.lesofn.archforge.meta.table.api.domain.MetaColumnType;
import com.lesofn.archforge.meta.table.internal.ddl.SqlIdentifier;
import java.util.ArrayList;
import java.util.List;

/**
 * 构建 REFERENCE 类型字段的列表/导出查询 SELECT 列与 JOIN 子句。
 */
public final class ReferenceDisplayBuilder {

    private ReferenceDisplayBuilder() {
    }

    /**
     * 构建 SELECT 列表，主表字段带别名，REFERENCE 字段额外返回 columnCode_display。
     */
    public static List<String> buildSelectColumns(List<MetaColumn> columns, String mainAlias) {
        List<String> result = new ArrayList<>();
        result.add(quoteAlias(mainAlias, "id"));
        for (MetaColumn column : columns) {
            result.add(quoteAlias(mainAlias, column.getColumnCode()));
            if (isReference(column)) {
                String displayExpr = buildDisplayExpression(column, mainAlias);
                result.add(displayExpr + " AS " + SqlIdentifier.quote(column.getColumnCode() + "_display"));
            }
        }
        result.add(quoteAlias(mainAlias, "creator_id"));
        result.add(quoteAlias(mainAlias, "create_time"));
        result.add(quoteAlias(mainAlias, "updater_id"));
        result.add(quoteAlias(mainAlias, "update_time"));
        result.add(quoteAlias(mainAlias, "deleted"));
        return result;
    }

    /**
     * 构建 LEFT JOIN 子句。
     */
    public static List<String> buildJoins(List<MetaColumn> columns, String mainAlias) {
        List<String> joins = new ArrayList<>();
        for (MetaColumn column : columns) {
            if (!isReference(column)) {
                continue;
            }
            String refAlias = refAlias(column.getColumnCode());
            String refTable = SqlIdentifier.quote(column.getReferenceTable());
            String refColumn = SqlIdentifier.quote(column.getReferenceColumn());
            String mainColumn = quoteAlias(mainAlias, column.getColumnCode());
            joins.add("LEFT JOIN " + refTable + " " + refAlias + " ON " + refAlias + "." + refColumn + " = " + mainColumn);
        }
        return joins;
    }

    /**
     * 构建 REFERENCE 字段显示表达式（含 join 别名）。
     */
    public static String buildDisplayExpression(MetaColumn column, String mainAlias) {
        if (!isReference(column)) {
            return null;
        }
        String displayExpression = column.getDisplayExpression();
        if (displayExpression == null || displayExpression.isBlank()) {
            return quoteAlias(mainAlias, column.getColumnCode());
        }
        String refAlias = refAlias(column.getColumnCode());
        return displayExpression.replaceAll("(?i)\\bref\\.", refAlias + ".");
    }

    public static String refAlias(String columnCode) {
        return "ref_" + columnCode;
    }

    private static boolean isReference(MetaColumn column) {
        return column.getDataType() == MetaColumnType.REFERENCE;
    }

    private static String quoteAlias(String alias, String column) {
        return alias + "." + SqlIdentifier.quote(column);
    }
}
