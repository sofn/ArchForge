package com.lesofn.archforge.user.api.dao;

import com.lesofn.archforge.user.api.domain.SysDept;
import java.util.List;

/** Type-safe query methods for {@link SysDept} using JPA Criteria API + Hibernate Metamodel. */
public interface SysDeptRepositoryCustom {

    List<SysDept> findAllActiveDepts();
}
