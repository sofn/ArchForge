package com.lesofn.archforge.user.dao;

import com.lesofn.archforge.common.repository.BaseEntity_;
import com.lesofn.archforge.common.repository.CriteriaQuerySupport;
import com.lesofn.archforge.user.domain.SysLoginLog;

/** Criteria API implementation — no raw JPQL strings. */
public class SysLoginLogRepositoryImpl extends CriteriaQuerySupport implements SysLoginLogRepositoryCustom {

    @Override
    public void clearAll() {
        deleteByAttribute(SysLoginLog.class, BaseEntity_.deleted, false);
    }
}
