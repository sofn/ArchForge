package com.lesofn.archsmith.demo.task.domain;

import com.google.common.base.Preconditions;
import jakarta.persistence.*;
import lombok.*;

/** Authors: sofn Version: 1.0 Created at 2015-10-12 00:12. */
@Setter
@Getter
@ToString
@EqualsAndHashCode(of = "id")
@NoArgsConstructor
@Entity
@Table(name = "task")
public class Task {

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
        Preconditions.checkState(
                this.status == TaskStatus.CREATED || this.status == TaskStatus.IN_PROGRESS,
                "Cannot reassign task in status: %s",
                this.status);
        this.uid = newUid;
    }

    private void transition(TaskStatus target) {
        Preconditions.checkState(
                this.status.canTransitionTo(target),
                "Cannot transition from %s to %s",
                this.status,
                target);
        this.status = target;
    }
}
