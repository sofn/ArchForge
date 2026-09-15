package com.lesofn.archforge.user.api.service;

import com.lesofn.archforge.user.api.domain.SysFile;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 文件元数据服务接口
 */
public interface SysFileService {

    SysFile create(SysFile sysFile);

    Optional<SysFile> findById(Long id);

    Page<SysFile> findFiles(String originalName, String storageType, Pageable pageable);

    void deleteById(Long id);
}
