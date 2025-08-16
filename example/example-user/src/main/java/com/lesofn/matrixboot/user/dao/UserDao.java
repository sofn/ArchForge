package com.lesofn.matrixboot.user.dao;

import com.lesofn.matrixboot.user.domain.User;
import org.springframework.data.repository.CrudRepository;

public interface UserDao extends CrudRepository<User, Long> {

    User findByUsername(String username);
}
