package com.lesofn.archforge.user.api.dao.dict;

import com.lesofn.archforge.user.api.domain.dict.SysDictItem;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface SysDictItemRepository extends JpaRepository<SysDictItem, Long>, JpaSpecificationExecutor<SysDictItem> {

    List<SysDictItem> findByDictTypeIdAndDeletedFalseOrderBySortAsc(Long dictTypeId);

    List<SysDictItem> findByDictTypeIdAndStatusAndDeletedFalseOrderBySortAsc(Long dictTypeId, Integer status);
}
