package com.lesofn.archforge.infrastructure.security.sign;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * API 签名注解。
 *
 * <p>
 * 标注后，服务端将校验请求头中的签名信息：
 * <ul>
 * <li>{@code X-App-Key}：应用标识</li>
 * <li>{@code X-Timestamp}：请求时间戳（毫秒）</li>
 * <li>{@code X-Nonce}：随机字符串（防重放）</li>
 * <li>{@code X-Sign}：HMAC-SHA256(appSecret, appKey + timestamp + nonce + body)</li>
 * </ul>
 *
 * @author sofn
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ApiSign {

    /** 允许的签名时间偏移（秒，默认 300 秒） */
    int timeoutSeconds() default 300;

    /** 签名字段头名 */
    String appKeyHeader() default "X-App-Key";

    String timestampHeader() default "X-Timestamp";

    String nonceHeader() default "X-Nonce";

    String signHeader() default "X-Sign";
}
