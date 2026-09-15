package com.lesofn.archforge.server.web.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.lesofn.archforge.cms.api.domain.CmsArticle;
import com.lesofn.archforge.cms.api.domain.CmsCategory;
import com.lesofn.archforge.cms.api.service.CmsArticleService;
import com.lesofn.archforge.cms.api.service.CmsCategoryService;
import com.lesofn.archforge.cms.testing.ArticleTestBuilder;
import com.lesofn.archforge.infrastructure.auth.LoginContext;
import com.lesofn.archforge.user.api.service.SysFileService;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class WebCmsControllerTest {

    @Mock
    private CmsCategoryService categoryService;

    @Mock
    private CmsArticleService articleService;

    @Mock
    private SysFileService sysFileService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        WebCmsController controller = new WebCmsController(categoryService, articleService, sysFileService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        LoginContext.setWebUser(7L, "alice");
    }

    @AfterEach
    void tearDown() {
        LoginContext.clearWebUser();
    }

    @Test
    void categoriesIncludePublishedArticleCount() throws Exception {
        CmsCategory category = new CmsCategory().setName("技术").setSlug("tech");
        category.setId(1L);
        when(categoryService.listVisible()).thenReturn(List.of(category));
        when(articleService.pagePublished(any(PageRequest.class), eq(1L), isNull()))
                .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 1), 3));

        mockMvc.perform(get("/web/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("技术"))
                .andExpect(jsonPath("$[0].slug").value("tech"))
                .andExpect(jsonPath("$[0].articleCount").value(3));
    }

    @Test
    void articlesReturnsPagedSummaries() throws Exception {
        CmsArticle article = ArticleTestBuilder.anArticle()
                .withSlug("hello-world")
                .published()
                .build();
        CmsCategory category = new CmsCategory().setName("技术").setSlug("tech");
        when(articleService.pagePublished(any(), isNull(), isNull()))
                .thenReturn(new PageImpl<>(List.of(article)));
        when(categoryService.findById(article.getCategoryId())).thenReturn(Optional.of(category));

        mockMvc.perform(get("/web/articles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.currentPage").value(1))
                .andExpect(jsonPath("$.list[0].slug").value("hello-world"))
                .andExpect(jsonPath("$.list[0].categoryName").value("技术"));
    }

    @Test
    void articleDetailResolvesCategory() throws Exception {
        CmsArticle article = ArticleTestBuilder.anArticle()
                .withSlug("detail-slug")
                .published()
                .build();
        CmsCategory category = new CmsCategory().setName("随笔").setSlug("essay");
        when(articleService.findPublishedBySlug("detail-slug")).thenReturn(Optional.of(article));
        when(categoryService.findById(article.getCategoryId())).thenReturn(Optional.of(category));

        mockMvc.perform(get("/web/articles/detail-slug"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.slug").value("detail-slug"))
                .andExpect(jsonPath("$.title").value(article.getTitle()))
                .andExpect(jsonPath("$.categoryName").value("随笔"));
    }

    @Test
    void createArticleReturnsGeneratedId() throws Exception {
        when(articleService.create(any(CmsArticle.class))).thenAnswer(inv -> {
            CmsArticle a = inv.getArgument(0);
            a.setId(99L);
            return a;
        });

        mockMvc.perform(post("/web/articles")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"categoryId\":1,\"title\":\"Hello\",\"content\":\"<p>x</p>\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(99));
    }

    @Test
    void myArticlesScopesToCurrentUser() throws Exception {
        CmsArticle article = ArticleTestBuilder.anArticle()
                .withAuthorId(7L)
                .withTitle("我的文章")
                .build();
        when(articleService.pageByAuthorId(any(), eq(7L))).thenReturn(new PageImpl<>(List.of(article)));
        when(categoryService.findById(article.getCategoryId())).thenReturn(Optional.empty());

        mockMvc.perform(get("/web/user/articles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.list[0].title").value("我的文章"));
    }
}
