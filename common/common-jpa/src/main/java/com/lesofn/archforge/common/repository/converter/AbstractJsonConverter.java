package com.lesofn.archforge.common.repository.converter;

import com.lesofn.archforge.common.utils.jackson.JsonUtil;
import jakarta.persistence.AttributeConverter;
import org.springframework.util.StringUtils;

/**
 * 通用JSON转换器基类 使用方式：为每个需要转换的类型创建一个具体的Converter类 @Converter public class MetaInfoConverter extends
 * AbstractJsonConverter<MetaDTO> {}
 *
 * <p>
 * 然后在entity字段上使用： @Convert(converter = MetaInfoConverter.class) @Column(columnDefinition =
 * "TEXT") private MetaDTO metaInfo;
 */
public abstract class AbstractJsonConverter<T> implements AttributeConverter<T, String> {

    @Override
    public String convertToDatabaseColumn(T attribute) {
        if (attribute == null) {
            return null;
        }
        return JsonUtil.to(attribute);
    }

    @Override
    public T convertToEntityAttribute(String dbData) {
        if (!StringUtils.hasText(dbData)) {
            return null;
        }
        return JsonUtil.from(dbData, getTargetType());
    }

    /** 子类必须实现此方法，返回目标类型 */
    protected abstract Class<T> getTargetType();
}
