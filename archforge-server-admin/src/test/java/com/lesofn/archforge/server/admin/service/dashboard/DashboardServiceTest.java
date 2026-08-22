package com.lesofn.archforge.server.admin.service.dashboard;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import com.lesofn.archforge.blog.api.dao.BlogArticleRepository;
import com.lesofn.archforge.blog.api.domain.BlogArticle;
import com.lesofn.archforge.blog.testing.ArticleTestBuilder;
import com.lesofn.archforge.meta.table.api.dao.MetaTableRepository;
import com.lesofn.archforge.user.api.dao.SysUserRepository;
import java.util.List;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

/** Unit tests for {@link DashboardService}. */
@Tag("P1")
@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private SysUserRepository userRepository;
    @Mock
    private BlogArticleRepository articleRepository;
    @Mock
    private MetaTableRepository metaTableRepository;

    @InjectMocks
    private DashboardService service;

    @Test
    void metricsCountsLiveRowsOnly() {
        when(userRepository.countByDeletedFalse()).thenReturn(11L);
        when(articleRepository.countByDeletedFalse()).thenReturn(22L);
        when(metaTableRepository.countByDeletedFalse()).thenReturn(33L);

        DashboardMetricsResponse metrics = service.metrics();

        assertEquals(11L, metrics.userCount());
        assertEquals(22L, metrics.articleCount());
        assertEquals(33L, metrics.metaTableCount());
        assertEquals(0L, metrics.taskCount());
    }

    @Test
    void trendsDefaultsToSevenDaysAndRepeatsCurrentTotals() {
        when(userRepository.countByDeletedFalse()).thenReturn(5L);
        when(articleRepository.countByDeletedFalse()).thenReturn(6L);

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
        BlogArticle titled = ArticleTestBuilder.anArticle().withId(1L).withTitle("Hello").build();
        // Builders default the title; clear it to exercise the service fallback.
        BlogArticle untitled = ArticleTestBuilder.anArticle().withId(2L).build();
        untitled.setTitle(null);
        when(articleRepository.findAll(org.mockito.ArgumentMatchers.any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(titled, untitled)));

        List<DashboardActivity> activities = service.recentActivities();

        assertEquals(2, activities.size());
        assertEquals("article", activities.get(0).type());
        assertEquals("Hello", activities.get(0).title());
        assertEquals("article-2", activities.get(1).title());
    }

    @Test
    void todoListsAllCountersWithLinks() {
        when(userRepository.countByDeletedFalse()).thenReturn(1L);
        when(articleRepository.countByDeletedFalse()).thenReturn(2L);
        when(metaTableRepository.countByDeletedFalse()).thenReturn(3L);

        List<DashboardTodo> todos = service.todo();

        assertEquals(3, todos.size());
        assertEquals("/welcome", todos.get(0).href());
        assertEquals("/blog/article/index", todos.get(1).href());
        assertEquals("/metatable", todos.get(2).href());
    }
}
