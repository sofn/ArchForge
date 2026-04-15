package com.lesofn.archsmith.user.domain;

import com.lesofn.archsmith.common.repository.BaseEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

@Setter
@Getter
@Accessors(chain = true)
@Entity
@Table(name = "sys_login_log")
@DynamicInsert
@DynamicUpdate
public class SysLoginLog extends BaseEntity<SysLoginLog> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long infoId;

    @Column(length = 64)
    private String username;

    @Column(length = 128)
    private String ip;

    @Column(length = 256)
    private String address;

    @Column(name = "os_name", length = 64)
    private String systemName;

    @Column(length = 64)
    private String browser;

    /** 登录状态（1成功 0失败） */
    private Integer status;

    @Column(length = 128)
    private String behavior;

    private LocalDateTime loginTime;
}
