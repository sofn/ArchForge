package com.lesofn.archforge.user.dao;

import com.lesofn.archforge.user.domain.SysDept;
import java.util.List;

/** Type-safe query methods for {@link SysDept} using JPA Criteria API + Hibernate Metamodel. */
public interface SysDeptRepositoryCustom {

    List<SysDept> findAllActiveDepts();
}
