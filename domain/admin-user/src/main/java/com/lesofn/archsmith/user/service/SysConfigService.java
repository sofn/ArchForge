package com.lesofn.archsmith.user.service;

import com.lesofn.archsmith.user.dao.SysConfigRepository;
import com.lesofn.archsmith.user.domain.SysConfig;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SysConfigService {

    private final SysConfigRepository configRepository;

    public Optional<SysConfig> findById(Long configId) {
        return configRepository.findById(configId);
    }

    public Optional<SysConfig> findByConfigKey(String configKey) {
        return configRepository.findByConfigKey(configKey);
    }

    public Page<SysConfig> findAll(Pageable pageable) {
        return configRepository.findAll(pageable);
    }

    public List<SysConfig> findAll() {
        return configRepository.findAll();
    }

    @Transactional
    public SysConfig create(SysConfig config) {
        return configRepository.save(config);
    }

    @Transactional
    public SysConfig update(SysConfig config) {
        return configRepository.save(config);
    }

    @Transactional
    public void deleteById(Long configId) {
        configRepository.deleteById(configId);
    }
}
