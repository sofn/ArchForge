package com.lesofn.appforge.user.service;

import com.lesofn.appforge.user.dao.SysDeptRepository;
import com.lesofn.appforge.user.domain.SysDept;
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
public class SysDeptService {

    private final SysDeptRepository deptRepository;

    public Optional<SysDept> findById(Long deptId) {
        return deptRepository.findById(deptId);
    }

    public List<SysDept> findAll() {
        return deptRepository.findAll();
    }

    public List<SysDept> findAllActiveDepts() {
        return deptRepository.findAllActiveDepts();
    }

    public List<SysDept> findByParentId(Long parentId) {
        return deptRepository.findByParentId(parentId);
    }

    @Transactional
    public SysDept create(SysDept dept) {
        return deptRepository.save(dept);
    }

    @Transactional
    public SysDept update(SysDept dept) {
        return deptRepository.save(dept);
    }

    @Transactional
    public void deleteById(Long deptId) {
        deptRepository.deleteById(deptId);
    }
}
