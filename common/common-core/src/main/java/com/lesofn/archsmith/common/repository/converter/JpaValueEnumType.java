package com.lesofn.archsmith.common.repository.converter;

import com.lesofn.archsmith.common.enums.BasicEnum;
import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Properties;
import org.hibernate.type.descriptor.WrapperOptions;
import org.hibernate.usertype.DynamicParameterizedType;
import org.hibernate.usertype.UserType;

/**
 * @author sofn
 * @version 1.0 Created at: 2021-01-29 14:53 Updated for Hibernate 7.x compatibility
 */
@SuppressWarnings({"rawtypes", "unchecked", "removal"})
public class JpaValueEnumType implements DynamicParameterizedType, UserType<Enum> {

    private Class<Enum> enumClass;

    @Override
    public int getSqlType() {
        return Types.INTEGER;
    }

    @Override
    public Class<Enum> returnedClass() {
        return enumClass;
    }

    @Override
    public boolean equals(Enum x, Enum y) {
        return x == y;
    }

    @Override
    public int hashCode(Enum x) {
        return x == null ? 0 : x.hashCode();
    }

    @Override
    public Enum nullSafeGet(ResultSet rs, int position, WrapperOptions options)
            throws SQLException {
        Integer value = rs.getObject(position, Integer.class);
        if (value == null) {
            return null;
        }

        for (Enum enumValue : returnedClass().getEnumConstants()) {
            if (enumValue instanceof BasicEnum basicEnum) {
                if (basicEnum.getValue() == value) {
                    return enumValue;
                }
            }
        }
        throw new IllegalStateException(
                "Unknown " + returnedClass().getSimpleName() + " value: " + value);
    }

    @Override
    public void nullSafeSet(PreparedStatement st, Enum value, int index, WrapperOptions options)
            throws SQLException {
        if (value == null) {
            st.setNull(index, Types.INTEGER);
        } else {
            st.setInt(index, ((BasicEnum) value).getValue());
        }
    }

    @Override
    public Enum deepCopy(Enum value) {
        return value; // Enums are immutable
    }

    @Override
    public boolean isMutable() {
        return false;
    }

    @Override
    public Serializable disassemble(Enum value) {
        return value;
    }

    @Override
    public Enum assemble(Serializable cached, Object owner) {
        return (Enum) cached;
    }

    @Override
    public Enum replace(Enum detached, Enum managed, Object owner) {
        return detached;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void setParameterValues(Properties parameters) {
        ParameterType params =
                (ParameterType) parameters.get(DynamicParameterizedType.PARAMETER_TYPE);
        enumClass = (Class<Enum>) params.getReturnedClass();
    }
}
