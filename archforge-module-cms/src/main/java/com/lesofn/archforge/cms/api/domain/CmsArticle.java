package com.lesofn.archforge.cms.api.domain;

import com.lesofn.archforge.cms.api.enums.CmsArticleStatus;
import com.lesofn.archforge.cms.api.errors.CmsErrorCode;
import com.lesofn.archforge.cms.api.errors.CmsException;
import com.lesofn.archforge.common.repository.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.time.ZoneId;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.jspecify.annotations.Nullable;

@Setter
@Getter
@Accessors(chain = true)
@Entity
@Table(name = "cms_article")
@DynamicInsert
@DynamicUpdate
public class CmsArticle extends BaseEntity<CmsArticle> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "category_id", nullable = false)
    private Long categoryId;

    @Column(nullable = false, length = 256)
    private @Nullable String title;

    @Column(nullable = false, unique = true, length = 256)
    private String slug;

    @Column(length = 1024)
    private String summary;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "cover_image_file_id")
    private Long coverImageFileId;

    /** C 端作者/管理员用户 ID */
    @Column(name = "author_id")
    private Long authorId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private CmsArticleStatus status = CmsArticleStatus.DRAFT;

    @Column(name = "publish_time")
    private LocalDateTime publishTime;

    public CmsArticle publish() {
        if (this.status != CmsArticleStatus.DRAFT) {
            throw new CmsException(CmsErrorCode.STATUS_TRANSITION_INVALID);
        }
        this.status = CmsArticleStatus.PUBLISHED;
        this.publishTime = LocalDateTime.now(ZoneId.systemDefault());
        return this;
    }

    public CmsArticle offline() {
        if (this.status != CmsArticleStatus.PUBLISHED) {
            throw new CmsException(CmsErrorCode.STATUS_TRANSITION_INVALID);
        }
        this.status = CmsArticleStatus.OFFLINE;
        return this;
    }
}
