package com.lesofn.appengine.common.config;

/**
 * 定义运行时环境，注意和gradle环境区分
 */
public abstract class ProfileLoader {

    // 默认线上环境，防止线上bug
    Env DEFAULT_DEV = Env.prod;

    // 环境变量
    enum Env {
        dev,
        test,
        prod
    }

    abstract Env getEnv();
}
