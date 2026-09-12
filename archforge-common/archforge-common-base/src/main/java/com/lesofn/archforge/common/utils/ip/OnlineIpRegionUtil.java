package com.lesofn.archforge.common.utils.ip;

import com.lesofn.archforge.common.utils.jackson.JsonUtil;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;

/**
 * query geography address from ip
 *
 * @author sofn
 */
@Slf4j
public class OnlineIpRegionUtil {

    private OnlineIpRegionUtil() {
    }

    /** website for query geography address from ip */
    public static final String ADDRESS_QUERY_SITE = "http://whois.pconline.com.cn/ipJson.jsp";

    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();

    public static @Nullable IpRegion getIpRegion(@Nullable String ip) {
        if (StringUtils.isBlank(ip) || IpUtil.isValidIpv6(ip) || !IpUtil.isValidIpv4(ip)) {
            return null;
        }

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(ADDRESS_QUERY_SITE + "?ip=" + ip + "&json=true"))
                    .timeout(Duration.ofSeconds(10))
                    .GET()
                    .build();
            String rspStr = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString()).body();

            if (StringUtils.isEmpty(rspStr)) {
                log.error("获取地理位置异常 {}", ip);
                return null;
            }

            String province = JsonUtil.getAsString(rspStr, "pro");
            String city = JsonUtil.getAsString(rspStr, "city");
            return new IpRegion(province, city);
        } catch (Exception e) {
            log.error("获取地理位置异常 {}", ip, e);
        }
        return null;
    }
}
