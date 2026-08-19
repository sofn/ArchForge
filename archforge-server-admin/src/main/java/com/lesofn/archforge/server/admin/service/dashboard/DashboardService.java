package com.lesofn.archforge.server.admin.service.dashboard;

import com.lesofn.archforge.blog.api.dao.BlogArticleRepository;
import com.lesofn.archforge.meta.table.api.dao.MetaTableRepository;
import com.lesofn.archforge.user.api.dao.SysUserRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final SysUserRepository userRepository;
    private final BlogArticleRepository articleRepository;
    private final MetaTableRepository metaTableRepository;

    public DashboardMetricsResponse metrics() {
        return new DashboardMetricsResponse(userRepository.count(), articleRepository.count(), metaTableRepository.count(), 0L);
    }

    public List<DashboardTrendPoint> trends(int days) {
        int window = days > 0 ? days : 7;
        List<DashboardTrendPoint> points = new ArrayList<>();
        long users = userRepository.count();
        long articles = articleRepository.count();
        for (int i = window - 1; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            points.add(new DashboardTrendPoint(date.toString(), users, articles));
        }
        return points;
    }

    public List<DashboardActivity> recentActivities() {
        return articleRepository
                .findAll(PageRequest.of(0, 8, Sort.by(Sort.Direction.DESC, "id")))
                .stream()
                .map(article -> new DashboardActivity("article", article.getTitle() != null ? article.getTitle()
                        : "article-" + article.getId(), article.getUpdateTime() != null ? article.getUpdateTime().toString()
                                : ""))
                .toList();
    }

    public List<DashboardTodo> todo() {
        return List.of(
                new DashboardTodo("Users", userRepository.count(), "/system/user/index"),
                new DashboardTodo("Articles", articleRepository.count(), "/blog/article"),
                new DashboardTodo("Meta tables", metaTableRepository.count(), "/metatable"));
    }
}
