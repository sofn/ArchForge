package com.lesofn.archforge.meta.table.api.dto;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

/**
 * 元表格数据导入结果。
 */
@Data
public class ImportResult {

    private int total;
    private int success;
    private int failed;
    private List<String> errors = new ArrayList<>();

    public static ImportResult of(int total, int success, List<String> errors) {
        ImportResult result = new ImportResult();
        result.setTotal(total);
        result.setSuccess(success);
        result.setFailed(errors.size());
        result.setErrors(errors);
        return result;
    }
}
