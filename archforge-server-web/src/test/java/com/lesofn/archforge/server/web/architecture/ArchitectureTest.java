package com.lesofn.archforge.server.web.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Architecture rules for server-web (G7). Rules fail on empty should-clauses by default, so a
 * package filter typo is a test failure rather than a silent pass.
 */
@Tag("contract")
class ArchitectureTest {

    private static JavaClasses classes;

    @BeforeAll
    static void importClasses() {
        classes = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("com.lesofn.archforge");
    }

    @Test
    void serverWebShouldNotDependOnServerAdmin() {
        noClasses()
                .that()
                .resideInAPackage("..archforge.server.web..")
                .should()
                .dependOnClassesThat()
                .resideInAPackage("..archforge.server.admin..")
                .check(classes);
    }

    @Test
    void controllersShouldNotDirectlyAccessRepositories() {
        noClasses()
                .that()
                .areAnnotatedWith(org.springframework.web.bind.annotation.RestController.class)
                .should()
                .dependOnClassesThat()
                .areAssignableTo(org.springframework.data.jpa.repository.JpaRepository.class)
                .check(classes);
    }
}
