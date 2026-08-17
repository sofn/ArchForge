package com.lesofn.archforge.user.infrastructure.adapter.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.lesofn.archforge.user.domain.model.aggregate.UserAggregate;
import com.lesofn.archforge.user.infrastructure.adapter.repository.po.UserPO;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class UserPOConvertorTest {

    private final UserPOConvertor convertor = new UserPOConvertor() {
    };

    @Test
    void missingRoleIsNotMappedToAdminRole() {
        UserPO po = newUserPo();
        po.setRoleId(null);

        UserAggregate aggregate = convertor.toAggregate(po);

        assertEquals(0L, aggregate.getRoleId().value());
    }

    @Test
    void zeroRoleIsNotMappedToAdminRole() {
        UserPO po = newUserPo();
        po.setRoleId(0L);

        UserAggregate aggregate = convertor.toAggregate(po);

        assertEquals(0L, aggregate.getRoleId().value());
    }

    @Test
    void auditFieldsRoundTrip() {
        LocalDateTime created = LocalDateTime.of(2024, 1, 2, 3, 4, 5);
        LocalDateTime updated = LocalDateTime.of(2024, 2, 3, 4, 5, 6);
        UserPO po = newUserPo();
        po.setRoleId(2L);
        po.setCreatorId(9L);
        po.setCreateTime(created);
        po.setUpdaterId(8L);
        po.setUpdateTime(updated);

        UserAggregate aggregate = convertor.toAggregate(po);
        UserPO saved = convertor.toPo(aggregate);

        assertEquals(9L, aggregate.getCreatorId());
        assertEquals(created, aggregate.getCreateTime());
        assertEquals(8L, aggregate.getUpdaterId());
        assertEquals(updated, aggregate.getUpdateTime());
        assertEquals(9L, saved.getCreatorId());
        assertEquals(created, saved.getCreateTime());
        assertEquals(8L, saved.getUpdaterId());
        assertEquals(updated, saved.getUpdateTime());
        assertFalse(saved.getDeleted());
        assertNull(convertor.toAggregate(null));
    }

    private static UserPO newUserPo() {
        UserPO po = new UserPO();
        po.setId(11L);
        po.setUsername("alice");
        po.setEmail("alice@example.com");
        po.setPhoneNumber("13900139000");
        po.setPassword("encrypted");
        po.setStatus(1);
        po.setDeleted(false);
        return po;
    }
}
