package com.lesofn.archforge.common.enums.common;

import com.lesofn.archforge.common.enums.DictionaryEnum;
import com.lesofn.archforge.common.enums.dictionary.CssTag;
import com.lesofn.archforge.common.enums.dictionary.Dictionary;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 对应sys_operation_log的status字段
 *
 * @author sofn
 */
@Getter
@AllArgsConstructor
@Dictionary(name = "sysOperationLog.status", label = "操作状态")
public enum OperationStatusEnum implements DictionaryEnum {

    /** 操作状态 */
    SUCCESS(1, "成功", CssTag.PRIMARY),
    FAIL(0, "失败", CssTag.DANGER);

    private final int value;
    private final String description;
    private final String cssTag;
}
