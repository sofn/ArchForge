package com.lesofn.appboot.common.repository.convert;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记字段为JSON类型
 * 使用此注解后，框架会自动为该字段生成对应的JSON转换器
 * 
 * 使用示例：
 * @JsonField
 * @Column(columnDefinition = "TEXT")
 * private MetaDTO metaInfo;
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface JsonField {
    
    /**
     * 是否自动创建转换器，默认为true
     */
    boolean autoConverter() default true;
    
    /**
     * 数据库列类型，默认为TEXT
     */
    String columnDefinition() default "TEXT";
}