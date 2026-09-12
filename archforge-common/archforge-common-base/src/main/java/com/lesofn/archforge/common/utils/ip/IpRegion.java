package com.lesofn.archforge.common.utils.ip;

import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.jspecify.annotations.Nullable;

/**
 * @author sofn
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class IpRegion {
    private static final String UNKNOWN = "未知";
    private @Nullable String country;
    private @Nullable String region;
    private @Nullable String province;
    private @Nullable String city;
    private @Nullable String isp;

    public IpRegion(@Nullable String province, @Nullable String city) {
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
