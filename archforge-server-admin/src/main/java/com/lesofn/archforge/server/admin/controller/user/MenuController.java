package com.lesofn.archforge.server.admin.controller.user;

import com.lesofn.archforge.infrastructure.annotation.Log;
import com.lesofn.archforge.server.admin.dto.AdminMenuDTO;
import com.lesofn.archforge.server.admin.dto.request.MenuCreateRequest;
import com.lesofn.archforge.server.admin.dto.request.MenuDeleteRequest;
import com.lesofn.archforge.server.admin.dto.request.MenuUpdateRequest;
import com.lesofn.archforge.server.admin.mapper.AdminMenuConvertor;
import com.lesofn.archforge.user.api.domain.SysMenu;
import com.lesofn.archforge.user.api.menu.SysMenuService;
import com.lesofn.archforge.user.api.menu.dto.MetaDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import cn.dev33.satoken.annotation.SaCheckRole;
import com.lesofn.archforge.infrastructure.auth.stp.StpAdminUtil;
import org.springframework.web.bind.annotation.*;

@Tag(name = "菜单管理")
@SaCheckRole(value = "ADMIN", type = StpAdminUtil.TYPE)
@RestController
@CrossOrigin
@RequiredArgsConstructor
@RequestMapping("/admin/menu")
public class MenuController {

    private final SysMenuService menuService;
    private final AdminMenuConvertor menuMapper;

    @Operation(summary = "获取全量菜单列表")
    @PostMapping
    public List<AdminMenuDTO> getMenuList() {
        List<SysMenu> allMenus = menuService.findAllActiveMenus();
        return allMenus.stream().map(menuMapper::toDto).collect(Collectors.toList());
    }

    @Log
    @Operation(summary = "创建菜单")
    @PostMapping("/create")
    public Long createMenu(@RequestBody @Valid MenuCreateRequest request) {
        SysMenu menu = buildMenuFromCreateRequest(request);
        SysMenu saved = menuService.create(menu);
        return saved.getMenuId();
    }

    @Log
    @Operation(summary = "更新菜单")
    @PutMapping("/update")
    public Boolean updateMenu(@RequestBody @Valid MenuUpdateRequest request) {
        Optional<SysMenu> opt = menuService.findById(request.getId());
        if (opt.isEmpty()) {
            return false;
        }
        SysMenu menu = opt.get();
        applyMenuFields(
                menu,
                request.getParentId(),
                request.getMenuType(),
                request.getName(),
                request.getPath(),
                request.getAuths(),
                request.getStatus(),
                request.getTitle(),
                request.getIcon(),
                request.getRank(),
                request.getShowLink(),
                request.getShowParent(),
                request.getKeepAlive(),
                request.getFrameSrc(),
                request.getFrameLoading(),
                request.getHiddenTag());
        menu.setStatus(menu.getStatus() != null ? menu.getStatus() : 1);
        menuService.update(menu);
        return true;
    }

    @Log
    @Operation(summary = "删除菜单")
    @PostMapping("/delete")
    public Boolean deleteMenu(@RequestBody @Valid MenuDeleteRequest request) {
        menuService.softDeleteById(request.getId());
        return true;
    }

    private SysMenu buildMenuFromCreateRequest(MenuCreateRequest request) {
        SysMenu menu = new SysMenu();
        applyMenuFields(
                menu,
                request.getParentId(),
                request.getMenuType(),
                request.getName(),
                request.getPath(),
                request.getAuths(),
                request.getStatus(),
                request.getTitle(),
                request.getIcon(),
                request.getRank(),
                request.getShowLink(),
                request.getShowParent(),
                request.getKeepAlive(),
                request.getFrameSrc(),
                request.getFrameLoading(),
                request.getHiddenTag());
        menu.setStatus(menu.getStatus() != null ? menu.getStatus() : 1);
        return menu;
    }

    private void applyMenuFields(
            SysMenu menu,
            Long parentId,
            Integer menuType,
            String name,
            String path,
            String auths,
            Integer status,
            String title,
            String icon,
            Integer rank,
            Boolean showLink,
            Boolean showParent,
            Boolean keepAlive,
            String frameSrc,
            Boolean frameLoading,
            Boolean hiddenTag) {
        if (parentId != null)
            menu.setParentId(parentId);
        if (menuType != null)
            menu.setMenuType(menuType);
        if (name != null)
            menu.setRouterName(name);
        if (path != null)
            menu.setPath(path);
        if (auths != null)
            menu.setPermission(auths);
        if (status != null)
            menu.setStatus(status);
        MetaDTO meta = menu.getMetaInfo() != null ? menu.getMetaInfo() : new MetaDTO();
        if (title != null)
            meta.setTitle(title);
        if (icon != null)
            meta.setIcon(icon);
        if (rank != null)
            meta.setRank(rank);
        if (showLink != null)
            meta.setShowLink(showLink);
        if (showParent != null)
            meta.setShowParent(showParent);
        if (keepAlive != null)
            meta.setKeepAlive(keepAlive);
        if (frameSrc != null)
            meta.setFrameSrc(frameSrc);
        if (frameLoading != null)
            meta.setFrameLoading(frameLoading);
        if (hiddenTag != null)
            meta.setHiddenTag(hiddenTag);
        menu.setMetaInfo(meta);
        if (title != null)
            menu.setMenuName(title);
    }
}
