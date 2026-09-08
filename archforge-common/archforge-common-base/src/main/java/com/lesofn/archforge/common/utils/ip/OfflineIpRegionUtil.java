package com.lesofn.archforge.common.utils.ip;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.lionsoul.ip2region.xdb.Searcher;
import org.lionsoul.ip2region.xdb.Version;

/**
 * Offline IP region lookup backed by ip2region's xdb data files.
 *
 * <p>
 * Routing: valid IPv6 addresses resolve against the v6 xdb, everything else falls back to the
 * v4 xdb. Searchers run in file-only mode — the 11/37 MB data stays on disk and is served through
 * the OS page cache instead of being fully materialised on the heap (the previous
 * newWithBuffer() approach permanently pinned ~11 MB of heap; lookups are microsecond-level, so
 * the file mode trade-off is negligible).
 *
 * <p>
 * The xdb files are downloaded and cached by {@link XdbManager} on first use. Failed
 * initialisation is retried with a backoff so a transient network outage cannot turn into a
 * permanent loss of offline lookups.
 *
 * <p>
 * ip2region's Searcher is not internally synchronised (3.3.7), so queries lock the shared
 * instance — the critical section is a microsecond file read.
 *
 * @author sofn
 */
@Slf4j
public class OfflineIpRegionUtil {

    private static final long RETRY_BACKOFF_MS = TimeUnit.SECONDS.toMillis(60);

    private static volatile Searcher v4Searcher;
    private static volatile Searcher v6Searcher;
    private static volatile long v4NextRetryAt;
    private static volatile long v6NextRetryAt;

    private OfflineIpRegionUtil() {
    }

    public static IpRegion getIpRegion(String ip) {
        if (StringUtils.isBlank(ip) || !XdbManager.isOfflineEnabled()) {
            return null;
        }
        try {
            // v6 for genuine IPv6 addresses; v4 as the fallback for everything else
            boolean ipv6 = IpUtil.isValidIpv6(ip);
            Searcher searcher = ipv6 ? v6Searcher() : v4Searcher();
            if (searcher == null) {
                return null;
            }

            String rawRegion;
            // ip2region Searcher is not thread-safe in file mode: share one instance under a lock
            synchronized (searcher) {
                rawRegion = searcher.search(ip);
            }

            if (StringUtils.isEmpty(rawRegion)) {
                return null;
            }
            String[] split = rawRegion.split("\\|");
            return new IpRegion(split[0], split[1], split[2], split[3], split[4]);
        } catch (Exception e) {
            log.error("获取IP地理位置失败", e);
        }
        return null;
    }

    private static Searcher v4Searcher() {
        Searcher s = v4Searcher;
        if (s == null) {
            s = initSearcher(Version.IPv4, false);
            v4Searcher = s;
        }
        return s;
    }

    private static Searcher v6Searcher() {
        Searcher s = v6Searcher;
        if (s == null) {
            s = initSearcher(Version.IPv6, true);
            v6Searcher = s;
        }
        return s;
    }

    /** Lazy initialisation with a retry backoff; returns null when the xdb is unavailable. */
    private static synchronized Searcher initSearcher(Version version, boolean ipv6) {
        long now = System.currentTimeMillis();
        long nextRetryAt = ipv6 ? v6NextRetryAt : v4NextRetryAt;
        if (now < nextRetryAt) {
            return null;
        }
        try {
            Path file = XdbManager.ensureXdb(ipv6);
            if (file == null || !Files.exists(file)) {
                throw new IllegalStateException("xdb file unavailable for " + version.name);
            }
            return Searcher.newWithFileOnly(version, file.toAbsolutePath().toString());
        } catch (Exception e) {
            log.error("初始化 ip2region {} 离线库失败，{} 秒后重试", version.name, RETRY_BACKOFF_MS / 1000, e);
            if (ipv6) {
                v6NextRetryAt = now + RETRY_BACKOFF_MS;
            } else {
                v4NextRetryAt = now + RETRY_BACKOFF_MS;
            }
            return null;
        }
    }
}
