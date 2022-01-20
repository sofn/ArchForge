package com.appengine.common.context;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author sofn
 * @version 1.0 Created at: 2022-01-20 16:46
 */
class ClientVersionTest {

    @Test
    public void testClientVersion() {
        String header = "2.4.1.0-1.0.0.0-udid-iPhone-test";
        ClientVersion version = ClientVersion.valueOf(header);
        assertEquals(2, version.sdkVersion.major);
        assertEquals(4, version.sdkVersion.minor);
        assertEquals(1, version.sdkVersion.revision);

        assertEquals(1, version.clientVersion.major);
        assertEquals(0, version.clientVersion.minor);
        assertEquals(0, version.clientVersion.revision);

        assertEquals("udid", version.udid);
        assertEquals("iPhone", version.device);
        assertEquals("test", version.channel);

        ClientVersion version2 = ClientVersion.valueOf(version.toString());
        assertEquals(version, version2);
        System.out.println(version);
        assertEquals(header, version2.toString());
    }


    @Test
    public void testClientVersionChannel() {
        String header = "0-0-unknow-unknow-test";
        ClientVersion version = ClientVersion.valueOf(header);
        System.out.println(version.toString());
    }

    @Test
    public void testClientVersionError() {
        ClientVersion version = ClientVersion.valueOf("test");
        assertNotNull(version);
        assertEquals(ClientVersion.NULL, version);

        version = ClientVersion.valueOf("test-");
        assertNotNull(version);
        assertEquals(ClientVersion.NULL, version);

        version = ClientVersion.valueOf("test-bbb");
        assertNotNull(version);
        assertEquals(ClientVersion.NULL, version);

        version = ClientVersion.valueOf(null);
        assertNotNull(version);
        assertEquals(ClientVersion.NULL, version);

        version = ClientVersion.valueOf("");
        assertNotNull(version);
        assertEquals(ClientVersion.NULL, version);
    }

    @Test
    public void testCompare() {
        assertEquals(1, ClientVersion.Version.valueOf("1.1.75").compareTo(null));
        assertEquals(1, ClientVersion.Version.valueOf("1.1.75").compareTo(ClientVersion.Version.valueOf("0.1.75")));
        assertEquals(1, ClientVersion.Version.valueOf("1.1.75").compareTo(ClientVersion.Version.valueOf("1.0.75")));
        assertEquals(1, ClientVersion.Version.valueOf("1.1.75").compareTo(ClientVersion.Version.valueOf("1.1.74")));

        assertEquals(0, ClientVersion.Version.valueOf("1.1.75").compareTo(ClientVersion.Version.valueOf("1.1.75")));

        assertEquals(-1, ClientVersion.Version.valueOf("1.1.75").compareTo(ClientVersion.Version.valueOf("2.1.75")));
        assertEquals(-1, ClientVersion.Version.valueOf("1.1.75").compareTo(ClientVersion.Version.valueOf("1.2.75")));
        assertEquals(-1, ClientVersion.Version.valueOf("1.1.75").compareTo(ClientVersion.Version.valueOf("1.1.76")));
    }

}