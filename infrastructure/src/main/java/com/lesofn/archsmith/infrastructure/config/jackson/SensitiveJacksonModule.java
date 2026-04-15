package com.lesofn.archsmith.infrastructure.config.jackson;

import com.lesofn.archsmith.common.sensitive.Sensitive;
import java.util.List;
import tools.jackson.databind.BeanDescription;
import tools.jackson.databind.SerializationConfig;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.module.SimpleModule;
import tools.jackson.databind.ser.BeanPropertyWriter;
import tools.jackson.databind.ser.ValueSerializerModifier;

/**
 * 数据脱敏Jackson模块（Jackson 3.x）
 *
 * <p>自动扫描Bean属性上的 @Sensitive 注解，为匹配的String字段注册脱敏序列化器。 作为Spring Bean暴露后，Spring Boot 4 会自动将其注册到
 * JsonMapper 中。
 *
 * @author sofn
 */
public class SensitiveJacksonModule extends SimpleModule {

    public SensitiveJacksonModule() {
        super("SensitiveModule");
        setSerializerModifier(new SensitiveSerializerModifier());
    }

    /** 扫描 @Sensitive 注解并替换序列化器 */
    private static class SensitiveSerializerModifier extends ValueSerializerModifier {

        @Override
        @SuppressWarnings("unchecked")
        public List<BeanPropertyWriter> changeProperties(
                SerializationConfig config,
                BeanDescription.Supplier beanDescSupplier,
                List<BeanPropertyWriter> beanProperties) {
            for (BeanPropertyWriter writer : beanProperties) {
                Sensitive sensitive = writer.getMember().getAnnotation(Sensitive.class);
                if (sensitive != null && String.class.equals(writer.getType().getRawClass())) {
                    SensitiveValueSerializer serializer =
                            new SensitiveValueSerializer(sensitive.value());
                    writer.assignSerializer((ValueSerializer) serializer);
                }
            }
            return beanProperties;
        }
    }
}
