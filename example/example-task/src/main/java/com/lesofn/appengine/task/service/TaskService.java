package com.lesofn.appengine.task.service;

import com.lesofn.appengine.task.dao.TaskDao;
import com.lesofn.appengine.task.domain.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

/**
 * Authors: sofn
 * Version: 1.0  Created at 2015-10-12 00:17.
 */
@Service
public class TaskService {
    private TaskDao taskDao;

    public Task getTask(Long id) {
        return taskDao.findById(id).orElse(null);
    }

    public void saveTask(Task entity) {
        taskDao.save(entity);
    }

    public boolean deleteTask(Long id) {
        Task task = getTask(id);
        if (task == null) {
            return false;
        }
        taskDao.deleteById(id);
        return true;
    }

    public Page<Task> getTasksByPage(long uid, PageRequest request) {
        return taskDao.findByUid(uid, request);
    }

    @Autowired
    public void setTaskDao(TaskDao taskDao) {
        this.taskDao = taskDao;
    }
}
