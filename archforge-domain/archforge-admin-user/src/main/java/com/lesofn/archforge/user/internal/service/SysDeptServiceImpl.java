package com.lesofn.archforge.user.internal.service;

import com.lesofn.archforge.user.api.service.SysDeptService;
import com.lesofn.archforge.user.api.dao.SysDeptRepository;
import com.lesofn.archforge.user.api.domain.SysDept;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 部门服务
 *
 * @author sofn
 */
@Service
@RequiredArgsConstructor
public class SysDeptServiceImpl implements SysDeptService {

    private final SysDeptRepository deptRepository;

    @Override
    public Optional<SysDept> findById(Long deptId) {
        return deptRepository.findById(deptId);
    }

    @Override
    public List<SysDept> findAll() {
        return deptRepository.findAll();
    }

    @Override
    public List<SysDept> findAllActiveDepts() {
        return deptRepository.findAllActiveDepts();
    }

    @Override
    public List<SysDept> findByParentId(Long parentId) {
        return deptRepository.findByParentId(parentId);
    }

    @Override
    @Transactional
    public SysDept create(SysDept dept) {
        return deptRepository.save(dept);
    }

    @Override
    @Transactional
    public SysDept update(SysDept dept) {
        return deptRepository.save(dept);
    }

    @Override
    @Transactional
    public void deleteById(Long deptId) {
        deptRepository.deleteById(deptId);
    }
}
