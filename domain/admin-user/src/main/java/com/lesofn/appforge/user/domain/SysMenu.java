package com.lesofn.appforge.user.domain;

import com.lesofn.appforge.common.repository.BaseEntity;
import com.lesofn.appforge.user.domain.convert.MetaInfoConverter;
import com.lesofn.appforge.user.menu.dto.MetaDTO;
import jakarta.persistence.*;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

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

    @Column(name = "is_button")
    private Boolean isButton;

    private String permission;

    @Convert(converter = MetaInfoConverter.class)
    private MetaDTO metaInfo;

    private Integer status;

    private String remark;

    @Transient private List<SysMenu> children;
}
