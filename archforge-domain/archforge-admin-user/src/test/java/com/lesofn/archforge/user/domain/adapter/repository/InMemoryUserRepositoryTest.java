package com.lesofn.archforge.user.domain.adapter.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.lesofn.archforge.user.domain.model.aggregate.UserAggregate;
import com.lesofn.archforge.user.domain.model.query.UserQuery;
import com.lesofn.archforge.user.domain.valueobject.Email;
import com.lesofn.archforge.user.domain.valueobject.Password;
import com.lesofn.archforge.user.domain.valueobject.PhoneNumber;
import com.lesofn.archforge.user.domain.valueobject.RoleId;
import com.lesofn.archforge.user.domain.valueobject.UserId;
import com.lesofn.archforge.user.domain.valueobject.Username;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

class InMemoryUserRepositoryTest {

    private InMemoryUserRepository repository;

    @BeforeEach
    void setUp() {
        this.repository = new InMemoryUserRepository();
        this.repository.save(createUser(1L, "admin", "admin@example.com", "13800138000", 2L));
        UserAggregate disabled = createUser(2L, "alice", "alice@example.com", "13900139000", 3L);
        disabled.disable();
        this.repository.save(disabled);
        UserAggregate deleted = createUser(3L, "bobby", "bobby@example.com", "13700137000", 4L);
        deleted.assignDept(9L);
        deleted.markDeleted();
        this.repository.save(deleted);
        UserAggregate inDept = createUser(4L, "carol", "carol@example.com", "13600136000", 5L);
        inDept.assignDept(9L);
        this.repository.save(inDept);
    }

    @Test
    void shouldFindByUsernameEmailAndPhone() {
        assertEquals("admin", this.repository.findByUsername(new Username("admin")).orElseThrow().getUser().getUsername()
                .value());
        assertEquals("alice", this.repository.findByEmail(new Email("alice@example.com")).orElseThrow().getUser().getUsername()
                .value());
        assertEquals("carol", this.repository.findByPhoneNumber(new PhoneNumber("13600136000")).orElseThrow().getUser()
                .getUsername().value());
    }

    @Test
    void shouldCountActiveAndOnlineUsers() {
        assertEquals(2L, this.repository.countActiveUsers());
        assertEquals(2L, this.repository.countOnlineUsers());
    }

    @Test
    void shouldSearchAndFindByDept() {
        UserQuery query = new UserQuery();
        query.setUsername("a");
        Page<UserAggregate> page = this.repository.search(query, PageRequest.of(0, 10));
        assertEquals(3, page.getTotalElements());

        List<UserAggregate> deptUsers = this.repository.findByDeptId(9L);
        assertEquals(1, deptUsers.size());
        assertEquals("carol", deptUsers.get(0).getUser().getUsername().value());
    }

    @Test
    void shouldCheckExistence() {
        assertTrue(this.repository.existsByUsername(new Username("admin")));
        assertTrue(this.repository.existsByEmail(new Email("alice@example.com")));
        assertFalse(this.repository.existsByPhoneNumber(new PhoneNumber("13100000000")));
    }

    private static UserAggregate createUser(long id, String username, String email, String phone, long roleId) {
        return UserAggregate.create(
                new UserId(id),
                new Username(username),
                new Email(email),
                new PhoneNumber(phone),
                Password.ofEncrypted("encrypted"),
                new RoleId(roleId));
    }
}
