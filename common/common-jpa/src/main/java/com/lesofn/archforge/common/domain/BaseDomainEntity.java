package com.lesofn.archforge.common.domain;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

/**
 * 领域实体基类。
 *
 * <p>
 * 不依赖 JPA 注解，用于承载纯领域逻辑、领域事件与身份标识。
 * 持久化映射请使用 {@link com.lesofn.archforge.common.persistence.BasePO} 或相应 PO。
 */
public abstract class BaseDomainEntity<ID> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    protected @Nullable ID id;
    protected long version;
    private final List<Object> domainEvents = new ArrayList<>();

    public @Nullable ID getId() { return id; }

    public void setId(@Nullable ID id) { this.id = id; }

    public long getVersion() { return version; }

    public void setVersion(long version) { this.version = version; }

    /**
     * 注册领域事件。
     *
     * @param event 领域事件，不能为 null
     */
    protected final void registerEvent(Object event) {
        if (event == null) {
            throw new IllegalArgumentException("Domain event must not be null");
        }
        this.domainEvents.add(event);
    }

    /**
     * 获取当前已注册的领域事件（不可修改视图）。
     */
    public List<Object> getDomainEvents() { return Collections.unmodifiableList(this.domainEvents); }

    /**
     * 清空已注册的领域事件。
     */
    public void clearEvents() {
        this.domainEvents.clear();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BaseDomainEntity<?> that = (BaseDomainEntity<?>) o;
        return Objects.equals(this.id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }
}
