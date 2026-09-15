package com.lesofn.archforge.user.internal.service;

import com.lesofn.archforge.common.enums.common.GenderEnum;
import com.lesofn.archforge.common.enums.common.UserStatusEnum;
import com.lesofn.archforge.user.api.dao.SysUserRepository;
import com.lesofn.archforge.user.api.domain.SysUser;
import com.lesofn.archforge.user.api.domain.query.SysUserQuery;
import com.lesofn.archforge.user.api.service.SysUserService;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SysUserServiceImpl implements SysUserService {

    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_.-]{2,64}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");
    private static final char LIKE_ESCAPE_CHAR = '!';

    private final SysUserRepository userRepository;

    @Override
    public Optional<SysUser> findById(Long id) {
        if (id == null || id <= 0L) {
            return Optional.empty();
        }
        return userRepository.findById(id);
    }

    @Override
    public Optional<SysUser> findByUsername(String username) {
        if (username == null || !USERNAME_PATTERN.matcher(username).matches()) {
            return Optional.empty();
        }
        return Optional.ofNullable(userRepository.findByUsername(username));
    }

    @Override
    public Optional<SysUser> findByEmail(String email) {
        if (email == null || email.isBlank() || !EMAIL_PATTERN.matcher(email).matches()) {
            return Optional.empty();
        }
        return Optional.ofNullable(userRepository.findByEmail(email));
    }

    @Override
    public Optional<SysUser> findByPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isBlank()) {
            return Optional.empty();
        }
        if (!PHONE_PATTERN.matcher(phoneNumber).matches()) {
            throw new IllegalArgumentException("Invalid phone number: " + phoneNumber);
        }
        return Optional.ofNullable(userRepository.findByPhoneNumber(phoneNumber));
    }

    @Override
    public Page<SysUser> findAll(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    @Override
    public Page<SysUser> findAll(Specification<SysUser> spec, Pageable pageable) {
        return userRepository.findAll(spec, pageable);
    }

    @Override
    public List<SysUser> findAll() {
        return userRepository.findAll();
    }

    @Transactional
    @Override
    public SysUser create(SysUser user) {
        return saveWithDefaults(user);
    }

    @Transactional
    @Override
    public SysUser update(SysUser user) {
        return saveWithDefaults(user);
    }

    @Transactional
    @Override
    public void deleteById(Long id) {
        if (id == null) {
            return;
        }
        userRepository.deleteById(id);
    }

    @Transactional
    @Override
    public void softDeleteById(Long id) {
        updateIfPresent(id, SysUser::markDeleted);
    }

    @Override
    public boolean existsByUsername(String username) {
        return username != null && USERNAME_PATTERN.matcher(username).matches() && userRepository.existsByUsername(username);
    }

    @Override
    public boolean existsByEmail(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException("Invalid email format: " + email);
        }
        return userRepository.existsByEmail(email);
    }

    @Override
    public boolean existsByPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isBlank()) {
            return false;
        }
        if (!PHONE_PATTERN.matcher(phoneNumber).matches()) {
            throw new IllegalArgumentException("Invalid phone number: " + phoneNumber);
        }
        return userRepository.existsByPhoneNumber(phoneNumber);
    }

    @Transactional
    @Override
    public void updateLoginInfo(Long userId, String loginIp) {
        updateIfPresent(userId, user -> user.recordLogin(loginIp));
    }

    @Transactional
    @Override
    public void resetPassword(Long userId, String newPassword) {
        updateIfPresent(userId, user -> user.changePassword(newPassword));
    }

    @Override
    public Page<SysUser> searchUsers(SysUserQuery query, Pageable pageable) {
        return userRepository.findAll(searchSpec(query), pageable);
    }

    @Transactional
    @Override
    public void updateStatus(Long userId, Integer status) {
        updateIfPresent(userId, user -> user.updateStatus(
                status == null ? Integer.valueOf(UserStatusEnum.NORMAL.getValue()) : status));
    }

    @Transactional
    @Override
    public void updatePassword(Long userId, String newPassword) {
        updateIfPresent(userId, user -> user.changePassword(newPassword));
    }

    @Transactional
    @Override
    public void updateProfile(
            Long userId,
            @Nullable String username,
            @Nullable String nickname,
            @Nullable String phoneNumber,
            @Nullable String email,
            @Nullable Integer sex,
            @Nullable String remark,
            @Nullable Long deptId) {
        updateIfPresent(userId, user -> {
            if (username != null) {
                if (!USERNAME_PATTERN.matcher(username).matches()) {
                    throw new IllegalArgumentException("Username must be 2-64 characters and only contain letters, digits, underscore, dot or hyphen");
                }
                user.setUsername(username);
            }
            if (phoneNumber != null && !phoneNumber.isBlank() && !PHONE_PATTERN.matcher(phoneNumber).matches()) {
                throw new IllegalArgumentException("Invalid phone number: " + phoneNumber);
            }
            if (email != null && !email.isBlank() && !EMAIL_PATTERN.matcher(email).matches()) {
                throw new IllegalArgumentException("Invalid email format: " + email);
            }
            user.updateProfile(nickname, phoneNumber, email,
                    sex == null ? null : GenderEnum.fromValue(sex), remark, deptId);
        });
    }

    @Transactional
    @Override
    public void assignRole(Long userId, Long roleId) {
        if (roleId == null) {
            return;
        }
        updateIfPresent(userId, user -> {
            Long current = user.getRoleId();
            if (!Objects.equals(roleId, current == null ? Long.valueOf(0L) : current)) {
                user.assignRole(roleId);
            }
        });
    }

    @Override
    public List<SysUser> findActiveUsers() {
        return userRepository.findAll(activeSpec());
    }

    @Override
    public List<SysUser> findByDeptId(Long deptId) {
        if (deptId == null) {
            return List.of();
        }
        return userRepository.findAll((root, q, cb) -> cb.and(
                cb.equal(root.get("deptId"), deptId),
                cb.equal(root.get("deleted"), false)));
    }

    @Override
    @Nullable
    public SysUser getUserByUserName(String username) {
        return findByUsername(username).orElse(null);
    }

    private void updateIfPresent(Long id, Consumer<SysUser> updater) {
        if (id == null || id <= 0L) {
            return;
        }
        userRepository.findById(id).ifPresent(user -> {
            updater.accept(user);
            userRepository.save(user);
        });
    }

    private SysUser saveWithDefaults(SysUser user) {
        if (user == null) {
            throw new IllegalArgumentException("User must not be null");
        }
        if (user.getUsername() == null || !USERNAME_PATTERN.matcher(user.getUsername()).matches()) {
            throw new IllegalArgumentException("Username must be 2-64 characters and only contain letters, digits, underscore, dot or hyphen");
        }
        String password = user.getPassword();
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Encrypted password must not be blank");
        }
        if (user.getEmail() != null && !user.getEmail().isBlank() && !EMAIL_PATTERN.matcher(user.getEmail()).matches()) {
            throw new IllegalArgumentException("Invalid email format: " + user.getEmail());
        }
        if (user.getPhoneNumber() != null && !user.getPhoneNumber().isBlank() && !PHONE_PATTERN.matcher(user.getPhoneNumber())
                .matches()) {
            throw new IllegalArgumentException("Invalid phone number: " + user.getPhoneNumber());
        }
        if (user.getStatus() == null) {
            user.setStatus(UserStatusEnum.NORMAL.getValue());
        }
        if (user.getRoleId() == null) {
            user.setRoleId(0L);
        }
        if (user.getUserId() != null) {
            userRepository.findById(user.getUserId())
                    .ifPresent(existing -> mergePreservedFields(user, existing));
        }
        return userRepository.save(user);
    }

    private void mergePreservedFields(SysUser target, SysUser source) {
        if (target.getNickname() == null || target.getNickname().isBlank()) {
            target.setNickname(source.getNickname());
        }
        if (target.getUserType() == null) {
            target.setUserType(source.getUserType());
        }
        if (target.getSex() == null) {
            target.setSex(source.getSex());
        }
        if (target.getAvatar() == null || target.getAvatar().isBlank()) {
            target.setAvatar(source.getAvatar());
        }
        if (target.getLoginIp() == null || target.getLoginIp().isBlank()) {
            target.setLoginIp(source.getLoginIp());
        }
        if (target.getLoginDate() == null) {
            target.setLoginDate(source.getLoginDate());
        }
        if (target.getIsAdmin() == null) {
            target.setIsAdmin(source.getIsAdmin());
        }
        if (target.getRemark() == null || target.getRemark().isBlank()) {
            target.setRemark(source.getRemark());
        }
        if (target.getDeptId() == null) {
            target.setDeptId(source.getDeptId());
        }
        target.setCreateTime(source.getCreateTime());
        target.setCreatorId(source.getCreatorId());
        if (target.getDeleted() == null) {
            target.setDeleted(source.getDeleted());
        }
    }

    private Specification<SysUser> searchSpec(SysUserQuery query) {
        return (root, criteriaQuery, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (query != null) {
                String username = query.getUsername();
                if (username != null && !username.isBlank()) {
                    predicates.add(like(cb, root, "username", username));
                }
                String email = query.getEmail();
                if (email != null && !email.isBlank()) {
                    predicates.add(like(cb, root, "email", email));
                }
                String phoneNumber = query.getPhoneNumber();
                if (phoneNumber != null && !phoneNumber.isBlank()) {
                    predicates.add(like(cb, root, "phoneNumber", phoneNumber));
                }
                if (query.getEnabled() != null) {
                    if (Boolean.TRUE.equals(query.getEnabled())) {
                        predicates.add(cb.equal(root.get("status"), UserStatusEnum.NORMAL.getValue()));
                    } else {
                        predicates.add(cb.notEqual(root.get("status"), UserStatusEnum.NORMAL.getValue()));
                    }
                }
            }
            predicates.add(cb.equal(root.get("deleted"), false));
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private Specification<SysUser> activeSpec() {
        return (root, query, cb) -> cb.and(
                cb.equal(root.get("status"), UserStatusEnum.NORMAL.getValue()),
                cb.equal(root.get("deleted"), false));
    }

    private static Predicate like(CriteriaBuilder cb, Root<SysUser> root, String attribute, String value) {
        String pattern = "%" + escapeLike(value) + "%";
        return cb.like(root.get(attribute).as(String.class), pattern, LIKE_ESCAPE_CHAR);
    }

    private static String escapeLike(String value) {
        String escape = String.valueOf(LIKE_ESCAPE_CHAR);
        return value.replace(escape, escape + escape)
                .replace("%", escape + "%")
                .replace("_", escape + "_");
    }

}
