package com.lesofn.appengine.auth.annotation;

/**
 * @author x-spirit
 * @author sofn
 */
public enum ApiStatus {
    DEV(1, "开发中"),
    BETA(1, "Beta内测"),
    INTERNAL(1, "已上线的内部接口"),
    COOPERATE(3, "已上线的合作方接口"),
    PUBLIC(4, "已上线的公开接口");

    private int id;

    private String desc;

    ApiStatus(int id, String desc) {
        this.id = id;
        this.desc = desc;
    }

    public int getId() {
        return id;
    }

    public String getDesc() {
        return desc;
    }
}
