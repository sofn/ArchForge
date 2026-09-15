package com.lesofn.archforge.meta.table.api.service;

import com.lesofn.archforge.meta.table.api.domain.OptionItem;
import java.util.List;

/**
 * 字典项查找 SPI，由引用方实现并提供 Bean。
 */
public interface DictionaryProvider {

    List<OptionItem> findItems(String dictCode);
}
