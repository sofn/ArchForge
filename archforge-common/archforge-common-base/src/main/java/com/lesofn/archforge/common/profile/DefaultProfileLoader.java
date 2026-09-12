package com.lesofn.archforge.common.profile;

import com.lesofn.archforge.common.utils.collection.CollectionUtils;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.yaml.snakeyaml.Yaml;

/**
 * 默认 profile 加载器。
 *
 * @author sofn
 */
@Slf4j
public class DefaultProfileLoader {
    /** 默认线上环境，防止线上bug */
    static final Env DEFAULT_DEV = Env.prod;

    public static final String APP_ENV_VAR = "profile";

    private static final DefaultProfileLoader LOADER = new DefaultProfileLoader();

    private volatile @Nullable Env envVar;

    private final Object envLock = new Object();

    private DefaultProfileLoader() {
    }

    public static DefaultProfileLoader getInstance() { return LOADER; }

    public Env getEnv() {
        Env snapshot = envVar;
        if (snapshot != null) {
            return snapshot;
        }
        synchronized (envLock) {
            return loadEnvLocked();
        }
    }

    private Env loadEnvLocked() {
        if (envVar != null) {
            return envVar;
        }
        // 先通过环境变量判断，再从 Yaml/Properties 文件中读取
        String env = System.getenv(APP_ENV_VAR);
        if (env == null) {
            Optional<String> envOptional = readFromYaml();
            if (!envOptional.isPresent()) {
                envOptional = readFromProperties();
            }
            if (envOptional.isPresent()) {
                env = envOptional.get();
            }
        }
        envVar = resolveEnv(env);
        System.setProperty(APP_ENV_VAR, envVar.name());
        log.info("AppEnv {}", envVar);
        return envVar;
    }

    /** Maps a raw profile name to {@link Env}. Unknown non-null names fail fast. */
    static Env resolveEnv(@Nullable String env) {
        Env parsed = Env.fromName(env);
        if (env != null && parsed == null) {
            throw new IllegalStateException("Unknown profile: " + env);
        }
        return parsed != null ? parsed : DEFAULT_DEV;
    }

    private Optional<String> readFromProperties() {
        String env = null;
        try {
            env = PropertiesLoaderUtils.loadAllProperties("application.properties")
                    .getProperty("profile");
            env = StringUtils.strip(env);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        return Optional.ofNullable(env);
    }

    private Optional<String> readFromYaml() {
        try {
            new Yaml().load("application.yaml");
            Yaml ya = new Yaml();
            URL url = DefaultProfileLoader.class.getClassLoader().getResource("application.yaml");
            if (url == null) {
                return Optional.empty();
            }
            String fileContent = IOUtils.toString(url, StandardCharsets.UTF_8);
            Map<?, ?> map = ya.load(fileContent);
            if (map.get("spring") != null && ((Map<?, ?>) map.get("spring")).get("profiles") != null) {
                String active = (String) ((Map<?, ?>) ((Map<?, ?>) map.get("spring")).get("profiles"))
                        .get("active");

                if (StringUtils.isEmpty(active)) {
                    return Optional.empty();
                }

                List<String> profiles = CollectionUtils.strListSplitter(active);
                for (String profile : profiles) {
                    if (Env.fromName(profile) != null) {
                        return Optional.of(profile);
                    }
                }
            }
        } catch (IOException e) {
            log.error("readFromYaml error", e);
        }
        return Optional.empty();
    }

    public static boolean isDev() { return LOADER.getEnv() == Env.dev; }

    public static boolean isTest() { return LOADER.getEnv() == Env.test; }

    public static boolean isProd() { return LOADER.getEnv() == Env.prod; }

    public static boolean accept(Env env) {
        return LOADER.getEnv() == env;
    }
}
