package com.lesofn.archforge.user.internal.service;

import com.lesofn.archforge.user.api.dao.SysUserRepository;
import com.lesofn.archforge.user.api.domain.SysUser;
import com.lesofn.archforge.user.api.domain.query.SysUserQuery;
import com.lesofn.archforge.user.api.service.SysUserService;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.time.LocalDateTime;
import java.util.ArrayList;
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

    private static final char LIKE_ESCAPE_CHAR = '!';

    private final SysUserRepository userRepository;

    public Optional<SysUser> findById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<SysUser> findByUsername(String username) {
        return Optional.ofNullable(userRepository.findByUsername(username));
    }

    public Optional<SysUser> findByEmail(String email) {
        return Optional.ofNullable(userRepository.findByEmail(email));
    }

    public Optional<SysUser> findByPhoneNumber(String phoneNumber) {
        return Optional.ofNullable(userRepository.findByPhoneNumber(phoneNumber));
    }

    public Page<SysUser> findAll(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    public Page<SysUser> findAll(Specification<SysUser> spec, Pageable pageable) {
        return userRepository.findAll(spec, pageable);
    }

    public List<SysUser> findAll() {
        return userRepository.findAll();
    }

    @Transactional
    public SysUser create(SysUser user) {
        user.setCreateTime(LocalDateTime.now());
        user.setDeleted(false);
        return userRepository.save(user);
    }

    @Transactional
    public SysUser update(SysUser user) {
        user.setUpdateTime(LocalDateTime.now());
        return userRepository.save(user);
    }

    @Transactional
    public void deleteById(Long id) {
        userRepository.deleteById(id);
    }

    private void updateIfPresent(Long id, Consumer<SysUser> updater) {
        userRepository
                .findById(id)
                .ifPresent(
                        user -> {
                            updater.accept(user);
                            user.setUpdateTime(LocalDateTime.now());
                            userRepository.save(user);
                        });
    }

    @Transactional
    public void softDeleteById(Long id) {
        updateIfPresent(id, user -> user.setDeleted(true));
    }

    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public boolean existsByPhoneNumber(String phoneNumber) {
        return userRepository.existsByPhoneNumber(phoneNumber);
    }

    @Transactional
    public void updateLoginInfo(Long userId, String loginIp) {
        updateIfPresent(
                userId,
                user -> {
                    user.setLoginIp(loginIp);
                    user.setLoginDate(LocalDateTime.now());
                });
    }

    @Transactional
    public void resetPassword(Long userId, String newPassword) {
        updateIfPresent(userId, user -> user.setPassword(newPassword));
    }

    public Page<SysUser> searchUsers(SysUserQuery query, Pageable pageable) {
        Specification<SysUser> spec = (root, criteriaQuery, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (query != null) {
                if (isNotBlank(query.getUsername())) {
                    predicates.add(like(cb, root, "username", query.getUsername()));
                }
                if (isNotBlank(query.getEmail())) {
                    predicates.add(like(cb, root, "email", query.getEmail()));
                }
                if (isNotBlank(query.getPhoneNumber())) {
                    predicates.add(like(cb, root, "phoneNumber", query.getPhoneNumber()));
                }
                if (query.getEnabled() != null) {
                    if (Boolean.TRUE.equals(query.getEnabled())) {
                        predicates.add(cb.equal(root.get("status"), 1));
                    } else {
                        predicates.add(cb.notEqual(root.get("status"), 1));
                    }
                }
            }
            predicates.add(cb.equal(root.get("deleted"), false));
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return userRepository.findAll(spec, pageable);
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

    private static boolean isNotBlank(String value) {
        return value != null && !value.isBlank();
    }

    @Transactional
    public void updateStatus(Long userId, Integer status) {
        updateIfPresent(userId, user -> user.setStatus(status));
    }

    @Transactional
    public void updatePassword(Long userId, String newPassword) {
        updateIfPresent(userId, user -> user.setPassword(newPassword));
    }

    public List<SysUser> findActiveUsers() {
        return userRepository.findAll((root, criteriaQuery, cb) -> cb.and(
                cb.equal(root.get("status"), 1),
                cb.equal(root.get("deleted"), false)));
    }

    public List<SysUser> findByDeptId(Long deptId) {
        if (deptId == null) {
            return List.of();
        }
        return userRepository.findAll((root, criteriaQuery, cb) -> cb.and(
                cb.equal(root.get("deptId"), deptId),
                cb.equal(root.get("deleted"), false)));
    }

    public SysUser getUserByUserName(String username) {
        return userRepository.findByUsername(username);
    }
}
