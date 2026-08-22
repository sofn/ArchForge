package com.lesofn.archforge.server.admin.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.web.bind.annotation.RestController;

/**
 * Architecture rules for the whole codebase (G7).
 *
 * <p>
 * Rules fail on empty should-clauses by default ({@code archRule.failOnEmptyShould} defaults to
 * {@code true}), so a rule that accidentally matches no classes is a test failure, not a silent
 * pass. See archunit.properties at the root of this module's test resources.
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
    void metaTableShouldNotDependOnInfrastructure() {
        noClasses()
                .that()
                .resideInAPackage("..archforge.meta.table..")
                .should()
                .dependOnClassesThat()
                .resideInAPackage("..archforge.infrastructure..")
                .check(classes);
    }

    @Test
    void blogShouldNotDependOnInfrastructure() {
        noClasses()
                .that()
                .resideInAPackage("..archforge.blog..")
                .should()
                .dependOnClassesThat()
                .resideInAPackage("..archforge.infrastructure..")
                .check(classes);
    }

    @Test
    void apiShouldNotDependOnInternal() {
        noClasses()
                .that()
                .resideInAPackage("..api..")
                .should()
                .dependOnClassesThat()
                .resideInAPackage("..internal..")
                .check(classes);
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

    @Test
    void domainModulesShouldNotDependOnServerApplications() {
        noClasses()
                .that()
                .resideInAnyPackage("..archforge.domain..", "..archforge.user..", "..archforge.blog..",
                        "..archforge.meta.table..")
                .should()
                .dependOnClassesThat()
                .resideInAPackage("..archforge.server..")
                .check(classes);
    }

    @Test
    void commonModulesShouldNotDependOnBusinessLayers() {
        noClasses()
                .that()
                .resideInAPackage("..archforge.common..")
                .should()
                .dependOnClassesThat()
                .resideInAnyPackage(
                        "..archforge.domain..",
                        "..archforge.server..",
                        "..archforge.infrastructure..",
                        "..archforge.starter..")
                .check(classes);
    }

    @Test
    void startersShouldNotDependOnBusinessModules() {
        noClasses()
                .that()
                .resideInAPackage("..archforge.starter..")
                .should()
                .dependOnClassesThat()
                .resideInAnyPackage("..archforge.domain..", "..archforge.server..")
                .check(classes);
    }
}
