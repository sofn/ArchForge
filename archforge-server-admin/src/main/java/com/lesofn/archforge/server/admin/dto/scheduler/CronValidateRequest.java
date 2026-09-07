package com.lesofn.archforge.server.admin.dto.scheduler;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Cron 表达式校验请求
 *
 * @author lesofn
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CronValidateRequest {

    private String cron;
}
