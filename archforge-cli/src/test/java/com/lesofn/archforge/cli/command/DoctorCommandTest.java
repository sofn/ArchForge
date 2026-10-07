package com.lesofn.archforge.cli.command;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.CharsetEncoder;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

/**
 * `doctor` is the first command a new teammate runs. On a GBK / non-UTF-8 console the old check/cross marks
 * (U+2713 / U+2717) and em dashes all printed as "?", so passing and failing lines were indistinguishable.
 */
class DoctorCommandTest {

    @Test
    void statusLinesSurviveAnyConsoleEncoding() {
        CharsetEncoder ascii = StandardCharsets.US_ASCII.newEncoder();
        for (String line : new String[] {
                DoctorCommand.okLine("java 25"), DoctorCommand.failLine("docker daemon not found on PATH"),
                DoctorCommand.summaryLine(3)
        }) {
            assertTrue(ascii.canEncode(line), "non-ASCII output is replaced by '?' on GBK consoles: " + line);
        }
    }

    @Test
    void passAndFailAreDistinguishable() {
        assertNotEquals(DoctorCommand.okLine("x").substring(0, 8), DoctorCommand.failLine("x").substring(0, 8));
        assertTrue(DoctorCommand.failLine("x").contains("FAIL"));
        assertTrue(DoctorCommand.okLine("x").contains("OK"));
    }
}
