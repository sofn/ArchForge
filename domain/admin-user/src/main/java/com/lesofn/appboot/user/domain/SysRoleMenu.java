package com.lesofn.appboot.user.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serializable;

@Data
@Accessors(chain = true)
@Entity
@Table(name = "sys_role_menu")
@DynamicInsert
@DynamicUpdate
@IdClass(SysRoleMenu.SysRoleMenuId.class)
public class SysRoleMenu {

    @Id
    @Column(name = "role_id", nullable = false)
    private Long roleId;

    @Id
    @Column(name = "menu_id", nullable = false)
    private Long menuId;

    @Data
    @Accessors(chain = true)
    public static class SysRoleMenuId implements Serializable {
        private Long roleId;
        private Long menuId;
    }
}