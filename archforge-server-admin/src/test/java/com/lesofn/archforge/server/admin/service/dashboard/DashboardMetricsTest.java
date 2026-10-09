package com.lesofn.archforge.server.admin.service.dashboard;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import com.lesofn.archforge.cms.api.service.CmsArticleService;
import com.lesofn.archforge.meta.table.api.service.MetaDefinitionRegistry;
import com.lesofn.archforge.user.api.service.SysUserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DashboardMetricsTest {

    @Mock
    private SysUserService userService;

    @Mock
    private CmsArticleService articleService;

    @Mock
    private MetaDefinitionRegistry metaDefinitionRegistry;

    @InjectMocks
    private DashboardService dashboardService;

    @Test
    void metricsAggregatesExistingCounts() {
        when(userService.countNotDeleted()).thenReturn(12L);
        when(articleService.countNotDeleted()).thenReturn(4L);
        when(metaDefinitionRegistry.count()).thenReturn(3L);

        DashboardMetricsResponse metrics = dashboardService.metrics();

        assertEquals(12L, metrics.userCount());
        assertEquals(4L, metrics.articleCount());
        assertEquals(3L, metrics.metaTableCount());
        assertEquals(0L, metrics.taskCount());
    }
}
