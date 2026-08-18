package com.lesofn.archforge.user.internal.service;

import com.lesofn.archforge.user.api.dao.SysFileRepository;
import com.lesofn.archforge.user.api.domain.SysFile;
import com.lesofn.archforge.user.api.service.SysFileService;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 文件元数据服务实现
 */
@Service
@RequiredArgsConstructor
public class SysFileServiceImpl implements SysFileService {

    private final SysFileRepository fileRepository;

    @Transactional
    public SysFile create(SysFile sysFile) {
        return fileRepository.save(sysFile);
    }

    @Transactional(readOnly = true)
    public Optional<SysFile> findById(Long id) {
        return fileRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Page<SysFile> findFiles(String originalName, String storageType, Pageable pageable) {
        Specification<SysFile> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("deleted"), false));
            if (originalName != null && !originalName.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("originalName")), "%" + originalName.toLowerCase() + "%"));
            }
            if (storageType != null && !storageType.isBlank()) {
                predicates.add(cb.equal(root.get("storageType"), storageType));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return fileRepository.findAll(spec, pageable);
    }

    @Transactional
    public void deleteById(Long id) {
        fileRepository.deleteById(id);
    }
}
