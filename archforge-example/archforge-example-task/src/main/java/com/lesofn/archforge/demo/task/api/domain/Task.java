package com.lesofn.archforge.demo.task.api.domain;

import com.lesofn.archforge.common.repository.BaseEntity;
import com.lesofn.archforge.demo.task.api.errors.TaskErrorCode;
import com.lesofn.archforge.demo.task.api.errors.TaskException;
import jakarta.persistence.*;
import lombok.*;

/** Authors: sofn Version: 1.0 Created at 2015-10-12 00:12. */
@Setter
@Getter
@ToString
@EqualsAndHashCode(of = "id", callSuper = false)
@NoArgsConstructor
@Entity
@Table(name = "task")
public class Task extends BaseEntity<Task> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    public String title;

    public String description;

    @Column(name = "uid", nullable = false)
    public long uid;

    @Enumerated(EnumType.STRING)
    private TaskStatus status = TaskStatus.CREATED;

    public Task(String title, String description, long uid) {
        this.title = title;
        this.description = description;
        this.uid = uid;
    }

    public static Task create(String title, String description, long uid) {
        Task task = new Task(title, description, uid);
        task.setDeleted(false);
        return task;
    }

    public void updateInfo(String title, String description) {
        if (this.status == TaskStatus.COMPLETED || this.status == TaskStatus.CANCELLED) {
            throw new TaskException(TaskErrorCode.TASK_ALREADY_DONE);
        }
        this.title = title;
        this.description = description;
    }

    public void start() {
        transition(TaskStatus.IN_PROGRESS);
    }

    public void complete() {
        transition(TaskStatus.COMPLETED);
    }

    public void cancel() {
        transition(TaskStatus.CANCELLED);
    }

    public void reassign(long newUid) {
        if (this.status == TaskStatus.COMPLETED || this.status == TaskStatus.CANCELLED) {
            throw new TaskException(TaskErrorCode.TASK_ALREADY_DONE);
        }
        this.uid = newUid;
    }

    public void softDelete() {
        if (this.status == TaskStatus.COMPLETED || this.status == TaskStatus.CANCELLED) {
            throw new TaskException(TaskErrorCode.TASK_ALREADY_DONE);
        }
        setDeleted(true);
    }

    public boolean isDone() { return this.status == TaskStatus.COMPLETED || this.status == TaskStatus.CANCELLED; }

    private void transition(TaskStatus target) {
        if (!this.status.canTransitionTo(target)) {
            throw new TaskException(TaskErrorCode.TASK_STATUS_TRANSITION_INVALID, this.status, target);
        }
        this.status = target;
    }
}
