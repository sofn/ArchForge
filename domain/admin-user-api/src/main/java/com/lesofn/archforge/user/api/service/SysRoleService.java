package com.lesofn.archforge.user.api.service;

import com.lesofn.archforge.user.api.dao.SysRoleRepository;
import com.lesofn.archforge.user.api.domain.SysMenu;
import com.lesofn.archforge.user.api.domain.SysRole;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

public interface SysRoleService {
    Optional<SysRole> findById(Long id);

    Optional<SysRole> findByRoleKey(String roleKey);

    Optional<SysRole> findByRoleName(String roleName);

    Page<SysRole> findAll(Pageable pageable);

    List<SysRole> findAll();

    List<SysRole> findAllActiveRoles();

    SysRole create(SysRole role);

    SysRole update(SysRole role);

    void deleteById(Long id);

    void softDeleteById(Long id);

    boolean existsByRoleKey(String roleKey);

    boolean existsByRoleName(String roleName);

    SysRole getById(Long roleId);

    List<SysMenu> getMenuListByRoleId(Long roleId);
}
