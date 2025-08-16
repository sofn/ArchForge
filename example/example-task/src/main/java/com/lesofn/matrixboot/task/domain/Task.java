package com.lesofn.matrixboot.task.domain;

import lombok.*;

import jakarta.persistence.*;

/**
 * Authors: sofn
 * Version: 1.0  Created at 2015-10-12 00:12.
 */
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

    public Task(String title, String description, long uid) {
        this.title = title;
        this.description = description;
        this.uid = uid;
    }
}
