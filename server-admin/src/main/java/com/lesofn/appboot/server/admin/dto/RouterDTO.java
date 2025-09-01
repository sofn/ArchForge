package com.lesofn.appboot.server.admin.dto;

import lombok.Data;

import java.util.List;

/**
 * 路由配置信息 DTO
 * @author sofn
 */
@Data
public class RouterDTO {
    
    /**
     * 路由名字
     */
    private String name;
    
    /**
     * 路由地址
     */
    private String path;
    
    /**
     * 是否隐藏路由，当为 true 时，该路由不会在侧边栏出现
     */
    private boolean hidden;
    
    /**
     * 重定向地址
     */
    private String redirect;
    
    /**
     * 组件地址
     */
    private String component;
    
    /**
     * 路由参数
     */
    private String query;
    
    /**
     * 当你一个路由下面的 children 声明的路由大于1个时，自动会变成嵌套的模式--如组件页面
     */
    private boolean alwaysShow;
    
    /**
     * 其他元素
     */
    private MetaDTO meta;
    
    /**
     * 子路由
     */
    private List<RouterDTO> children;
    
    @Data
    public static class MetaDTO {
        /**
         * 路由在侧边栏和面包屑中展示的名字
         */
        private String title;
        
        /**
         * 路由的图标
         */
        private String icon;
        
        /**
         * 如果设置为 true，则不会被 <keep-alive> 缓存
         */
        private boolean noCache;
        
        /**
         * 内链地址
         */
        private String link;
    }
}