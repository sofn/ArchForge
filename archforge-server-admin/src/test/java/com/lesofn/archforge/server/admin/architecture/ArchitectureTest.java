package com.lesofn.archforge.server.admin.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.web.bind.annotation.RestController;

class ArchitectureTest {

    private static JavaClasses classes;

    @BeforeAll
    static void importClasses() {
        classes = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("com.lesofn.archforge.meta.table", "com.lesofn.archforge.server.admin");
    }

    @Test
    void metaTableShouldNotDependOnInfrastructure() {
        assertDoesNotThrow(() -> noClasses()
                .that()
                .resideInAPackage("..archforge.meta.table..")
                .should()
                .dependOnClassesThat()
                .resideInAPackage("..archforge.infrastructure..")
                .check(classes));
    }

    @Test
    void apiShouldNotDependOnInternal() {
        assertDoesNotThrow(() -> noClasses()
                .that()
                .resideInAPackage("..api..")
                .should()
                .dependOnClassesThat()
                .resideInAPackage("..internal..")
                .check(classes));
    }

    @Test
    void controllersShouldNotDirectlyAccessRepositories() {
        noClasses()
                .that()
                .areAnnotatedWith(RestController.class)
                .should()
                .dependOnClassesThat()
                .areAssignableTo(JpaRepository.class)
                .check(classes);
    }
}
