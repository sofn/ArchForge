package com.lesofn.archforge.user.api.menu.dto;

import lombok.Data;

/**
 * 菜单附加图标配置 DTO。
 *
 * @author sofn
 */
@Data
@SuppressWarnings("NullAway.Init") // fields populated via setters (Jackson/JPA)
public class ExtraIconDTO {

    // 是否是svg
    private boolean svg;
    // iconfont名称，目前只支持iconfont，后续拓展
    private String name;
}
