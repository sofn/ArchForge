package com.lesofn.archforge.user.dao;

import com.lesofn.archforge.common.repository.BaseEntity_;
import com.lesofn.archforge.common.repository.CriteriaQuerySupport;
import com.lesofn.archforge.user.domain.SysDept;
import com.lesofn.archforge.user.domain.SysDept_;
import java.util.List;

/** Criteria API implementation — no raw JPQL strings. */
public class SysDeptRepositoryImpl extends CriteriaQuerySupport implements SysDeptRepositoryCustom {

    @Override
    public List<SysDept> findAllActiveDepts() {
        return findByNotDeleted(SysDept.class, BaseEntity_.deleted, SysDept_.sort);
    }
}
