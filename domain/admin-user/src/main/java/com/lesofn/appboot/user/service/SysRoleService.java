package com.lesofn.appboot.user.service;

import com.lesofn.appboot.user.dao.SysRoleRepository;
import com.lesofn.appboot.user.domain.SysMenu;
import com.lesofn.appboot.user.domain.SysRole;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SysRoleService {

    private final SysRoleRepository roleRepository;

    public Optional<SysRole> findById(Long id) {
        return roleRepository.findById(id);
    }

    public Optional<SysRole> findByRoleKey(String roleKey) {
        return Optional.ofNullable(roleRepository.findByRoleKey(roleKey));
    }

    public Optional<SysRole> findByRoleName(String roleName) {
        return Optional.ofNullable(roleRepository.findByRoleName(roleName));
    }

    public Page<SysRole> findAll(Pageable pageable) {
        return roleRepository.findAll(pageable);
    }

    public List<SysRole> findAll() {
        return roleRepository.findAll();
    }

    public List<SysRole> findAllActiveRoles() {
        return roleRepository.findAllActiveRoles();
    }

    @Transactional
    public SysRole create(SysRole role) {
        role.setCreateTime(LocalDateTime.now());
        role.setDeleted(false);
        return roleRepository.save(role);
    }

    @Transactional
    public SysRole update(SysRole role) {
        role.setUpdateTime(LocalDateTime.now());
        return roleRepository.save(role);
    }

    @Transactional
    public void deleteById(Long id) {
        roleRepository.deleteById(id);
    }

    @Transactional
    public void softDeleteById(Long id) {
        roleRepository.findById(id).ifPresent(role -> {
            role.setDeleted(true);
            role.setUpdateTime(LocalDateTime.now());
            roleRepository.save(role);
        });
    }

    public boolean existsByRoleKey(String roleKey) {
        return roleRepository.existsByRoleKey(roleKey);
    }

    public boolean existsByRoleName(String roleName) {
        return roleRepository.existsByRoleName(roleName);
    }

    public SysRole getById(Long roleId) {
        return roleRepository.findById(roleId).orElse(null);
    }

    public List<SysMenu> getMenuListByRoleId(Long roleId) {
        return roleRepository.getMenuListByRoleId(roleId);
    }
}