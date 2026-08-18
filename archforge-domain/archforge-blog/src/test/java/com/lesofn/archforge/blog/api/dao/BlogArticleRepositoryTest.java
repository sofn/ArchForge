package com.lesofn.archforge.blog.api.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.lesofn.archforge.blog.api.domain.BlogArticle;
import com.lesofn.archforge.blog.api.enums.BlogArticleStatus;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

class BlogArticleRepositoryTest {

    @Test
    void publishedCategoryQueryKeepsPagingContract() {
        Pageable pageable = PageRequest.of(1, 20);
        RecordingRepository repository = new RecordingRepository();

        Page<BlogArticle> page = repository.findByCategoryIdAndStatusAndDeletedFalse(
                8L, BlogArticleStatus.PUBLISHED, pageable);

        assertEquals(8L, repository.categoryId);
        assertEquals(BlogArticleStatus.PUBLISHED, repository.status);
        assertEquals(pageable, repository.pageable);
        assertEquals(0, page.getTotalElements());
    }

    private static final class RecordingRepository {

        private Long categoryId;
        private BlogArticleStatus status;
        private Pageable pageable;

        Page<BlogArticle> findByCategoryIdAndStatusAndDeletedFalse(
                Long categoryId, BlogArticleStatus status, Pageable pageable) {
            this.categoryId = categoryId;
            this.status = status;
            this.pageable = pageable;
            return new PageImpl<>(java.util.List.of(), pageable, 0);
        }
    }
}
