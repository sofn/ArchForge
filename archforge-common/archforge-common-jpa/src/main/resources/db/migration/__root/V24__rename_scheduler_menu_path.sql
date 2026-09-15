-- V24: Drop Quartz branding from the scheduler menu (path + router name).
-- REST moved from /quartz to /admin/scheduler-job; the admin view lives at
-- /system/scheduler-job/index. Permission codes stay monitor:job:* (controller)
-- and system:quartz:list is rewritten to system:scheduler-job:list for the menu row.
UPDATE sys_menu
SET path = '/system/scheduler-job/index',
    router_name = 'SystemSchedulerJob',
    permission = 'system:scheduler-job:list',
    remark = 'db-scheduler 反射调度任务管理'
WHERE menu_id = 100;
