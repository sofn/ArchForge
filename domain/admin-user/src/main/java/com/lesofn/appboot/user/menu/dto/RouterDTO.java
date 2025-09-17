package com.lesofn.appboot.user.menu.dto;

import com.google.common.collect.Lists;
import com.lesofn.appboot.user.domain.SysMenu;
import lombok.Data;

import java.util.List;

/**
 * 路由配置信息 DTO
 * @author sofn
 */
@Data
public class RouterDTO {

    public RouterDTO(SysMenu entity) {
        if (entity != null) {
            this.name = entity.getRouterName();
            this.path = entity.getPath();
            // 暂时不需要component
//            this.component = entity.getComponent();
//            this.rank = entity.getRank();
//            this.redirect = entity.getRedirect();
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
    
   /* *//**
     * 是否隐藏路由，当为 true 时，该路由不会在侧边栏出现
     *//*
    private boolean hidden;
    
    *//**
     * 重定向地址
     *//*
    private String redirect;
    
    *//**
     * 组件地址
     *//*
    private String component;
    
    *//**
     * 路由参数
     *//*
    private String query;*/
    
/*    *//**
     * 当你一个路由下面的 children 声明的路由大于1个时，自动会变成嵌套的模式--如组件页面
     *//*
    private boolean alwaysShow;*/
    
    /**
     * 其他元素
     */
    private MetaDTO meta;
    
    /**
     * 子路由
     */
    private List<RouterDTO> children;

}