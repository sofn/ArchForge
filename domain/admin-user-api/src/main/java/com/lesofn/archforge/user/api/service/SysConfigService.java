package com.lesofn.archforge.user.api.service;

import com.lesofn.archforge.user.api.dao.SysConfigRepository;
import com.lesofn.archforge.user.api.domain.SysConfig;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

public interface SysConfigService {
    Optional<SysConfig> findById(Long configId);

    Optional<SysConfig> findByConfigKey(String configKey);

    Page<SysConfig> findAll(Pageable pageable);

    List<SysConfig> findAll();

    SysConfig create(SysConfig config);

    SysConfig update(SysConfig config);

    void deleteById(Long configId);
}
