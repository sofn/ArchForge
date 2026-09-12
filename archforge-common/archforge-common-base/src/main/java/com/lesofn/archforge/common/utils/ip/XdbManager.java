package com.lesofn.archforge.common.utils.ip;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;
import lombok.extern.slf4j.Slf4j;
import org.lionsoul.ip2region.xdb.Header;
import org.jspecify.annotations.Nullable;

/**
 * Manages the local cache of ip2region xdb data files.
 *
 * <p>
 * Files are downloaded on first use into {@code ./data} (relative to the working directory,
 * git-ignored) instead of shipping 11+37 MB of binaries inside the jar / git repository. A cached
 * file is reused without re-downloading, but its integrity is verified: the xdb header must parse
 * and carry the expected ip version, and (best-effort, network permitting) the remote
 * Content-Length must match the local size — otherwise the file is fetched again.
 *
 * <p>
 * URL templates are configurable in application.yaml ({@code arch-forge.ip-region.*}); the
 * {@code {version}} placeholder is filled from the ip2region-xdb.properties injected at build
 * time, keeping the data version tied to the gradle build rather than hand-edited in yml.
 *
 * <p>
 * Note: cdn.jsdelivr.net caps files at 20 MB, so the v6 xdb (37 MB) must use
 * raw.githubusercontent.com (or a mirror) — see the defaults in application.yaml.
 *
 * @author sofn
 */
@Slf4j
public final class XdbManager {

    /** Default CDN templates; overridable via {@link #configure(String, String, String)}. */
    static final String DEFAULT_V4_URL = "https://cdn.jsdelivr.net/gh/lionsoul2014/ip2region@{version}/data/ip2region_v4.xdb";

    /**
     * jsdelivr rejects files over 20 MB (the v6 xdb is ~37 MB), so the official raw source is the
     * default. CN deployments may point this at a mirror via yml.
     */
    static final String DEFAULT_V6_URL = "https://raw.githubusercontent.com/lionsoul2014/ip2region/{version}/data/ip2region_v6.xdb";

    static final String V4_FILE = "ip2region_v4.xdb";
    static final String V6_FILE = "ip2region_v6.xdb";
    static final int HEADER_LENGTH = 256;

    private static volatile String v4UrlTemplate = DEFAULT_V4_URL;
    private static volatile String v6UrlTemplate = DEFAULT_V6_URL;
    private static volatile Path cacheDir = Path.of(".", "data");
    private static volatile boolean offlineEnabled = true;

    private static final HttpClient HTTP = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).followRedirects(
            HttpClient.Redirect.NORMAL).build();

    private XdbManager() {
    }

    /** Whether offline (xdb) lookups are enabled at all — integration tests set this to false. */
    public static boolean isOfflineEnabled() {
        return offlineEnabled;
    }

    /** Spring wiring: overrides url templates / cache dir / switch from application.yaml. */
    public static void configure(@Nullable String v4Template, @Nullable String v6Template, @Nullable String directory,
            boolean enabled) {
        offlineEnabled = enabled;
        if (v4Template != null && !v4Template.isBlank()) {
            v4UrlTemplate = v4Template.trim();
        }
        if (v6Template != null && !v6Template.isBlank()) {
            v6UrlTemplate = v6Template.trim();
        }
        if (directory != null && !directory.isBlank()) {
            cacheDir = Path.of(directory.trim());
        }
    }

    /** The data-file version injected from the gradle build (ip2regionXdbVersion). */
    static @Nullable String xdbVersion() {
        try (InputStream in = XdbManager.class.getResourceAsStream("/ip2region-xdb.properties")) {
            Properties props = new Properties();
            if (in == null) {
                return null;
            }
            props.load(in);
            String v = props.getProperty("xdb.version");
            return (v == null || v.isBlank() || v.startsWith("${")) ? null : v.trim();
        } catch (IOException e) {
            return null;
        }
    }

    /** Resolves the actual download url for the given address family. */
    static String resolveUrl(boolean ipv6) {
        String template = ipv6 ? v6UrlTemplate : v4UrlTemplate;
        String version = xdbVersion();
        if (version != null && template.contains("{version}")) {
            return template.replace("{version}", version);
        }
        return template;
    }

    /**
     * Returns a ready-to-use xdb file for the requested family, downloading or re-validating as
     * needed. Returns null when the file cannot be obtained (offline, bad url, ...).
     */
    public static @Nullable Path ensureXdb(boolean ipv6) {
        String url = resolveUrl(ipv6);
        Path target = cacheDir.resolve(ipv6 ? V6_FILE : V4_FILE);
        try {
            if (Files.exists(target)) {
                if (isHeaderValid(target, ipv6) && isSizeFresh(target, url)) {
                    return target;
                }
                log.info("Cached xdb invalid/stale, re-downloading: {}", target);
            } else {
                log.info("No cached xdb, downloading {} -> {}", url, target);
            }
            return download(url, target, ipv6);
        } catch (Exception e) {
            log.error("Failed to ensure xdb file {} from {}", target, url, e);
            // last resort: an existing but stale file still beats nothing
            if (Files.exists(target) && isHeaderValid(target, ipv6)) {
                log.warn("Falling back to the existing local xdb despite the failed refresh");
                return target;
            }
            return null;
        }
    }

    /** Verifies the 256-byte header parses and carries the expected ip version. */
    static boolean isHeaderValid(Path file, boolean expectIpv6) {
        try (InputStream in = Files.newInputStream(file)) {
            byte[] header = in.readNBytes(HEADER_LENGTH);
            if (header.length < HEADER_LENGTH) {
                return false;
            }
            Header parsed = new Header(Arrays.copyOf(header, HEADER_LENGTH));
            return parsed.ipVersion == (expectIpv6 ? 6 : 4);
        } catch (Exception e) {
            return false;
        }
    }

    /** Best-effort remote size check; true when unreachable (offline tolerance) or matching. */
    private static boolean isSizeFresh(Path file, String url) {
        try {
            HttpRequest head = HttpRequest.newBuilder(URI.create(url)).method("HEAD", HttpRequest.BodyPublishers.noBody())
                    .build();
            HttpResponse<Void> resp = HTTP.send(head, HttpResponse.BodyHandlers.discarding());
            String len = resp.headers().firstValue("Content-Length").orElse(null);
            if (len == null) {
                return true;
            }
            long remote = Long.parseLong(len);
            long local = Files.size(file);
            if (remote != local) {
                log.info("xdb size mismatch (remote={}, local={}), refresh needed", remote, local);
                return false;
            }
            return true;
        } catch (Exception e) {
            log.warn("Could not verify remote xdb size for {}, trusting the local cache: {}", url, e.getMessage());
            return true;
        }
    }

    private static Path download(String url, Path target, boolean ipv6) throws IOException, InterruptedException {
        Path parent = target.toAbsolutePath().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Path tmp = target.resolveSibling(target.getFileName() + ".tmp");
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(300))
                .GET()
                .build();
        HttpResponse<InputStream> resp = HTTP.send(request, HttpResponse.BodyHandlers.ofInputStream());
        if (resp.statusCode() / 100 != 2) {
            throw new IOException("HTTP " + resp.statusCode() + " fetching " + url);
        }
        try (InputStream body = resp.body()) {
            Files.copy(body, tmp, StandardCopyOption.REPLACE_EXISTING);
        }
        if (!isHeaderValid(tmp, ipv6)) {
            Files.deleteIfExists(tmp);
            throw new IOException("Downloaded file failed the xdb header check: " + url);
        }
        Files.move(tmp, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        log.info("xdb cached: {} ({} bytes)", target, Files.size(target));
        return target;
    }
}
