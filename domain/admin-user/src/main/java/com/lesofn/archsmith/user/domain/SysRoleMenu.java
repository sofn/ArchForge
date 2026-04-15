package com.lesofn.archsmith.user.domain;

import jakarta.persistence.*;
import java.io.Serializable;
import lombok.Data;
import lombok.experimental.Accessors;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

@Data
@Accessors(chain = true)
@Entity
@Table(name = "sys_role_menu")
@DynamicInsert
@DynamicUpdate
@IdClass(SysRoleMenu.SysRoleMenuId.class)
public class SysRoleMenu {

    @Id private Long roleId;

    @Id private Long menuId;

    @Data
    @Accessors(chain = true)
    public static class SysRoleMenuId implements Serializable {
        private Long roleId;
        private Long menuId;
    }
}
