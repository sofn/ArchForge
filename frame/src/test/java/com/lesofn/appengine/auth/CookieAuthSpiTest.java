package com.lesofn.appengine.auth;

import com.lesofn.appengine.auth.spi.CookieAuthSpi;
import org.junit.jupiter.api.Test;

/**
 * Authors: sofn
 * Version: 1.0  Created at 2015-10-04 22:45.
 */
public class CookieAuthSpiTest {
    @Test
    public void testCreate() {
        String mauth = CookieAuthSpi.generateCookie(1000);
        System.out.println(mauth);
    }
}
