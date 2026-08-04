package com.lesofn.archforge.common.repository.converter;

import com.lesofn.archforge.common.utils.jackson.JsonUtil;
import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import org.hibernate.type.descriptor.WrapperOptions;
import org.hibernate.usertype.UserType;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.postgresql.util.PGobject;

/**
 * 将 Java {@link String} 与 PostgreSQL {@code jsonb} 类型互转的 Hibernate {@link UserType}。
 * 存储前会校验字符串是否为合法 JSON，并通过 {@link Types#OTHER} 正确绑定 {@link PGobject}。
 */
@NullMarked
public class JsonbStringUserType implements UserType<String> {

    @Override
    public int getSqlType() { return Types.OTHER; }

    @Override
    public Class<String> returnedClass() {
        return String.class;
    }

    @Override
    public boolean equals(String x, String y) {
        if (x == null && y == null) {
            return true;
        }
        if (x == null || y == null) {
            return false;
        }
        return x.equals(y);
    }

    @Override
    public int hashCode(String x) {
        return x == null ? 0 : x.hashCode();
    }

    @Override
    public @Nullable String nullSafeGet(ResultSet rs, int position, WrapperOptions options)
            throws SQLException {
        PGobject value = rs.getObject(position, PGobject.class);
        if (value == null) {
            return null;
        }
        return value.getValue();
    }

    @Override
    public void nullSafeSet(PreparedStatement st, @Nullable String value, int index, WrapperOptions options)
            throws SQLException {
        if (value == null) {
            st.setNull(index, Types.OTHER);
            return;
        }
        validateJson(value);
        PGobject pgObject = new PGobject();
        pgObject.setType("jsonb");
        pgObject.setValue(value);
        st.setObject(index, pgObject, Types.OTHER);
    }

    @Override
    public String deepCopy(String value) {
        return value;
    }

    @Override
    public boolean isMutable() { return false; }

    @Override
    public Serializable disassemble(String value) {
        return value;
    }

    @Override
    public String assemble(Serializable cached, Object owner) {
        return (String) cached;
    }

    @Override
    public String replace(String detached, String managed, Object owner) {
        return detached;
    }

    private void validateJson(String json) {
        try {
            JsonUtil.getObjectMapper().readTree(json);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid JSON value: " + json, e);
        }
    }
}
