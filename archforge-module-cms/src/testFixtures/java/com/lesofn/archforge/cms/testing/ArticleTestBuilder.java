package com.lesofn.archforge.cms.testing;

import com.lesofn.archforge.cms.api.domain.CmsArticle;
import com.lesofn.archforge.cms.api.enums.CmsArticleStatus;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Test data builder for {@link CmsArticle}. Produces valid-by-default draft articles.
 */
public final class ArticleTestBuilder {

    private static final AtomicLong SEQUENCE = new AtomicLong();

    private final CmsArticle article = new CmsArticle();

    private ArticleTestBuilder() {
    }

    public static ArticleTestBuilder anArticle() {
        return new ArticleTestBuilder();
    }

    public ArticleTestBuilder withId(Long id) {
        article.setId(id);
        return this;
    }

    public ArticleTestBuilder withTitle(String title) {
        article.setTitle(title);
        return this;
    }

    public ArticleTestBuilder withSlug(String slug) {
        article.setSlug(slug);
        return this;
    }

    public ArticleTestBuilder withSummary(String summary) {
        article.setSummary(summary);
        return this;
    }

    public ArticleTestBuilder withContent(String content) {
        article.setContent(content);
        return this;
    }

    public ArticleTestBuilder withCategoryId(Long categoryId) {
        article.setCategoryId(categoryId);
        return this;
    }

    public ArticleTestBuilder withAuthorId(Long authorId) {
        article.setAuthorId(authorId);
        return this;
    }

    public ArticleTestBuilder withCoverImageFileId(Long coverImageFileId) {
        article.setCoverImageFileId(coverImageFileId);
        return this;
    }

    /** Marks the article published without going through the domain state machine. */
    public ArticleTestBuilder published() {
        article.setStatus(CmsArticleStatus.PUBLISHED);
        article.setPublishTime(LocalDateTime.now(ZoneId.systemDefault()));
        return this;
    }

    public ArticleTestBuilder offline() {
        article.setStatus(CmsArticleStatus.OFFLINE);
        return this;
    }

    public CmsArticle build() {
        long seq = SEQUENCE.incrementAndGet();
        if (article.getTitle() == null) {
            article.setTitle("测试文章" + seq);
        }
        if (article.getSlug() == null) {
            article.setSlug("article-" + seq);
        }
        if (article.getContent() == null) {
            article.setContent("<p>测试内容" + seq + "</p>");
        }
        if (article.getSummary() == null) {
            article.setSummary("测试摘要" + seq);
        }
        if (article.getCategoryId() == null) {
            article.setCategoryId(1L);
        }
        if (article.getStatus() == null) {
            article.setStatus(CmsArticleStatus.DRAFT);
        }
        if (article.getDeleted() == null) {
            article.setDeleted(false);
        }
        return article;
    }
}
