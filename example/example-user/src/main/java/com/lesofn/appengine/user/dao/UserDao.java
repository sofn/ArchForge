package com.lesofn.appengine.user.dao;

import com.lesofn.appengine.user.domain.User;
import org.springframework.data.repository.CrudRepository;

public interface UserDao extends CrudRepository<User, Long> {

    User findByUsername(String username);
}
