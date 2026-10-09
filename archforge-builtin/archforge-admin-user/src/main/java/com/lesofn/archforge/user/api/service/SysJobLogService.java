package com.lesofn.archforge.user.api.service;

import com.lesofn.archforge.user.api.domain.SysJobLog;

/** Execution log of scheduled jobs — written by the job handler in the server application. */
public interface SysJobLogService {

    SysJobLog record(SysJobLog log);
}
