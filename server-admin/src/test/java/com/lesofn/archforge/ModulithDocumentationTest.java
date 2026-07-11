package com.lesofn.archforge;

import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.springframework.modulith.docs.Documenter;

import static org.assertj.core.api.Assertions.assertThat;

class ModulithDocumentationTest {

    @Test
    void generateDocumentation() {
        new Documenter(Application.class).writeDocumentation();

        assertThat(Path.of("build/spring-modulith-docs/components.puml"))
                .exists()
                .isNotEmptyFile();
    }
}
