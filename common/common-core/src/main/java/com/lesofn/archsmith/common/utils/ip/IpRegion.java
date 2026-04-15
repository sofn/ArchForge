package com.lesofn.archsmith.common.utils.ip;

import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

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
        return String.format(
                        "%s %s",
                        Objects.requireNonNullElse(province, UNKNOWN),
                        Objects.requireNonNullElse(city, UNKNOWN))
                .trim();
    }
}
