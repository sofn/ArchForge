package com.lesofn.archsmith.server.admin.dto;

import com.lesofn.archsmith.common.enums.dictionary.DictionaryData;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 系统配置 DTO
 *
 * @author sofn
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConfigDTO {

    /** 是否开启验证码 */
    private Boolean isCaptchaOn = true;

    private Map<String, List<DictionaryData>> dictionary;
}
