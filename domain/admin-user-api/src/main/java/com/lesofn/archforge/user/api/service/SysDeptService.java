package com.lesofn.archforge.user.api.service;

import com.lesofn.archforge.user.api.dao.SysDeptRepository;
import com.lesofn.archforge.user.api.domain.SysDept;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

public interface SysDeptService {
    Optional<SysDept> findById(Long deptId);

    List<SysDept> findAll();

    List<SysDept> findAllActiveDepts();

    List<SysDept> findByParentId(Long parentId);

    SysDept create(SysDept dept);

    SysDept update(SysDept dept);

    void deleteById(Long deptId);
}
