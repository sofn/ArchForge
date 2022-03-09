package com.lesofn.appengine.common.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

/**
 * @author sofn
 */
@Service
public class SpringProfileLoader extends ProfileLoader {

    @Autowired
    private Environment env;

    private Env envVar;

    @Override
    public Env getEnv() {
        if (envVar == null) {
            String[] profiles = env.getActiveProfiles();
            for (String profile : profiles) {
                if (Env.valueOf(profile) != null) {
                    envVar = Env.valueOf(profile);
                }
            }
            if (envVar == null) {
                envVar = DEFAULT_DEV;
            }
        }
        return envVar;
    }

    public boolean isDev() {
        return this.getEnv() == Env.dev;
    }

    public boolean isTest() {
        return this.getEnv() == Env.test;
    }

    public boolean isProd() {
        return this.getEnv() == Env.prod;
    }

    public boolean accept(Env env) {
        return this.getEnv() == env;
    }
}
