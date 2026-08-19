package com.lesofn.archforge.common.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.lesofn.archforge.common.repository.BaseEntity;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class BasePOAuditTest {

    @Test
    void prePersistFillsCreateTimeAndDeleted() {
        BasePO po = new BasePO() {};
        po.prePersist();
        assertNotNull(po.getCreateTime());
        assertFalse(po.getDeleted());
    }

    @Test
    void preUpdateSetsUpdateTime() {
        BasePO po = new BasePO() {};
        po.prePersist();
        po.preUpdate();
        assertNotNull(po.getUpdateTime());
        assertTrue(!po.getUpdateTime().isBefore(po.getCreateTime()));
    }

    @Test
    void existingCreateTimeIsKept() {
        BasePO po = new BasePO() {};
        LocalDateTime existing = LocalDateTime.of(2024, 1, 1, 0, 0);
        po.setCreateTime(existing);
        po.prePersist();
        assertEquals(existing, po.getCreateTime());
    }

    @Test
    void baseEntityAppliesSameAuditRules() {
        BaseEntity<Long> entity = new BaseEntity<>();
        entity.prePersist();
        assertNotNull(entity.getCreateTime());
        assertFalse(entity.getDeleted());
        entity.preUpdate();
        assertNotNull(entity.getUpdateTime());
    }
}
