package com.lesofn.archforge.meta.table.api.domain.convert;

import com.lesofn.archforge.common.utils.jackson.JsonUtil;
import org.jspecify.annotations.Nullable;
import com.lesofn.archforge.meta.table.api.domain.OptionItem;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.Collections;
import java.util.List;
import org.springframework.util.StringUtils;

/**
 * 字段枚举选项（List<OptionItem>）与 JSON 字符串的转换器。
 */
@Converter
public class OptionListConverter implements AttributeConverter<List<OptionItem>, String> {

    @Override
    public @Nullable String convertToDatabaseColumn(@Nullable List<OptionItem> attribute) {
        if (attribute == null) {
            return null;
        }
        return JsonUtil.to(attribute);
    }

    @Override
    public @Nullable List<OptionItem> convertToEntityAttribute(@Nullable String dbData) {
        if (!StringUtils.hasText(dbData)) {
            return Collections.emptyList();
        }
        return JsonUtil.fromList(dbData, OptionItem.class);
    }
}
