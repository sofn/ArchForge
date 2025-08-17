package com.lesofn.matrixboot.common.utils.ip;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.apache.commons.lang3.ObjectUtils;

/**
 * @author sofn
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class IpRegion {
    private static final String UNKNOWN = "未知";
    private String country;
    private String region;
    private String province;
    private String city;
    private String isp;

    public IpRegion(String province, String city) {
        this.province = province;
        this.city = city;
    }

    public String briefLocation() {
        return String.format("%s %s",
                ObjectUtils.defaultIfNull(province, UNKNOWN),
                ObjectUtils.defaultIfNull(city, UNKNOWN)).trim();
    }

}
