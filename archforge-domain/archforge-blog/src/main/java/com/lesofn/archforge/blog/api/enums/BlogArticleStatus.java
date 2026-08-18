package com.lesofn.archforge.blog.api.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BlogArticleStatus {
    DRAFT("草稿"),
    PUBLISHED("已发布"),
    OFFLINE("已下线");

    private final String label;

    public boolean isPublished() { return this == PUBLISHED; }
}
