package com.lesofn.archforge.user.api.dao.dict;

import com.lesofn.archforge.user.api.domain.dict.SysDictType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface SysDictTypeRepository extends JpaRepository<SysDictType, Long>, JpaSpecificationExecutor<SysDictType> {

    Optional<SysDictType> findByDictCode(String dictCode);
}
