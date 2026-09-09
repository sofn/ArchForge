package com.lesofn.archforge.common.utils.ip;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.sun.net.httpserver.HttpServer;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Unit tests for {@link XdbManager} — download, cache-reuse, integrity checks — against a local
 * mock HTTP server (no external network involved). Headers use the exact 256-byte prefixes of
 * the real v3.18.0 xdb files, captured from
 * https://cdn.jsdelivr.net/gh/lionsoul2014/ip2region@v3.18.0/data/.
 */
class XdbManagerTest {

    private static final String V4_HEADER_HEX = "03000100D511916A0A5F330066B5A9000400040000000000000000000000000000000000" +
            "000000000000000000000000000000000000000000000000000000000000000000000000" +
            "000000000000000000000000000000000000000000000000000000000000000000000000" +
            "000000000000000000000000000000000000000000000000000000000000000000000000" +
            "000000000000000000000000000000000000000000000000000000000000000000000000" +
            "000000000000000000000000000000000000000000000000000000000000000000000000" +
            "000000000000000000000000000000000000000000000000000000000000000000000000" + "00000000000000000000";

    private static final String V6_HEADER_HEX = "03000100E1A97E6ADFA98E004FD038020600040000000000000000000000000000000000" +
            "000000000000000000000000000000000000000000000000000000000000000000000000" +
            "000000000000000000000000000000000000000000000000000000000000000000000000" +
            "000000000000000000000000000000000000000000000000000000000000000000000000" +
            "000000000000000000000000000000000000000000000000000000000000000000000000" +
            "000000000000000000000000000000000000000000000000000000000000000000000000" +
            "000000000000000000000000000000000000000000000000000000000000000000000000" +
            "00000000000000000000000000000000000000";

    @TempDir
    Path tempDir;

    private HttpServer server;
    private final AtomicInteger headCount = new AtomicInteger();
    private final AtomicInteger getCount = new AtomicInteger();

    @BeforeEach
    void setUp() throws Exception {
        server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.createContext("/v4.xdb", exchange -> {
            byte[] body = xdbBytes(V4_HEADER_HEX);
            if ("HEAD".equals(exchange.getRequestMethod())) {
                headCount.incrementAndGet();
                exchange.sendResponseHeaders(200, body.length);
            } else {
                getCount.incrementAndGet();
                exchange.sendResponseHeaders(200, body.length);
                try (OutputStream out = exchange.getResponseBody()) {
                    out.write(body);
                }
                return;
            }
            exchange.close();
        });
        server.start();
        XdbManager.configure(
                url("/v4.xdb"),
                url("/v6.xdb"),
                tempDir.toString(),
                true);
    }

    @AfterEach
    void tearDown() {
        server.stop(0);
        XdbManager.configure(null, null, null, true);
    }

    @Test
    void urlTemplatesGetVersionInjected() {
        String version = XdbManager.xdbVersion();
        XdbManager.configure("https://example.com/ip2region@{version}/v4.xdb", null, null, true);
        String resolved = XdbManager.resolveUrl(false);
        assertTrue(resolved.endsWith("/v4.xdb"));
        if (version != null) {
            assertEquals("https://example.com/ip2region@" + version + "/v4.xdb", resolved);
            assertTrue(resolved.contains(version));
        } else {
            // properties not expanded (running outside the gradle resource pipeline)
            assertTrue(resolved.contains("{version}"));
        }
    }

    @Test
    void headerValidationDetectsFamilyAndCorruption() throws Exception {
        Path v4 = tempDir.resolve("real-v4.xdb");
        Files.write(v4, xdbBytes(V4_HEADER_HEX));
        Path v6 = tempDir.resolve("real-v6.xdb");
        Files.write(v6, xdbBytes(V6_HEADER_HEX));
        Path truncated = tempDir.resolve("truncated.xdb");
        Files.write(truncated, new byte[64]);

        assertTrue(XdbManager.isHeaderValid(v4, false));
        assertFalse(XdbManager.isHeaderValid(v4, true), "v4 file must not pass the v6 check");
        assertTrue(XdbManager.isHeaderValid(v6, true));
        assertFalse(XdbManager.isHeaderValid(v6, false));
        assertFalse(XdbManager.isHeaderValid(truncated, false), "truncated file must fail");
    }

    @Test
    void downloadsOnceThenServesFromCache() throws Exception {
        Path file = XdbManager.ensureXdb(false);
        assertNotNull(file);
        assertTrue(Files.exists(file));
        assertEquals(1, getCount.get(), "first call downloads");

        Path again = XdbManager.ensureXdb(false);
        assertEquals(file, again);
        assertEquals(1, getCount.get(), "cached call must not re-download");
        assertTrue(headCount.get() >= 1, "cached call verifies the remote size (best effort)");
    }

    @Test
    void sizeMismatchTriggersRefresh() throws Exception {
        Path file = XdbManager.ensureXdb(false);
        assertEquals(1, getCount.get());
        // corrupt the cached size -> HEAD mismatch -> re-download
        Files.write(file, xdbBytes(V6_HEADER_HEX));

        Path refreshed = XdbManager.ensureXdb(false);
        assertNotNull(refreshed);
        assertEquals(2, getCount.get(), "stale cache must be re-downloaded");
        assertEquals(xdbBytes(V4_HEADER_HEX).length, Files.size(refreshed));
    }

    @Test
    void disabledOfflineIsRespected() {
        XdbManager.configure(null, null, null, false);
        assertFalse(XdbManager.isOfflineEnabled());
        XdbManager.configure(null, null, null, true);
    }

    @Test
    void downloadFailureReturnsNullAndKeepsTmpClean() throws Exception {
        XdbManager.configure(url("/missing.xdb"), null, tempDir.toString(), true);
        assertNull(XdbManager.ensureXdb(false));
        try (var listing = Files.list(tempDir)) {
            assertEquals(0, listing.filter(p -> {
                java.nio.file.Path fn = p.getFileName();
                return fn != null && fn.toString().endsWith(".tmp");
            }).count(), "no tmp leftovers on failed download");
        }
    }

    private String url(String path) {
        return "http://localhost:" + server.getAddress().getPort() + path;
    }

    private static byte[] xdbBytes(String headerHex) {
        byte[] header = hex(headerHex);
        // the header itself is a valid-enough payload for the manager (Searcher is not opened here)
        return header;
    }

    private static byte[] hex(String hex) {
        byte[] out = new byte[hex.length() / 2];
        for (int i = 0; i < out.length; i++) {
            out[i] = (byte) Integer.parseInt(hex.substring(i * 2, i * 2 + 2), 16);
        }
        return out;
    }

    @SuppressWarnings("unused")
    private static final String UNUSED_UTF8 = StandardCharsets.UTF_8.name();
}
