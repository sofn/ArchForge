package com.lesofn.archforge.server.admin.event;

import com.lesofn.archforge.user.api.domain.SysOperLog;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Event published by {@link com.lesofn.archforge.server.admin.aspect.LogAspect} after an
 * annotated operation is executed. Consumed asynchronously by {@link LogEventListener}.
 */
@Getter
@AllArgsConstructor
public class LogEvent {

    private final SysOperLog operLog;
}
