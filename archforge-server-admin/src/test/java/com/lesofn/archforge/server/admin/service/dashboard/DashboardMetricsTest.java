package com.lesofn.archforge.server.admin.service.dashboard;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import com.lesofn.archforge.blog.api.dao.BlogArticleRepository;
import com.lesofn.archforge.demo.task.internal.repository.TaskDao;
import com.lesofn.archforge.meta.table.api.dao.MetaTableRepository;
import com.lesofn.archforge.user.api.dao.SysUserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DashboardMetricsTest {

    @Mock
    private SysUserRepository userRepository;

    @Mock
    private BlogArticleRepository articleRepository;

    @Mock
    private MetaTableRepository metaTableRepository;

    @Mock
    private TaskDao taskRepository;

    @InjectMocks
    private DashboardService dashboardService;

    @Test
    void metricsAggregatesExistingCounts() {
        when(userRepository.count()).thenReturn(12L);
        when(articleRepository.count()).thenReturn(4L);
        when(metaTableRepository.count()).thenReturn(3L);
        when(taskRepository.count()).thenReturn(7L);

        DashboardMetricsResponse metrics = dashboardService.metrics();

        assertEquals(12L, metrics.userCount());
        assertEquals(4L, metrics.articleCount());
        assertEquals(3L, metrics.metaTableCount());
        assertEquals(7L, metrics.taskCount());
    }
}
