package com.lesofn.matrixboot.task.dao;

import com.lesofn.matrixboot.task.domain.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskDao extends JpaRepository<Task, Long>, JpaSpecificationExecutor<Task> {

    @Modifying
    @Query("delete from Task task where task.uid=?1")
    void deleteByUid(Long id);

    Page<Task> findByUid(long uid, Pageable request);
}
