package com.lesofn.archforge.user.api.service;

import com.lesofn.archforge.user.api.domain.SysOperLog;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.time.LocalDateTime;
import java.util.List;

public interface SysOperLogService {
    Optional<SysOperLog> findById(Long operId);

    Page<SysOperLog> findAll(Pageable pageable);

    SysOperLog create(SysOperLog operLog);

    void deleteById(Long operId);

    void clearAll();

    /** Operations at or after {@code since}. */
    long countSince(LocalDateTime since);

    /** The {@code limit} most recent operations, newest first. */
    List<SysOperLog> latest(int limit);
}
