package com.lesofn.archforge.meta.table.api.domain;

import java.io.Serializable;
import lombok.Data;
import org.jspecify.annotations.Nullable;

/**
 * 枚举类型字段的选项项。
 */
@Data
public class OptionItem implements Serializable {

    private static final long serialVersionUID = 1L;

    private String label;

    private @Nullable Object value;

    public OptionItem() {
    }

    public OptionItem(String label, Object value) {
        this.label = label;
        this.value = value;
    }
}
