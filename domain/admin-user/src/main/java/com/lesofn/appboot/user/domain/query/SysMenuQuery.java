package com.lesofn.appboot.user.domain.query;

import lombok.Data;

@Data
public class SysMenuQuery {
    private String menuName;
    private Short menuType;
    private Long parentId;
    private Boolean isButton;
    private String permission;
    private Short status;
}