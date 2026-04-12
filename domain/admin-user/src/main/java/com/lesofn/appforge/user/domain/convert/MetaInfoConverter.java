package com.lesofn.appforge.user.domain.convert;

import com.lesofn.appforge.common.repository.converter.AbstractJsonConverter;
import com.lesofn.appforge.user.menu.dto.MetaDTO;
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