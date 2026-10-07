package com.lesofn.archforge.meta.table.internal.util;

import com.lesofn.archforge.meta.table.api.errors.MetaTableErrorCode;
import com.lesofn.archforge.meta.table.api.errors.MetaTableException;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * REFERENCE 字段的显示表达式：白名单文法，渲染后直接进入列表/导出的 SELECT 列表。
 *
 * <pre>
 * expression := term ( '||' term )*
 * term       := ref.&lt;column&gt; [ ::text | ::varchar ]  |  'string literal'
 * </pre>
 *
 * <p>
 * 其余一切（函数调用、运算符、括号、子查询、注释……）都拒绝。字符串字面量内容原样保留——它在引号内，
 * 不会被数据库当作 SQL。校验与渲染共用同一解析，所以存量数据即使绕过了设计期校验，运行期也只会输出
 * 白名单内的构造。
 */
public final class DisplayExpression {

    /** 表达式长度上限，防止畸形输入拖慢解析。 */
    private static final int MAX_LENGTH = 512;

    private static final Pattern REF_TERM = Pattern.compile("ref\\.([a-z][a-z0-9_]{0,62})(?:::(text|varchar))?",
            Pattern.CASE_INSENSITIVE);

    private static final Pattern LITERAL = Pattern.compile("'(?:[^']|'')*'");

    private static final String CONCAT = "||";

    private DisplayExpression() {
    }

    /**
     * 校验并渲染为 SQL 片段：{@code ref.col} 变为 {@code <refAlias>."col"}。
     *
     * @throws MetaTableException 表达式不在白名单文法内
     */
    public static String render(String expression, String refAlias) {
        if (expression == null || expression.isBlank()) {
            throw invalid("不能为空");
        }
        if (expression.length() > MAX_LENGTH) {
            throw invalid("长度不能超过 " + MAX_LENGTH);
        }
        int length = expression.length();
        Matcher refTerm = REF_TERM.matcher(expression);
        Matcher literal = LITERAL.matcher(expression);
        StringBuilder sql = new StringBuilder();
        int pos = skipWhitespace(expression, 0);
        while (true) {
            if (refTerm.region(pos, length).lookingAt()) {
                sql.append(refAlias).append('.').append(SqlIdentifier.quote(refTerm.group(1).toLowerCase(Locale.ROOT)));
                if (refTerm.group(2) != null) {
                    sql.append("::").append(refTerm.group(2).toLowerCase(Locale.ROOT));
                }
                pos = refTerm.end();
            } else if (literal.region(pos, length).lookingAt()) {
                sql.append(literal.group());
                pos = literal.end();
            } else {
                throw invalid("第 " + (pos + 1) + " 个字符处应为 ref.列名 或 '字符串'");
            }
            pos = skipWhitespace(expression, pos);
            if (pos == length) {
                return sql.toString();
            }
            if (!expression.startsWith(CONCAT, pos)) {
                throw invalid("第 " + (pos + 1) + " 个字符处只允许 || 连接");
            }
            sql.append(' ').append(CONCAT).append(' ');
            pos = skipWhitespace(expression, pos + CONCAT.length());
        }
    }

    private static int skipWhitespace(String text, int from) {
        int pos = from;
        while (pos < text.length() && Character.isWhitespace(text.charAt(pos))) {
            pos++;
        }
        return pos;
    }

    private static MetaTableException invalid(String reason) {
        return new MetaTableException(MetaTableErrorCode.META_COLUMN_VALUE_INVALID, "显示表达式只允许 ref.列名、'字符串' 与 || 连接（" + reason +
                "）");
    }
}
