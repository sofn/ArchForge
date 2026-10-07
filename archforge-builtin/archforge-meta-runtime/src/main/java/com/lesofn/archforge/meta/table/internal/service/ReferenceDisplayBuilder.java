package com.lesofn.archforge.meta.table.internal.service;

import com.lesofn.archforge.meta.table.api.domain.MetaColumn;
import org.jspecify.annotations.Nullable;
import com.lesofn.archforge.meta.table.api.domain.MetaColumnType;
import com.lesofn.archforge.meta.table.api.errors.MetaTableException;
import com.lesofn.archforge.meta.table.internal.util.DisplayExpression;
import com.lesofn.archforge.meta.table.internal.util.SqlIdentifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;

/**
 * 构建 REFERENCE 类型字段的列表/导出查询 SELECT 列与 JOIN 子句。
 */
@Slf4j
public final class ReferenceDisplayBuilder {

    private static final Set<String> WARNED = ConcurrentHashMap.newKeySet();

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
     * 构建 REFERENCE 字段显示表达式（含 join 别名）。表达式按 {@link DisplayExpression} 白名单文法解析后重新渲染，
     * 不会把存储的原文直接拼进 SELECT。
     */
    public static @Nullable String buildDisplayExpression(MetaColumn column, String mainAlias) {
        if (!isReference(column)) {
            return null;
        }
        String displayExpression = column.getDisplayExpression();
        if (displayExpression == null || displayExpression.isBlank()) {
            return quoteAlias(mainAlias, column.getColumnCode());
        }
        try {
            return DisplayExpression.render(displayExpression, refAlias(column.getColumnCode()));
        } catch (MetaTableException e) {
            // 存量数据可能绕过过设计期校验：不执行库里的任意 SQL，降级为关联列原值，列表照常可用。
            // 每个进程每条非法表达式只告警一次——列表接口每次请求都会走到这里。
            if (WARNED.add(column.getColumnCode() + "|" + displayExpression)) {
                log.warn("meta column {} has a display expression outside the whitelist grammar - showing the raw value: {}",
                        column.getColumnCode(), e.getMessage());
            }
            return quoteAlias(mainAlias, column.getColumnCode());
        }
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
