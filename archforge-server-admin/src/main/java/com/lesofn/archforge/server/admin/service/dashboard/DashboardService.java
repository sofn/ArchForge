package com.lesofn.archforge.server.admin.service.dashboard;

import com.lesofn.archforge.cms.api.service.CmsArticleService;
import com.lesofn.archforge.meta.table.api.service.MetaDefinitionRegistry;
import com.lesofn.archforge.user.api.service.SysUserService;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final SysUserService userService;
    private final CmsArticleService articleService;
    private final MetaDefinitionRegistry metaDefinitionRegistry;

    public DashboardMetricsResponse metrics() {
        return new DashboardMetricsResponse(userService.countNotDeleted(), articleService
                .countNotDeleted(), metaDefinitionRegistry.count(), 0L);
    }

    public List<DashboardTrendPoint> trends(int days) {
        int window = days > 0 ? days : 7;
        List<DashboardTrendPoint> points = new ArrayList<>();
        long users = userService.countNotDeleted();
        long articles = articleService.countNotDeleted();
        for (int i = window - 1; i >= 0; i--) {
            LocalDate date = LocalDate.now(ZoneId.systemDefault()).minusDays(i);
            points.add(new DashboardTrendPoint(date.toString(), users, articles));
        }
        return points;
    }

    public List<DashboardActivity> recentActivities() {
        return articleService
                .latest(8)
                .stream()
                .map(article -> new DashboardActivity("article", article.getTitle() != null ? article.getTitle()
                        : "article-" + article.getId(), article.getUpdateTime() != null ? article.getUpdateTime().toString()
                                : ""))
                .toList();
    }

    public List<DashboardTodo> todo() {
        return List.of(
                new DashboardTodo("Users", userService.countNotDeleted(), "/welcome"),
                new DashboardTodo("Articles", articleService.countNotDeleted(), "/cms/article/index"),
                new DashboardTodo("Meta tables", metaDefinitionRegistry.count(), "/metatable"));
    }
}
