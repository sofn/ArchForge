package com.lesofn.archforge.user.api.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.IdClass;
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
@SuppressWarnings("NullAway.Init") // fields populated via setters (Jackson/JPA)
public class SysRoleMenu {

    @Id
    private Long roleId;

    @Id
    private Long menuId;

    @Data
    @Accessors(chain = true)
    public static class SysRoleMenuId implements Serializable {
        private Long roleId;
        private Long menuId;
    }
}
