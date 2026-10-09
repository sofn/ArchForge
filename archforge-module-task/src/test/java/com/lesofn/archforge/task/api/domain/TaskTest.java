package com.lesofn.archforge.task.api.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.lesofn.archforge.task.api.errors.TaskErrorCode;
import com.lesofn.archforge.task.api.errors.TaskException;
import java.util.EnumSet;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

/** The task lifecycle: CREATED → IN_PROGRESS → COMPLETED, CANCELLED from either open state, nothing after done. */
class TaskTest {

    @Test
    void transitionsFollowTheLifecycle() {
        assertEquals(EnumSet.of(TaskStatus.IN_PROGRESS, TaskStatus.CANCELLED), allowedFrom(TaskStatus.CREATED));
        assertEquals(EnumSet.of(TaskStatus.COMPLETED, TaskStatus.CANCELLED), allowedFrom(TaskStatus.IN_PROGRESS));
        assertEquals(EnumSet.noneOf(TaskStatus.class), allowedFrom(TaskStatus.COMPLETED));
        assertEquals(EnumSet.noneOf(TaskStatus.class), allowedFrom(TaskStatus.CANCELLED));
    }

    @Test
    void aNewTaskIsOpenAndNotDeleted() {
        Task task = Task.create("write tests", "module-task had none", 7L);

        assertEquals(TaskStatus.CREATED, task.getStatus());
        assertEquals(7L, task.getUid());
        assertFalse(task.getDeleted());
        assertFalse(task.isDone());
        assertEquals("待处理", task.getStatus().getLabel());
    }

    @Test
    void aTaskCanBeStartedAndCompleted() {
        Task task = Task.create("t", null, 1L);

        task.start();
        task.complete();

        assertEquals(TaskStatus.COMPLETED, task.getStatus());
        assertTrue(task.isDone());
    }

    @Test
    void skippingAStateIsRejected() {
        Task task = Task.create("t", null, 1L);

        assertError(TaskErrorCode.TASK_STATUS_TRANSITION_INVALID, task::complete);
        assertEquals(TaskStatus.CREATED, task.getStatus());
    }

    @Test
    void aDoneTaskCannotBeEditedReassignedDeletedOrReopened() {
        Task task = Task.create("t", null, 1L);
        task.cancel();

        assertError(TaskErrorCode.TASK_ALREADY_DONE, () -> task.updateInfo("x", null));
        assertError(TaskErrorCode.TASK_ALREADY_DONE, () -> task.reassign(2L));
        assertError(TaskErrorCode.TASK_ALREADY_DONE, task::softDelete);
        assertError(TaskErrorCode.TASK_STATUS_TRANSITION_INVALID, task::start);
        assertEquals("t", task.getTitle());
        assertEquals(1L, task.getUid());
    }

    @Test
    void anOpenTaskCanBeEditedReassignedAndDeleted() {
        Task task = Task.create("t", null, 1L);
        task.start();

        task.updateInfo("renamed", "details");
        task.reassign(2L);
        task.softDelete();

        assertEquals("renamed", task.getTitle());
        assertEquals("details", task.getDescription());
        assertEquals(2L, task.getUid());
        assertTrue(task.getDeleted());
    }

    private static EnumSet<TaskStatus> allowedFrom(TaskStatus from) {
        EnumSet<TaskStatus> allowed = EnumSet.noneOf(TaskStatus.class);
        for (TaskStatus target : TaskStatus.values()) {
            if (from.canTransitionTo(target)) {
                allowed.add(target);
            }
        }
        return allowed;
    }

    private static void assertError(TaskErrorCode expected, Executable action) {
        TaskException e = assertThrows(TaskException.class, action);
        assertEquals(expected.getCode(), e.getErrorInfo().getCode());
    }
}
