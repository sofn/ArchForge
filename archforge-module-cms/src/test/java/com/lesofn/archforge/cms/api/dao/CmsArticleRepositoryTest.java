package com.lesofn.archforge.cms.api.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.lesofn.archforge.cms.api.domain.CmsArticle;
import org.jspecify.annotations.Nullable;
import com.lesofn.archforge.cms.api.enums.CmsArticleStatus;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

class CmsArticleRepositoryTest {

    @Test
    void publishedCategoryQueryKeepsPagingContract() {
        Pageable pageable = PageRequest.of(1, 20);
        RecordingRepository repository = new RecordingRepository();

        Page<CmsArticle> page = repository.findByCategoryIdAndStatusAndDeletedFalse(
                8L, CmsArticleStatus.PUBLISHED, pageable);

        assertEquals(8L, repository.categoryId);
        assertEquals(CmsArticleStatus.PUBLISHED, repository.status);
        assertEquals(pageable, repository.pageable);
        assertEquals(0, page.getTotalElements());
    }

    private static final class RecordingRepository {

        private @Nullable Long categoryId;
        private @Nullable CmsArticleStatus status;
        private @Nullable Pageable pageable;

        Page<CmsArticle> findByCategoryIdAndStatusAndDeletedFalse(
                Long categoryId, CmsArticleStatus status, Pageable pageable) {
            this.categoryId = categoryId;
            this.status = status;
            this.pageable = pageable;
            return new PageImpl<>(java.util.List.of(), pageable, 0);
        }
    }
}
