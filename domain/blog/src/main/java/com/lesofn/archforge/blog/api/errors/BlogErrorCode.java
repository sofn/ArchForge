package com.lesofn.archforge.blog.api.errors;

import com.lesofn.archforge.common.error.api.ErrorCode;
import com.lesofn.archforge.common.error.manager.ErrorManager;
import com.lesofn.archforge.common.error.ArchForgeProjectModule;
import lombok.Getter;

@Getter
public enum BlogErrorCode implements ErrorCode {
    CATEGORY_NOT_FOUND(1, "分类不存在"),
    ARTICLE_NOT_FOUND(2, "文章不存在"),
    SLUG_EXISTS(3, "URL标识已存在"),
    CATEGORY_HAS_ARTICLES(4, "分类下存在文章，无法删除"),
    STATUS_TRANSITION_INVALID(5, "文章状态转换非法");

    private final int nodeNum;
    private final String msg;

    BlogErrorCode(int nodeNum, String msg) {
        this.nodeNum = nodeNum;
        this.msg = msg;
        ErrorManager.register(ArchForgeProjectModule.BLOG, this);
    }
}
