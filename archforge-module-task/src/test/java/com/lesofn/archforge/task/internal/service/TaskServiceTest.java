package com.lesofn.archforge.task.internal.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.lesofn.archforge.task.api.domain.Task;
import com.lesofn.archforge.task.api.domain.TaskStatus;
import com.lesofn.archforge.task.api.dto.TaskCreateRequest;
import com.lesofn.archforge.task.api.dto.TaskListRequest;
import com.lesofn.archforge.task.api.dto.TaskPageResponse;
import com.lesofn.archforge.task.api.dto.TaskResponse;
import com.lesofn.archforge.task.api.dto.TaskUpdateRequest;
import com.lesofn.archforge.task.api.errors.TaskErrorCode;
import com.lesofn.archforge.task.api.errors.TaskException;
import com.lesofn.archforge.task.internal.repository.TaskDao;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

class TaskServiceTest {

    private final TaskDao taskDao = mock(TaskDao.class);
    private final TaskService service = new TaskService(taskDao);

    @Test
    void createSavesAnOpenTaskOwnedByTheCaller() {
        when(taskDao.save(any(Task.class))).thenAnswer(invocation -> {
            Task task = invocation.getArgument(0);
            task.setId(41L);
            return task;
        });
        TaskCreateRequest request = new TaskCreateRequest();
        request.setTitle("ship it");
        request.setDescription("by friday");

        assertEquals(41L, service.createTask(request, 9L));

        ArgumentCaptor<Task> saved = ArgumentCaptor.forClass(Task.class);
        verify(taskDao).save(saved.capture());
        assertEquals("ship it", saved.getValue().getTitle());
        assertEquals(9L, saved.getValue().getUid());
        assertEquals(TaskStatus.CREATED, saved.getValue().getStatus());
    }

    @Test
    void aMissingTaskIsATaskError() {
        when(taskDao.findById(5L)).thenReturn(Optional.empty());

        TaskException e = assertThrows(TaskException.class, () -> service.startTask(5L));

        assertEquals(TaskErrorCode.TASK_NOT_EXISTS.getCode(), e.getErrorInfo().getCode());
        assertFalse(service.getTask(5L).isPresent());
    }

    @Test
    void lifecycleCommandsPersistTheNewState() {
        Task task = Task.create("t", null, 1L);
        when(taskDao.findById(3L)).thenReturn(Optional.of(task));

        assertTrue(service.startTask(3L));
        assertEquals(TaskStatus.IN_PROGRESS, task.getStatus());
        assertTrue(service.completeTask(3L));
        assertEquals(TaskStatus.COMPLETED, task.getStatus());
        verify(taskDao, org.mockito.Mockito.times(2)).save(task);
    }

    @Test
    void cancelAndDeleteWorkOnOpenTasks() {
        Task cancelled = Task.create("a", null, 1L);
        Task deleted = Task.create("b", null, 1L);
        when(taskDao.findById(1L)).thenReturn(Optional.of(cancelled));
        when(taskDao.findById(2L)).thenReturn(Optional.of(deleted));

        assertTrue(service.cancelTask(1L));
        assertTrue(service.deleteTask(2L));

        assertEquals(TaskStatus.CANCELLED, cancelled.getStatus());
        assertTrue(deleted.getDeleted());
    }

    @Test
    void updateRenamesAndReassignsWhenAnOwnerIsGiven() {
        Task task = Task.create("old", null, 1L);
        when(taskDao.findById(8L)).thenReturn(Optional.of(task));
        TaskUpdateRequest request = new TaskUpdateRequest();
        request.setId(8L);
        request.setTitle("new");
        request.setUid(4L);

        assertTrue(service.updateTask(request));

        assertEquals("new", task.getTitle());
        assertEquals(4L, task.getUid());
        verify(taskDao).save(task);
    }

    @Test
    @SuppressWarnings("unchecked")
    void searchPagesWithDefaultsAndMapsRows() {
        Task task = Task.create("row", "d", 6L);
        task.setId(11L);
        LocalDateTime created = LocalDateTime.of(2026, 10, 8, 12, 0);
        task.setCreateTime(created);
        when(taskDao.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(task), PageRequest.of(0, 10), 1));
        TaskListRequest request = new TaskListRequest();
        request.setCurrentPage(0);
        request.setPageSize(0);
        request.setStatus("NOT_A_STATUS");

        TaskPageResponse<TaskResponse> page = service.searchTasks(request);

        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
        verify(taskDao).findAll(any(Specification.class), pageable.capture());
        assertEquals(0, pageable.getValue().getPageNumber());
        assertEquals(10, pageable.getValue().getPageSize());
        TaskResponse row = page.getList().get(0);
        assertEquals(11L, row.getId());
        assertEquals("CREATED", row.getStatus());
        assertEquals("待处理", row.getStatusLabel());
        assertEquals(created.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(), row.getCreateTime());
        assertEquals(1L, page.getTotal());
    }

    @Test
    void pagingByOwnerDelegatesToTheDao() {
        Page<Task> expected = new PageImpl<>(List.of());
        when(taskDao.findByUid(3L, PageRequest.of(0, 5))).thenReturn(expected);

        assertEquals(expected, service.getTasksByPage(3L, PageRequest.of(0, 5)));
    }
}
