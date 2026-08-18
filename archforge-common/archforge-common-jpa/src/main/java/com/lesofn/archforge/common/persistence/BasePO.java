package com.lesofn.archforge.common.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import org.jspecify.annotations.Nullable;

/**
 * JPA 持久化对象基类。
 *
 * <p>
 * 提供统一的主键、审计字段与逻辑删除标识，专供贫血/富领域模型映射后的 PO 使用。
 */
@Setter
@Getter
@MappedSuperclass
public abstract class BasePO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected @Nullable Long id;

    @Column(name = "creator_id")
    protected Long creatorId;

    @Column(name = "create_time")
    protected LocalDateTime createTime;

    @Column(name = "updater_id")
    protected Long updaterId;

    @Column(name = "update_time")
    protected LocalDateTime updateTime;

    /** 逻辑删除标识：请在数据库中设置为非 null 默认值 0。 */
    @Column(name = "deleted")
    protected Boolean deleted;

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
