package com.lesofn.appforge.common.repository;

import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * @author sofn
 * @version 1.0 Created at: 2025-09-14 23:06
 */
@Setter
@Getter
@MappedSuperclass
public class BaseEntity<T> {

    private Long creatorId;

    private LocalDateTime createTime;

    private Long updaterId;

    private LocalDateTime updateTime;

    /** deleted字段请在数据库中 设置为tinyInt 并且非null 默认值为0 */
    private Boolean deleted;

    @PrePersist
    public void prePersist() {
        if (this.createTime == null) {
            this.createTime = LocalDateTime.now();
        }
        if (this.deleted == null) {
            this.deleted = false;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updateTime = LocalDateTime.now();
    }
}
