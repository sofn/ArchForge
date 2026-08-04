package com.lesofn.archforge.blog.api.domain;

import com.lesofn.archforge.blog.api.enums.BlogArticleStatus;
import com.lesofn.archforge.blog.api.errors.BlogErrorCode;
import com.lesofn.archforge.blog.api.errors.BlogException;
import com.lesofn.archforge.common.repository.BaseEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

@Setter
@Getter
@Accessors(chain = true)
@Entity
@Table(name = "blog_article")
@DynamicInsert
@DynamicUpdate
public class BlogArticle extends BaseEntity<BlogArticle> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "category_id", nullable = false)
    private Long categoryId;

    @Column(nullable = false, length = 256)
    private String title;

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
    private BlogArticleStatus status = BlogArticleStatus.DRAFT;

    @Column(name = "publish_time")
    private LocalDateTime publishTime;

    public BlogArticle publish() {
        if (this.status != BlogArticleStatus.DRAFT) {
            throw new BlogException(BlogErrorCode.STATUS_TRANSITION_INVALID);
        }
        this.status = BlogArticleStatus.PUBLISHED;
        this.publishTime = LocalDateTime.now();
        return this;
    }

    public BlogArticle offline() {
        if (this.status != BlogArticleStatus.PUBLISHED) {
            throw new BlogException(BlogErrorCode.STATUS_TRANSITION_INVALID);
        }
        this.status = BlogArticleStatus.OFFLINE;
        return this;
    }
}
