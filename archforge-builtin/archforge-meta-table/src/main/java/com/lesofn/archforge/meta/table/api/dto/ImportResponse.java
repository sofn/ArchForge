package com.lesofn.archforge.meta.table.api.dto;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

/**
 * 元表格数据导入结果。
 */
@Data
public class ImportResponse {

    private int total;
    private int success;
    private int failed;
    private boolean errorTruncated;
    private List<String> errors = new ArrayList<>();

    public static ImportResponse of(int total, int success, int failed, List<String> errors, boolean errorTruncated) {
        ImportResponse result = new ImportResponse();
        result.setTotal(total);
        result.setSuccess(success);
        result.setFailed(failed);
        result.setErrorTruncated(errorTruncated);
        result.setErrors(errors);
        return result;
    }
}
