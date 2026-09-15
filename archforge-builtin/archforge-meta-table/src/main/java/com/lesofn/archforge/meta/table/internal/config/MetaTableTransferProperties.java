package com.lesofn.archforge.meta.table.internal.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 元表格数据导入/导出资源上限配置。
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "arch-forge.meta-table.transfer")
public class MetaTableTransferProperties {

    /** 单次导入最大数据行数 */
    private long maxImportRows = 10000;

    /** 单次导入文件最大字节数 */
    private long maxFileBytes = 10L * 1024 * 1024;

    /** 导入响应中保留的错误明细条数上限，超出部分截断 */
    private int maxErrorList = 100;

    /** 单次导出最大数据行数，超出直接拒绝并提示添加过滤条件 */
    private long maxExportRows = 50000;

    /** 导入批量 INSERT 每批行数 */
    private int importBatchSize = 500;

    /** 导出 keyset 分块每块行数 */
    private int exportChunkSize = 1000;
}
