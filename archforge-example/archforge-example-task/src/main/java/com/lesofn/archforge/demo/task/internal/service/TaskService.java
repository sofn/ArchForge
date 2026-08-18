package com.lesofn.archforge.demo.task.internal.service;

import static com.lesofn.archforge.demo.task.api.errors.TaskErrorCode.TASK_NOT_EXISTS;

import com.lesofn.archforge.demo.task.internal.repository.TaskDao;
import com.lesofn.archforge.demo.task.api.domain.Task;
import com.lesofn.archforge.demo.task.api.domain.TaskStatus;
import com.lesofn.archforge.demo.task.api.dto.*;
import com.lesofn.archforge.demo.task.api.errors.TaskException;
import jakarta.persistence.criteria.Predicate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/** Task application service */
@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskDao taskDao;

    public Optional<Task> getTask(Long id) {
        return taskDao.findById(id);
    }

    public Task findById(Long id) {
        return taskDao.findById(id).orElseThrow(() -> new TaskException(TASK_NOT_EXISTS));
    }

    @Transactional
    public Long createTask(TaskCreateRequest request, Long uid) {
        long ownerUid = uid != null ? uid : 0L;
        Task task = Task.create(request.getTitle(), request.getDescription(), ownerUid);
        Task saved = taskDao.save(task);
        return saved.getId();
    }

    @Transactional
    public Boolean updateTask(TaskUpdateRequest request) {
        Task task = findById(request.getId());
        task.updateInfo(request.getTitle(), request.getDescription());
        if (request.getUid() != null) {
            task.reassign(request.getUid());
        }
        taskDao.save(task);
        return true;
    }

    @Transactional
    public Boolean deleteTask(Long id) {
        Task task = findById(id);
        task.softDelete();
        taskDao.save(task);
        return true;
    }

    @Transactional
    public Boolean startTask(Long id) {
        Task task = findById(id);
        task.start();
        taskDao.save(task);
        return true;
    }

    @Transactional
    public Boolean completeTask(Long id) {
        Task task = findById(id);
        task.complete();
        taskDao.save(task);
        return true;
    }

    @Transactional
    public Boolean cancelTask(Long id) {
        Task task = findById(id);
        task.cancel();
        taskDao.save(task);
        return true;
    }

    public TaskPageResponse<TaskDTO> searchTasks(TaskListRequest request) {
        int currentPage = request.getCurrentPage() != null && request.getCurrentPage() > 0
                ? request.getCurrentPage()
                : 1;
        int pageSize = request.getPageSize() != null && request.getPageSize() > 0
                ? request.getPageSize()
                : 10;
        Pageable pageable = PageRequest.of(currentPage - 1, pageSize);

        TaskStatus statusFilter = parseStatus(request.getStatus());

        Specification<Task> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("deleted"), false));
            if (StringUtils.hasText(request.getTitle())) {
                predicates.add(cb.like(
                        root.get("title"),
                        "%" + request.getTitle() + "%",
                        '!'));
            }
            if (statusFilter != null) {
                predicates.add(cb.equal(root.get("status"), statusFilter));
            }
            if (request.getUid() != null) {
                predicates.add(cb.equal(root.get("uid"), request.getUid()));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<Task> page = taskDao.findAll(spec, pageable);
        List<TaskDTO> list = page.getContent().stream()
                .map(this::toResponse)
                .toList();
        return TaskPageResponse.of(list, page.getTotalElements(), pageSize, currentPage);
    }

    public Page<Task> getTasksByPage(long uid, Pageable request) {
        return taskDao.findByUid(uid, request);
    }

    private TaskDTO toResponse(Task task) {
        TaskDTO response = new TaskDTO();
        response.setId(task.getId());
        response.setTitle(task.getTitle());
        response.setDescription(task.getDescription());
        response.setStatus(task.getStatus().name());
        response.setStatusLabel(task.getStatus().getLabel());
        response.setUid(task.getUid());
        response.setCreateTime(toEpochMilli(task.getCreateTime()));
        return response;
    }

    private Long toEpochMilli(java.time.LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    private TaskStatus parseStatus(String status) {
        if (!StringUtils.hasText(status)) {
            return null;
        }
        try {
            return TaskStatus.valueOf(status);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
