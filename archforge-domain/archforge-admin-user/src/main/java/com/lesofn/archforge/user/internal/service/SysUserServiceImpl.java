package com.lesofn.archforge.user.internal.service;

import com.lesofn.archforge.user.api.domain.SysUser;
import com.lesofn.archforge.user.api.domain.query.SysUserQuery;
import com.lesofn.archforge.user.api.service.SysUserService;
import com.lesofn.archforge.user.domain.adapter.repository.UserRepository;
import com.lesofn.archforge.user.domain.model.aggregate.UserAggregate;
import com.lesofn.archforge.user.domain.model.query.UserQuery;
import com.lesofn.archforge.user.domain.valueobject.Email;
import com.lesofn.archforge.user.domain.valueobject.Password;
import com.lesofn.archforge.user.domain.valueobject.PhoneNumber;
import com.lesofn.archforge.user.domain.valueobject.RoleId;
import com.lesofn.archforge.user.domain.valueobject.UserId;
import com.lesofn.archforge.user.domain.valueobject.Username;
import com.lesofn.archforge.user.internal.convert.SysUserConvertor;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SysUserServiceImpl implements SysUserService {

    private final UserRepository userRepository;
    private final SysUserConvertor sysUserConvertor;

    public Optional<SysUser> findById(Long id) {
        if (id == null || id <= 0L) {
            return Optional.empty();
        }
        return userRepository.findById(new UserId(id)).map(sysUserConvertor::toSysUser);
    }

    public Optional<SysUser> findByUsername(String username) {
        if (username == null || username.isBlank()) {
            return Optional.empty();
        }
        try {
            return userRepository.findByUsername(new Username(username)).map(sysUserConvertor::toSysUser);
        } catch (IllegalArgumentException ex) {
            return Optional.empty();
        }
    }

    public Optional<SysUser> findByEmail(String email) {
        if (email == null || email.isBlank()) {
            return Optional.empty();
        }
        try {
            return userRepository.findByEmail(new Email(email)).map(sysUserConvertor::toSysUser);
        } catch (IllegalArgumentException ex) {
            return Optional.empty();
        }
    }

    public Optional<SysUser> findByPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isBlank()) {
            return Optional.empty();
        }
        return userRepository.findByPhoneNumber(new PhoneNumber(phoneNumber)).map(sysUserConvertor::toSysUser);
    }

    public Page<SysUser> findAll(Pageable pageable) {
        return userRepository.findAll(pageable).map(sysUserConvertor::toSysUser);
    }

    public Page<SysUser> findAll(Specification<SysUser> spec, Pageable pageable) {
        return userRepository.findAll(spec, pageable).map(sysUserConvertor::toSysUser);
    }

    public List<SysUser> findAll() {
        return userRepository.findAll().stream().map(sysUserConvertor::toSysUser).toList();
    }

    @Transactional("userDomainTransactionManager")
    public SysUser create(SysUser user) {
        return sysUserConvertor.toSysUser(userRepository.save(sysUserConvertor.toAggregate(user)));
    }

    @Transactional("userDomainTransactionManager")
    public SysUser update(SysUser user) {
        return sysUserConvertor.toSysUser(userRepository.save(sysUserConvertor.toAggregate(user)));
    }

    @Transactional("userDomainTransactionManager")
    public void deleteById(Long id) {
        userRepository.deleteById(new UserId(id));
    }

    @Transactional("userDomainTransactionManager")
    public void softDeleteById(Long id) {
        updateIfPresent(id, UserAggregate::markDeleted);
    }

    public boolean existsByUsername(String username) {
        if (username == null || username.isBlank()) {
            return false;
        }
        try {
            return userRepository.existsByUsername(new Username(username));
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    public boolean existsByEmail(String email) {
        return email != null && !email.isBlank() && userRepository.existsByEmail(new Email(email));
    }

    public boolean existsByPhoneNumber(String phoneNumber) {
        return phoneNumber != null && !phoneNumber.isBlank() && userRepository.existsByPhoneNumber(
                new PhoneNumber(phoneNumber));
    }

    @Transactional("userDomainTransactionManager")
    public void updateLoginInfo(Long userId, String loginIp) {
        updateIfPresent(userId, user -> user.recordLogin(loginIp));
    }

    @Transactional("userDomainTransactionManager")
    public void resetPassword(Long userId, String newPassword) {
        updateIfPresent(userId, user -> user.changePassword(Password.ofEncrypted(newPassword)));
    }

    public Page<SysUser> searchUsers(SysUserQuery query, Pageable pageable) {
        UserQuery userQuery = new UserQuery();
        if (query != null) {
            userQuery.setUsername(query.getUsername());
            userQuery.setEmail(query.getEmail());
            userQuery.setPhoneNumber(query.getPhoneNumber());
            userQuery.setEnabled(query.getEnabled());
        }
        return userRepository.search(userQuery, pageable).map(sysUserConvertor::toSysUser);
    }

    @Transactional("userDomainTransactionManager")
    public void updateStatus(Long userId, Integer status) {
        updateIfPresent(userId, user -> user.updateStatus(status));
    }

    @Transactional("userDomainTransactionManager")
    public void updatePassword(Long userId, String newPassword) {
        updateIfPresent(userId, user -> user.changePassword(Password.ofEncrypted(newPassword)));
    }

    @Transactional("userDomainTransactionManager")
    public void updateProfile(
            Long userId,
            String username,
            String nickname,
            String phoneNumber,
            String email,
            Integer sex,
            String remark,
            Long deptId) {
        updateIfPresent(userId, user -> {
            user.rename(username);
            user.updateProfile(nickname, phoneNumber, email, sex, remark, deptId);
        });
    }

    @Transactional("userDomainTransactionManager")
    public void assignRole(Long userId, Long roleId) {
        if (roleId == null) {
            return;
        }
        updateIfPresent(userId, user -> {
            RoleId nextRole = new RoleId(roleId);
            if (!nextRole.equals(user.getRoleId())) {
                user.assignRole(nextRole);
            }
        });
    }

    public List<SysUser> findActiveUsers() {
        return userRepository.findActiveUsers().stream().map(sysUserConvertor::toSysUser).toList();
    }

    public List<SysUser> findByDeptId(Long deptId) {
        return userRepository.findByDeptId(deptId).stream().map(sysUserConvertor::toSysUser).toList();
    }

    public SysUser getUserByUserName(String username) {
        return findByUsername(username).orElse(null);
    }

    private void updateIfPresent(Long id, Consumer<UserAggregate> updater) {
        if (id == null || id <= 0L) {
            return;
        }
        userRepository.findById(new UserId(id)).ifPresent(user -> {
            updater.accept(user);
            userRepository.save(user);
        });
    }
}
