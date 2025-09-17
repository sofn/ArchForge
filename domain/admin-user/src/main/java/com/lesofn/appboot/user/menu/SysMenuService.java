package com.lesofn.appboot.user.menu;

import com.lesofn.appboot.common.enums.common.StatusEnum;
import com.lesofn.appboot.infrastructure.auth.model.SystemLoginUser;
import com.lesofn.appboot.user.menu.dto.MetaDTO;
import com.lesofn.appboot.user.menu.repository.SysMenuRepository;
import com.lesofn.appboot.user.dao.SysRoleMenuRepository;
import com.lesofn.appboot.user.domain.SysMenu;
import com.lesofn.appboot.user.menu.dto.RouterDTO;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SysMenuService {

    private final SysMenuRepository sysMenuRepository;
    private final SysRoleMenuRepository roleMenuRepository;

    public Optional<SysMenu> findById(Long id) {
        return sysMenuRepository.findById(id);
    }

    public List<SysMenu> findByParentId(Long parentId) {
        return sysMenuRepository.findByParentId(parentId);
    }

    public List<SysMenu> findMenusByRoleId(Long roleId) {
        return sysMenuRepository.findMenusByRoleId(roleId);
    }

    public List<SysMenu> findAllActiveMenus() {
        return sysMenuRepository.findAllActiveMenus();
    }

    public List<SysMenu> findByPermission(String permission) {
        return sysMenuRepository.findByPermission(permission);
    }

    @Transactional
    public SysMenu create(SysMenu menu) {
        menu.setCreateTime(LocalDateTime.now());
        menu.setDeleted(false);
        return sysMenuRepository.save(menu);
    }

    @Transactional
    public SysMenu update(SysMenu menu) {
        menu.setUpdateTime(LocalDateTime.now());
        return sysMenuRepository.save(menu);
    }

    @Transactional
    public void deleteById(Long id) {
        sysMenuRepository.deleteById(id);
        roleMenuRepository.deleteByMenuId(id);
    }

    @Transactional
    public void softDeleteById(Long id) {
        sysMenuRepository.findById(id).ifPresent(menu -> {
            menu.setDeleted(true);
            menu.setUpdateTime(LocalDateTime.now());
            sysMenuRepository.save(menu);
        });
    }

    public List<SysMenu> buildMenuTree(List<SysMenu> menus) {
        return buildMenuTree(menus, 0L);
    }

    private List<SysMenu> buildMenuTree(List<SysMenu> menus, Long parentId) {
        return menus.stream()
                .filter(menu -> menu.getParentId().equals(parentId))
                .peek(menu -> menu.setChildren(buildMenuTree(menus, menu.getMenuId())))
                .toList();
    }

    public List<RouterDTO> getRouterTree(SystemLoginUser loginUser) {

        List<SysMenu> allMenus;
        if (loginUser.isAdmin()) {
            allMenus = sysMenuRepository.findAll();
        } else {
            allMenus = sysMenuRepository.selectMenuListByUserId(loginUser.getUserId());
        }

        // 传给前端的路由排除掉按钮和停用的菜单
        List<SysMenu> noButtonMenus = allMenus.stream()
                .filter(menu -> !menu.getIsButton())
                .filter(menu-> StatusEnum.ENABLE.getValue() == menu.getStatus())
                .collect(Collectors.toList());


        Map<Long, SysMenu> parentMap = noButtonMenus.stream()
                .collect(Collectors.toMap(SysMenu::getMenuId, Function.identity()));

        Map<SysMenu, RouterDTO> routerMap = noButtonMenus.stream()
                .collect(Collectors.toMap(Function.identity(), RouterDTO::new));

        List<RouterDTO> roots = new ArrayList<>();

        for (Map.Entry<SysMenu, RouterDTO> entry : routerMap.entrySet()) {
            if (entry.getKey().getParentId() == null || entry.getKey().getParentId() == 0) {
                roots.add(entry.getValue());
            } else {
                SysMenu parentSysMenu = parentMap.get(entry.getKey().getParentId());
                RouterDTO routerDTO = routerMap.get(parentSysMenu);
                if (routerDTO == null) {
                    continue;
                }
                List<RouterDTO> children = Objects.requireNonNullElse(routerDTO.getChildren(), new ArrayList<>());
                children.add(entry.getValue());
                routerDTO.setChildren(children);
            }
        }

        roots = roots.stream()
                .sorted(Comparator.comparing(it -> Optional.ofNullable(it.getMeta()).map(MetaDTO::getRank).orElse(0)))
                .toList();

        sortRouterDTOChildren(roots);
        return roots;
    }

    private void sortRouterDTOChildren(List<RouterDTO> routers) {
        if (CollectionUtils.isEmpty(routers)) {
            return;
        }

        Deque<RouterDTO> stack = new ArrayDeque<>(routers);

        while (!stack.isEmpty()) {
            RouterDTO current = stack.pop();

            if (CollectionUtils.isNotEmpty(current.getChildren())) {
                List<RouterDTO> sortedChildren = current.getChildren().stream()
                        .sorted(Comparator.comparing(it -> Optional.ofNullable(it.getMeta()).map(MetaDTO::getRank).orElse(0)))
                        .toList();
                current.setChildren(sortedChildren);

                stack.addAll(sortedChildren);
            }
        }
    }

}