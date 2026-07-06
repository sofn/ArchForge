package com.lesofn.archforge.user.dao;

import com.lesofn.archforge.common.repository.BaseEntity_;
import com.lesofn.archforge.common.repository.CriteriaQuerySupport;
import com.lesofn.archforge.user.domain.SysOperLog;

/** Criteria API implementation — no raw JPQL strings. */
public class SysOperLogRepositoryImpl extends CriteriaQuerySupport implements SysOperLogRepositoryCustom {

    @Override
    public void clearAll() {
        deleteByAttribute(SysOperLog.class, BaseEntity_.deleted, false);
    }
}
