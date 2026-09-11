package com.lesofn.archforge.common.dictionary;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 枚举字典扫描配置（{@code arch-forge.dictionary.*}）。
 *
 * <p>
 * 独立属性类，替代原 ArchForgeProperties.Dictionary——common 层不反向依赖 infrastructure.config。
 */
@Setter
@Getter
@ConfigurationProperties(prefix = "arch-forge.dictionary")
public class DictionaryProperties {

    /** 是否启用枚举字典扫描 */
    private boolean enabled = true;

    /** 扫描根包，支持子包递归 */
    private List<String> enumBasePackages = List.of("com.lesofn.archforge");
}
