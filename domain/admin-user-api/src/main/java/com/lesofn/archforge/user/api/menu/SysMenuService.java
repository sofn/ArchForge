package com.lesofn.archforge.user.api.menu;

import com.lesofn.archforge.common.enums.common.StatusEnum;
import com.lesofn.archforge.infrastructure.auth.model.SystemLoginUser;
import com.lesofn.archforge.user.api.dao.SysRoleMenuRepository;
import com.lesofn.archforge.user.api.domain.SysMenu;
import com.lesofn.archforge.user.api.menu.dto.MetaDTO;
import com.lesofn.archforge.user.api.menu.dto.RouterDTO;
import com.lesofn.archforge.user.api.menu.repository.SysMenuRepository;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

public interface SysMenuService {
    Optional<SysMenu> findById(Long id);

    List<SysMenu> findByParentId(Long parentId);

    List<SysMenu> findMenusByRoleId(Long roleId);

    List<SysMenu> findAllActiveMenus();

    List<SysMenu> findByPermission(String permission);

    SysMenu create(SysMenu menu);

    SysMenu update(SysMenu menu);

    void deleteById(Long id);

    void softDeleteById(Long id);

    List<SysMenu> buildMenuTree(List<SysMenu> menus);

    List<RouterDTO> getRouterTree(SystemLoginUser loginUser);
}
