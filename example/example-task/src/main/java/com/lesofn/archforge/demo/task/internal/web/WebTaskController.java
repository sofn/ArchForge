package com.lesofn.archforge.demo.task.internal.web;

import com.lesofn.archforge.demo.task.api.domain.Task;
import com.lesofn.archforge.demo.task.internal.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 任务界面跳转Controller
 *
 * <p>
 * Authors: sofn Version: 1.0 Created at 2015-10-22 00:11.
 */
@Controller
@RequiredArgsConstructor
@RequestMapping("/web/task")
public class WebTaskController {

    private final TaskService taskService;

    @RequestMapping(method = RequestMethod.GET)
    public String list() {
        return "task/list";
    }

    @RequestMapping(value = "save", method = RequestMethod.GET)
    public String save(@RequestParam(required = false, defaultValue = "0") long id, Model model) {
        Task task = null;
        if (id > 0) {
            task = taskService.getTask(id).orElse(null);
        }
        model.addAttribute("task", task);
        return "task/save";
    }
}
