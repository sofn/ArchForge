package com.lesofn.archforge.server.admin.config;

import com.lesofn.archforge.infrastructure.config.ArchForgeProperties;
import com.lesofn.archforge.infrastructure.config.ProductionSecrets;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Profile("prod")
@Order(0)
@RequiredArgsConstructor
public class ProductionSecretsInitializer implements ApplicationRunner {

    private final ArchForgeProperties properties;

    @Override
    public void run(ApplicationArguments args) {
        ProductionSecrets.requireRsaPrivateKey(properties.getRsaPrivateKey());
    }
}
