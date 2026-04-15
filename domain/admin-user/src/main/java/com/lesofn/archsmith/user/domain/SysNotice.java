package com.lesofn.archsmith.user.domain;

import com.lesofn.archsmith.common.repository.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

@Setter
@Getter
@Accessors(chain = true)
@Entity
@Table(name = "sys_notice")
@DynamicInsert
@DynamicUpdate
public class SysNotice extends BaseEntity<SysNotice> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long noticeId;

    @Column(length = 128)
    private String noticeTitle;

    /** 公告类型（1通知 2公告） */
    private Integer noticeType;

    @Column(columnDefinition = "TEXT")
    private String noticeContent;

    /** 状态（1正常 0关闭） */
    private Integer status;

    @Column(length = 512)
    private String remark;
}
