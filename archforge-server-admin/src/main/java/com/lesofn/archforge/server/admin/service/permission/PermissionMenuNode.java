package com.lesofn.archforge.server.admin.service.permission;

import java.util.ArrayList;
import java.util.List;

public record PermissionMenuNode(
        Long id, Long parentId, String name, String permission, boolean button, List<PermissionMenuNode> children) {
    public PermissionMenuNode {
        if (children == null) {
            children = new ArrayList<>();
        }
    }
}
