package com.lesofn.appforge.common.repository.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lesofn.appforge.common.spring.SpringContextHolder;
import com.lesofn.appforge.common.utils.jackson.JsonUtil;
import jakarta.persistence.AttributeConverter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

/**
 * 通用JSON转换器基类
 * 使用方式：为每个需要转换的类型创建一个具体的Converter类
 * 
 * @Converter
 * public class MetaInfoConverter extends AbstractJsonConverter<MetaDTO> {}
 * 
 * 然后在entity字段上使用：
 * @Convert(converter = MetaInfoConverter.class)
 * @Column(columnDefinition = "TEXT")
 * private MetaDTO metaInfo;
 */
@Slf4j
public abstract class AbstractJsonConverter<T> implements AttributeConverter<T, String> {

    @Override
    public String convertToDatabaseColumn(T attribute) {
        if (attribute == null) {
            return null;
        }
        try {
            return getObjectMapper().writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            log.error("Error converting object to JSON string: {}", attribute, e);
            throw new RuntimeException("Failed to convert object to JSON", e);
        }
    }

    @Override
    public T convertToEntityAttribute(String dbData) {
        if (!StringUtils.hasText(dbData)) {
            return null;
        }
        try {
            return getObjectMapper().readValue(dbData, getTargetType());
        } catch (JsonProcessingException e) {
            log.error("Error converting JSON string to object: {}", dbData, e);
            throw new RuntimeException("Failed to convert JSON to object", e);
        }
    }

    /**
     * 子类必须实现此方法，返回目标类型
     */
    protected abstract Class<T> getTargetType();

    /**
     * 获取Spring配置的ObjectMapper实例
     */
    protected ObjectMapper getObjectMapper() {
        if (SpringContextHolder.isInjectedApplicationContext()) {
            return SpringContextHolder.getBean(ObjectMapper.class);
        } else {
            return JsonUtil.getObjectMapper();
        }
    }
}