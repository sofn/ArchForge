package com.lesofn.archforge.server.admin.service.dashboard;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import com.lesofn.archforge.cms.api.service.CmsArticleService;
import com.lesofn.archforge.cms.api.domain.CmsArticle;
import com.lesofn.archforge.cms.testing.ArticleTestBuilder;
import com.lesofn.archforge.meta.table.api.service.MetaDefinitionRegistry;
import com.lesofn.archforge.user.api.service.SysUserService;
import java.util.List;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Unit tests for {@link DashboardService}. */
@Tag("P1")
@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private SysUserService userService;
    @Mock
    private CmsArticleService articleService;
    @Mock
    private MetaDefinitionRegistry metaDefinitionRegistry;

    @InjectMocks
    private DashboardService service;

    @Test
    void metricsCountsLiveRowsOnly() {
        when(userService.countNotDeleted()).thenReturn(11L);
        when(articleService.countNotDeleted()).thenReturn(22L);
        when(metaDefinitionRegistry.count()).thenReturn(33L);

        DashboardMetricsResponse metrics = service.metrics();

        assertEquals(11L, metrics.userCount());
        assertEquals(22L, metrics.articleCount());
        assertEquals(33L, metrics.metaTableCount());
        assertEquals(0L, metrics.taskCount());
    }

    @Test
    void trendsDefaultsToSevenDaysAndRepeatsCurrentTotals() {
        when(userService.countNotDeleted()).thenReturn(5L);
        when(articleService.countNotDeleted()).thenReturn(6L);

        List<DashboardTrendPoint> points = service.trends(0);

        assertEquals(7, points.size());
        points.forEach(point -> {
            assertEquals(5L, point.users());
            assertEquals(6L, point.articles());
        });
    }

    @Test
    void trendsHonorsPositiveWindow() {
        List<DashboardTrendPoint> points = service.trends(3);

        assertEquals(3, points.size());
    }

    @Test
    void recentActivitiesFallsBackToIdWhenTitleMissing() {
        CmsArticle titled = ArticleTestBuilder.anArticle().withId(1L).withTitle("Hello").build();
        // Builders default the title; clear it to exercise the service fallback.
        CmsArticle untitled = ArticleTestBuilder.anArticle().withId(2L).build();
        untitled.setTitle(null);
        when(articleService.latest(8)).thenReturn(List.of(titled, untitled));

        List<DashboardActivity> activities = service.recentActivities();

        assertEquals(2, activities.size());
        assertEquals("article", activities.get(0).type());
        assertEquals("Hello", activities.get(0).title());
        assertEquals("article-2", activities.get(1).title());
    }

    @Test
    void todoListsAllCountersWithLinks() {
        when(userService.countNotDeleted()).thenReturn(1L);
        when(articleService.countNotDeleted()).thenReturn(2L);
        when(metaDefinitionRegistry.count()).thenReturn(3L);

        List<DashboardTodo> todos = service.todo();

        assertEquals(3, todos.size());
        assertEquals("/welcome", todos.get(0).href());
        assertEquals("/cms/article/index", todos.get(1).href());
        assertEquals("/metatable", todos.get(2).href());
    }
}
