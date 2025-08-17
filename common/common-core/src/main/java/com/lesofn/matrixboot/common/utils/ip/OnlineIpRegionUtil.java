package com.lesofn.matrixboot.common.utils.ip;

import com.lesofn.matrixboot.common.utils.jackson.JacksonUtil;
import kong.unirest.core.GetRequest;
import kong.unirest.core.Unirest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * query geography address from ip
 *
 * @author sofn
 */
@Slf4j
public class OnlineIpRegionUtil {

    private OnlineIpRegionUtil() {
    }

    /**
     * website for query geography address from ip
     */
    public static final String ADDRESS_QUERY_SITE = "http://whois.pconline.com.cn/ipJson.jsp";


    public static IpRegion getIpRegion(String ip) {
        if (StringUtils.isBlank(ip) || IpUtil.isValidIpv6(ip) || !IpUtil.isValidIpv4(ip)) {
            return null;
        }

        try {
            GetRequest request = Unirest.get(ADDRESS_QUERY_SITE + "?ip=" + ip + "&json=true");
            String rspStr = request.asString().getBody();

            if (StringUtils.isEmpty(rspStr)) {
                log.error("获取地理位置异常 {}", ip);
                return null;
            }

            String province = JacksonUtil.getAsString(rspStr, "pro");
            String city = JacksonUtil.getAsString(rspStr, "city");
            return new IpRegion(province, city);
        } catch (Exception e) {
            log.error("获取地理位置异常 {}", ip, e);
        }
        return null;
    }

}
