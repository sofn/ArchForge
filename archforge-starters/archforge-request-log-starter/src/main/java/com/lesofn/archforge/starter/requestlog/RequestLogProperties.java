package com.lesofn.archforge.starter.requestlog;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.Ordered;

/**
 * 请求日志配置：打印所有请求参数与返回结果。
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "arch-forge.request-log")
public class RequestLogProperties {

    /** 是否启用请求日志过滤器。 */
    private boolean enabled = true;

    /** 是否记录请求体（仅 JSON/text/form/xml 等可文本化内容）。 */
    private boolean includeRequestPayload = true;

    /** 是否记录响应体（仅 content-type 含 application/json 时写入日志）。 */
    private boolean includeResponsePayload = true;

    /** 单条日志最大长度；超过则降级为摘要行（不打印参数与响应体）。 */
    private int maxPayloadLength = 2048;

    /** 需要脱敏的字段名（不区分大小写），命中后值替换为 ***。 */
    private List<String> maskFields = new ArrayList<>(List.of(
            "password", "old_password", "pwd", "secret", "token", "authorization"));

    /** 不参与日志记录的路径前缀/后缀匹配（静态资源、文档、二进制下载）。 */
    private List<String> excludePatterns = new ArrayList<>(List.of(
            "/webjars", "/static", "/js", "/css", "/libs", "/WEB-INF",
            "/swagger-", "/v3/api-docs", "/file/download/", "/user/export",
            ".html", ".js", ".css", ".png", ".jpg", ".jpeg", ".gif", ".ico", ".svg", ".woff", ".woff2"));

    /** 过滤器顺序，默认排在上下文/请求体缓存过滤器之后。 */
    private int order = Ordered.HIGHEST_PRECEDENCE + 10;
}
