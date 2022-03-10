package com.lesofn.appengine.common.config;

import com.lesofn.appengine.common.utils.collection.GlobalCollectionUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author sofn
 */
@Slf4j
public class DefaultProfileLoader extends ProfileLoader {

    public static final String APP_ENV_VAR = "profile";

    public static DefaultProfileLoader loader = new DefaultProfileLoader();

    private Env envVar;

    public static DefaultProfileLoader getInstance() {
        return loader;
    }

    @Override
    public Env getEnv() {
        if (envVar != null) {
            return envVar;
        }
        synchronized (this) {
            if (envVar == null) {
                //先通过环境变量判断
                String env = System.getenv(APP_ENV_VAR);
                if (env == null) {
                    //先从Yaml文件中读取
                    Optional<String> envOptional = readFromYaml();
                    if (!envOptional.isPresent()) {
                        envOptional = readFromProperties();
                    }
                    //再从再从Properties文件中读取
                    if (envOptional.isPresent()) {
                        env = envOptional.get();
                    }
                }
                if (env == null) {
                    envVar = DEFAULT_DEV;
                } else {
                    envVar = Env.valueOf(env);
                }
                System.setProperty(APP_ENV_VAR, envVar.name());

                log.info("AppEnv {}", envVar);
            }
        }
        return envVar;
    }

    private Optional<String> readFromProperties() {
        String env = null;
        try {
            env = PropertiesLoaderUtils.loadAllProperties("application.properties").getProperty("profile");
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
            String fileContent = IOUtils.toString(url, "UTF-8");
            Map map = (Map) ya.load(fileContent);
            if (map.get("spring") != null && ((Map) map.get("spring")).get("profiles") != null) {
                String active = (String) ((Map) ((Map) map.get("spring")).get("profiles")).get("active");

                if (StringUtils.isEmpty(active)) {
                    return Optional.empty();
                }

                List<String> profiles = GlobalCollectionUtils.strListSplitter(active);
                for (String profile : profiles) {
                    if (Env.valueOf(profile) != null) {
                        return Optional.of(profile);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    public static boolean isDev() {
        return loader.getEnv() == Env.dev;
    }

    public static boolean isTest() {
        return loader.getEnv() == Env.test;
    }

    public static boolean isProd() {
        return loader.getEnv() == Env.prod;
    }

    public static boolean accept(Env env) {
        return loader.getEnv() == env;
    }
}
