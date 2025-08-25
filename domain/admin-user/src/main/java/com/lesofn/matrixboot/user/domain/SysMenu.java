package com.lesofn.matrixboot.user.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Accessors(chain = true)
@Entity
@Table(name = "sys_menu")
@DynamicInsert
@DynamicUpdate
public class SysMenu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "menu_id")
    private Long menuId;

    @Column(name = "menu_name", nullable = false, length = 64)
    private String menuName;

    @Column(name = "menu_type", nullable = false)
    private Short menuType;

    @Column(name = "router_name", nullable = false, length = 255)
    private String routerName;

    @Column(name = "parent_id", nullable = false)
    private Long parentId;

    @Column(name = "path", length = 255)
    private String path;

    @Column(name = "is_button", nullable = false)
    private Boolean isButton;

    @Column(name = "permission", length = 128)
    private String permission;

    @Column(name = "meta_info", nullable = false, length = 1024)
    private String metaInfo;

    @Column(name = "status", nullable = false)
    private Short status;

    @Column(name = "remark", length = 256)
    private String remark;

    @Column(name = "creator_id")
    private Long creatorId;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @Column(name = "updater_id")
    private Long updaterId;

    @Column(name = "update_time")
    private LocalDateTime updateTime;

    @Column(name = "deleted", nullable = false)
    private Boolean deleted;

    @Transient
    private List<SysMenu> children;
}