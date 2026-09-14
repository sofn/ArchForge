package com.lesofn.archforge.cli.config;

import picocli.CommandLine.ITypeConverter;

/**
 * Deployment profiles selecting the docker-compose stack file. Dependency
 * containers (postgres/redis) always use {@code docker-compose.infra.yml}
 * regardless of profile.
 */
public enum Profile {
    /** Local dev full stack — docker-compose.yml (postgres + redis + app image). */
    dev,
    /** Single-image product stack — docker-compose.allinone.yml (nginx + 2 JVM + next.js in one container). */
    allinone,
    /** Full stack on the full JRE image variant. */
    fulljre,
    /** Full stack on the jlink image variant. */
    jlink,
    /** Full stack on the GraalVM native-image variant (CLI spelling: "native"). */
    nativeImage,
    /** Staging environment stack. */
    staging,
    /** Production environment stack. */
    prod;

    /** Accepts "native" for {@link #nativeImage}; any other spelling errors out. */
    public static class Converter implements ITypeConverter<Profile> {
        @Override
        public Profile convert(String value) {
            if ("native".equalsIgnoreCase(value)) {
                return nativeImage;
            }
            try {
                return Profile.valueOf(value);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Unknown profile '" + value +
                        "' — expected one of: dev, allinone, fulljre, jlink, native, staging, prod");
            }
        }
    }
}
