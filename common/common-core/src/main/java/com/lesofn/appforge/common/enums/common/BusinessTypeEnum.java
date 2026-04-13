package com.lesofn.appforge.common.enums.common;

import com.lesofn.appforge.common.enums.DictionaryEnum;
import com.lesofn.appforge.common.enums.dictionary.CssTag;
import com.lesofn.appforge.common.enums.dictionary.Dictionary;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 对应sys_operation_log的business_type
 *
 * @author sofn
 */
@Getter
@AllArgsConstructor
@Dictionary(name = "sysOperationLog.businessType")
public enum BusinessTypeEnum implements DictionaryEnum {

    /** 操作类型 */
    OTHER(0, "其他操作", CssTag.INFO),
    ADD(1, "添加", CssTag.PRIMARY),
    MODIFY(2, "修改", CssTag.PRIMARY),
    DELETE(3, "删除", CssTag.DANGER),
    GRANT(4, "授权", CssTag.PRIMARY),
    EXPORT(5, "导出", CssTag.WARNING),
    IMPORT(6, "导入", CssTag.WARNING),
    FORCE_LOGOUT(7, "强退", CssTag.DANGER),
    CLEAN(8, "清空", CssTag.DANGER),
    ;

    private final int value;
    private final String description;
    private final String cssTag;
}
