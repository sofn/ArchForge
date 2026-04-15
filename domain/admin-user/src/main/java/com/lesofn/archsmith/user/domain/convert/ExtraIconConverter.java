package com.lesofn.archsmith.user.domain.convert;

import com.lesofn.archsmith.common.repository.converter.AbstractJsonConverter;
import com.lesofn.archsmith.user.menu.dto.ExtraIconDTO;
import jakarta.persistence.Converter;

/** ExtraIconDTO的JSON转换器示例 这样任何需要JSON转换的DTO都可以快速创建对应的Converter */
@Converter
public class ExtraIconConverter extends AbstractJsonConverter<ExtraIconDTO> {

    @Override
    protected Class<ExtraIconDTO> getTargetType() {
        return ExtraIconDTO.class;
    }
}
