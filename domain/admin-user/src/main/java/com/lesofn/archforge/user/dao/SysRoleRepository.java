package com.lesofn.archforge.user.dao;

import com.lesofn.archforge.user.domain.SysRole;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface SysRoleRepository extends JpaRepository<SysRole, Long>, JpaSpecificationExecutor<SysRole>, SysRoleRepositoryCustom {

    SysRole findByRoleKey(String roleKey);

    SysRole findByRoleName(String roleName);

    boolean existsByRoleKey(String roleKey);

    boolean existsByRoleName(String roleName);

    List<SysRole> queryByRoleId(Long roleId);
}
