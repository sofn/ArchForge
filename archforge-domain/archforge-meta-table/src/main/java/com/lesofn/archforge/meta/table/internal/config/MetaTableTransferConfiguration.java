package com.lesofn.archforge.meta.table.internal.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 元表格模块配置属性注册。
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(MetaTableTransferProperties.class)
public class MetaTableTransferConfiguration {
}
