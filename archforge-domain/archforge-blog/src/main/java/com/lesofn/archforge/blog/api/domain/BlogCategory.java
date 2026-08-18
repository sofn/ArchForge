package com.lesofn.archforge.blog.api.domain;

import com.lesofn.archforge.common.repository.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

@Setter
@Getter
@Accessors(chain = true)
@Entity
@Table(name = "blog_category")
@DynamicInsert
@DynamicUpdate
public class BlogCategory extends BaseEntity<BlogCategory> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 128)
    private String name;

    @Column(nullable = false, unique = true, length = 128)
    private String slug;

    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    /** 1=显示 0=隐藏 */
    @Column(nullable = false)
    private Integer status = 1;

    public boolean isVisible() { return Integer.valueOf(1).equals(this.status); }
}
