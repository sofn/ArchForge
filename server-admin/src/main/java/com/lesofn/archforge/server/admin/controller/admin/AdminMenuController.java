package com.lesofn.archforge.server.admin.controller.admin;

import com.lesofn.archforge.server.admin.dto.AdminMenuItemDTO;
import com.lesofn.archforge.server.admin.dto.request.MenuCreateRequest;
import com.lesofn.archforge.server.admin.dto.request.MenuDeleteRequest;
import com.lesofn.archforge.server.admin.dto.request.MenuUpdateRequest;
import com.lesofn.archforge.server.admin.mapper.AdminMenuMapper;
import com.lesofn.archforge.user.domain.SysMenu;
import com.lesofn.archforge.user.menu.SysMenuService;
import com.lesofn.archforge.user.menu.dto.MetaDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "菜单管理")
@RestController
@CrossOrigin
@RequiredArgsConstructor
public class AdminMenuController {

    private final SysMenuService menuService;
    private final AdminMenuMapper menuMapper;

    @Operation(summary = "获取全量菜单列表")
    @PostMapping("/menu")
    public List<AdminMenuItemDTO> getMenuList() {
        List<SysMenu> allMenus = menuService.findAllActiveMenus();
        return allMenus.stream().map(menuMapper::toDto).collect(Collectors.toList());
    }

    @Operation(summary = "创建菜单")
    @PostMapping("/menu/create")
    public Long createMenu(@RequestBody @Valid MenuCreateRequest request) {
        SysMenu menu = buildMenuFromCreateRequest(request);
        SysMenu saved = menuService.create(menu);
        return saved.getMenuId();
    }

    @Operation(summary = "更新菜单")
    @PutMapping("/menu/update")
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

    @Operation(summary = "删除菜单")
    @PostMapping("/menu/delete")
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
