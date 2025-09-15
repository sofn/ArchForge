package com.lesofn.appboot.user.domain;

import com.lesofn.appboot.common.repository.BaseEntity;
import com.lesofn.appboot.user.domain.convert.MetaInfoConverter;
import com.lesofn.appboot.user.menu.dto.MetaDTO;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.util.List;

@Setter
@Getter
@Accessors(chain = true)
@Entity
@Table(name = "sys_menu")
@DynamicInsert
@DynamicUpdate
public class SysMenu extends BaseEntity<SysMenu> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long menuId;

    private String menuName;

    private Integer menuType;

    private String routerName;

    private Long parentId;

    private String path;

    private Boolean isButton;

    private String permission;

    @Convert(converter = MetaInfoConverter.class)
    @Column(columnDefinition = "TEXT")
    private MetaDTO metaInfo;

    private Integer status;

    private String remark;

    @Transient
    private List<SysMenu> children;

}