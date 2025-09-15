package com.lesofn.appboot.user.domain.convert;

import com.lesofn.appboot.common.repository.convert.AbstractJsonConverter;
import com.lesofn.appboot.user.menu.dto.MetaDTO;
import jakarta.persistence.Converter;

/**
 * MetaDTO的JSON转换器
 * 继承自AbstractJsonConverter，自动获得JSON转换功能
 */
@Converter
public class MetaInfoConverter extends AbstractJsonConverter<MetaDTO> {

    @Override
    protected Class<MetaDTO> getTargetType() {
        return MetaDTO.class;
    }
}