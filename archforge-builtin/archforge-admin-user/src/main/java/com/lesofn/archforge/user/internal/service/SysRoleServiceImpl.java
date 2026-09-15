package com.lesofn.archforge.user.internal.service;

import com.lesofn.archforge.user.api.dao.SysRoleRepository;
import com.lesofn.archforge.user.api.domain.SysMenu;
import com.lesofn.archforge.user.api.domain.SysRole;
import com.lesofn.archforge.user.api.service.SysRoleService;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SysRoleServiceImpl implements SysRoleService {

    private final SysRoleRepository roleRepository;

    @Override
    public Optional<SysRole> findById(Long id) {
        return roleRepository.findById(id);
    }

    @Override
    public Optional<SysRole> findByRoleKey(String roleKey) {
        return Optional.ofNullable(roleRepository.findByRoleKey(roleKey));
    }

    @Override
    public Optional<SysRole> findByRoleName(String roleName) {
        return Optional.ofNullable(roleRepository.findByRoleName(roleName));
    }

    @Override
    public Page<SysRole> findAll(Pageable pageable) {
        return roleRepository.findAll(pageable);
    }

    @Override
    public List<SysRole> findAll() {
        return roleRepository.findAll();
    }

    @Override
    public List<SysRole> findAllActiveRoles() {
        return roleRepository.findAllActiveRoles();
    }

    @Override
    @Transactional
    public SysRole create(SysRole role) {
        role.setCreateTime(LocalDateTime.now(ZoneId.systemDefault()));
        role.setDeleted(false);
        return roleRepository.save(role);
    }

    @Override
    @Transactional
    public SysRole update(SysRole role) {
        role.setUpdateTime(LocalDateTime.now(ZoneId.systemDefault()));
        return roleRepository.save(role);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        roleRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void softDeleteById(Long id) {
        roleRepository
                .findById(id)
                .ifPresent(
                        role -> {
                            role.setDeleted(true);
                            role.setUpdateTime(LocalDateTime.now(ZoneId.systemDefault()));
                            roleRepository.save(role);
                        });
    }

    @Override
    public boolean existsByRoleKey(String roleKey) {
        return roleRepository.existsByRoleKey(roleKey);
    }

    @Override
    public boolean existsByRoleName(String roleName) {
        return roleRepository.existsByRoleName(roleName);
    }

    @Override
    public @Nullable SysRole getById(Long roleId) {
        return roleRepository.findById(roleId).orElse(null);
    }

    @Override
    public List<SysMenu> getMenuListByRoleId(Long roleId) {
        return roleRepository.getMenuListByRoleId(roleId);
    }
}
