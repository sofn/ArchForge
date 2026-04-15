package com.lesofn.archsmith.infrastructure.config.jackson;

import com.lesofn.archsmith.common.sensitive.SensitiveType;
import com.lesofn.archsmith.common.sensitive.SensitiveUtil;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;

/**
 * 数据脱敏Jackson序列化器（Jackson 3.x）
 *
 * <p>将标注了 @Sensitive 注解的 String 字段在 JSON 序列化时自动进行脱敏处理
 *
 * @author sofn
 */
public class SensitiveValueSerializer extends StdSerializer<String> {

    private final SensitiveType sensitiveType;

    public SensitiveValueSerializer(SensitiveType sensitiveType) {
        super(String.class);
        this.sensitiveType = sensitiveType;
    }

    @Override
    public void serialize(String value, JsonGenerator gen, SerializationContext ctxt) {
        if (value == null) {
            gen.writeNull();
        } else {
            gen.writeString(SensitiveUtil.mask(sensitiveType, value));
        }
    }
}
