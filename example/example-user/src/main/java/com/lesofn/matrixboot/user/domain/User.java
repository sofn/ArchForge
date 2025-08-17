package com.lesofn.matrixboot.user.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import jakarta.persistence.*;
import java.io.Serializable;

@Setter
@Getter
@EqualsAndHashCode(of = "uid")
@Entity
@NoArgsConstructor
@ToString
@Table(name = "user")
public class User implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long uid;
    private String username;
    @JsonIgnore
    private String password;
    @JsonIgnore
    private String salt;

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }
}