package com.lesofn.appforge.user.menu.dto;

import com.google.common.collect.Lists;
import com.lesofn.appforge.user.domain.SysMenu;
import lombok.Data;

import java.util.List;

/**
 * 路由配置信息 DTO
 *
 * @author sofn
 */
@Data
public class RouterDTO {

    public RouterDTO(SysMenu entity) {
        if (entity != null) {
            this.name = entity.getRouterName();
            this.path = entity.getPath();
            if (entity.getMetaInfo() != null) {
                this.meta = entity.getMetaInfo();
            } else {
                this.meta = new MetaDTO();
            }
            this.meta.setAuths(Lists.newArrayList(entity.getPermission()));
        }
    }

    /**
     * 路由名字
     */
    private String name;

    /**
     * 路由地址
     */
    private String path;

    /**
     * 其他元素
     */
    private MetaDTO meta;

    /**
     * 子路由
     */
    private List<RouterDTO> children;

}