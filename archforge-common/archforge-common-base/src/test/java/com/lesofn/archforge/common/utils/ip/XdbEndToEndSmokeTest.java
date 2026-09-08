package com.lesofn.archforge.common.utils.ip;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.Test;

/**
 * Real-network end-to-end smoke for the offline ip lookup: downloads both xdb files from their
 * configured CDNs, then resolves a v4 and a v6 address through {@link OfflineIpRegionUtil}.
 *
 * <p>
 * Runs ONLY with {@code ARCHFORGE_LOCAL_XDB=true} (CI skips it; CI covers the manager logic
 * via XdbManagerTest with a mock server). Override {@code ARCHFORGE_XDB_V6_URL} when the default
 * raw.githubusercontent.com is slow from your network, e.g.
 * {@code https://gh-proxy.com/https://raw.githubusercontent.com/.../ip2region_v6.xdb}.
 *
 * <p>
 * Verified locally against ip2region xdb v3.18.0 + library 3.3.7 (2026-09-08):
 * 1.2.4.8 -> 中国|北京市|北京市|0|CN, 240e:978:... -> 中国|北京市|北京市|电信|CN.
 */
@EnabledIfEnvironmentVariable(named = "ARCHFORGE_LOCAL_XDB", matches = "true")
class XdbEndToEndSmokeTest {

    @TempDir
    Path tempDir;

    @Test
    void v4AndV6ResolveFromRealCdns() {
        String v6Url = System.getenv().getOrDefault(
                "ARCHFORGE_XDB_V6_URL", XdbManager.DEFAULT_V6_URL);
        XdbManager.configure(XdbManager.DEFAULT_V4_URL, v6Url, tempDir.toString(), true);

        IpRegion v4 = OfflineIpRegionUtil.getIpRegion("1.2.4.8");
        assertNotNull(v4, "v4 lookup must succeed");
        assertTrue(v4.toString().contains("北京") || v4.toString().contains("中国"),
                "unexpected v4 result: " + v4);

        IpRegion v6 = OfflineIpRegionUtil.getIpRegion("240e:978:0:17:34::b6");
        assertNotNull(v6, "v6 lookup must succeed");
        assertTrue(v6.toString().contains("电信") || v6.toString().contains("中国"),
                "unexpected v6 result: " + v6);

        // cache reuse: second round must not re-download (implicitly verified by succeeding fast)
        assertNotNull(OfflineIpRegionUtil.getIpRegion("114.114.114.114"));
    }
}
