package com.lesofn.archforge;

import org.springframework.modulith.Modulithic;

/**
 * Dedicated Modulith root for architecture tests.
 *
 * <p>
 * Keeps the module system root at {@code com.lesofn.archforge} while the Spring Boot
 * application entry point lives in {@code com.lesofn.archforge.server.admin} to avoid
 * accidentally scanning sibling modules such as {@code server-web}.
 */
@Modulithic(systemName = "ArchForge", sharedModules = "common")
public class ModulithRoot {
}
