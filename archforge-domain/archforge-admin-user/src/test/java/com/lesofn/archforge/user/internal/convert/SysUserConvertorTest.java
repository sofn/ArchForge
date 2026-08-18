package com.lesofn.archforge.user.internal.convert;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.lesofn.archforge.common.enums.common.GenderEnum;
import com.lesofn.archforge.user.api.domain.SysUser;
import com.lesofn.archforge.user.domain.model.aggregate.UserAggregate;
import org.junit.jupiter.api.Test;

class SysUserConvertorTest {

    private final SysUserConvertor convertor = new SysUserConvertor();

    @Test
    void shouldRoundTripProfileFields() {
        SysUser source = new SysUser();
        source.setUserId(11L);
        source.setRoleId(2L);
        source.setDeptId(8L);
        source.setUsername("alice");
        source.setNickname("Alice");
        source.setEmail("alice@example.com");
        source.setPhoneNumber("13900139000");
        source.setSex(GenderEnum.FEMALE);
        source.setPassword("encrypted");
        source.setStatus(1);
        source.setRemark("note");

        UserAggregate aggregate = convertor.toAggregate(source);
        SysUser converted = convertor.toSysUser(aggregate);

        assertEquals(11L, converted.getUserId());
        assertEquals(2L, converted.getRoleId());
        assertEquals(8L, converted.getDeptId());
        assertEquals("alice", converted.getUsername());
        assertEquals("Alice", converted.getNickname());
        assertEquals("alice@example.com", converted.getEmail());
        assertEquals("13900139000", converted.getPhoneNumber());
        assertEquals(GenderEnum.FEMALE, converted.getSex());
        assertEquals("note", converted.getRemark());
        assertFalse(converted.isDeleted());
    }

    @Test
    void shouldTreatUnassignedRoleAsNull() {
        SysUser source = new SysUser();
        source.setUserId(1L);
        source.setUsername("admin");
        source.setPassword("encrypted");
        source.setStatus(1);

        UserAggregate aggregate = convertor.toAggregate(source);
        SysUser converted = convertor.toSysUser(aggregate);

        assertNull(converted.getRoleId());
        assertEquals("", converted.getEmail());
        assertEquals(0L, aggregate.getRoleId().value());
    }

    @Test
    void shouldCopyAuditFields() {
        SysUser source = new SysUser();
        source.setUserId(1L);
        source.setUsername("admin");
        source.setPassword("encrypted");
        source.setStatus(1);
        source.setCreatorId(3L);
        source.setUpdaterId(4L);

        UserAggregate aggregate = convertor.toAggregate(source);
        SysUser converted = convertor.toSysUser(aggregate);

        assertEquals(3L, converted.getCreatorId());
        assertEquals(4L, converted.getUpdaterId());
    }
}
