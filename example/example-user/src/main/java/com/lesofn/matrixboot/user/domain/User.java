package com.lesofn.matrixboot.user.domain;

import com.alibaba.fastjson.annotation.JSONField;
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
    @JSONField(serialize = false)
    private String password;
    @JSONField(serialize = false)
    private String salt;

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }
}