package com.lesofn.archforge.server.admin.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.library.freeze.FreezingArchRule;
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

    /**
     * ADR-0001: domain modules contain exactly {@code api} + {@code internal} top-level
     * packages. The collapsed {@code domain/}/{@code infrastructure/} trees must not
     * regrow — a class placed there fails the build.
     */
    @Test
    void domainModulesOnlyContainApiAndInternal() {
        noClasses()
                .should()
                .resideInAnyPackage(
                        "com.lesofn.archforge.user.domain..",
                        "com.lesofn.archforge.user.infrastructure..",
                        "com.lesofn.archforge.blog.domain..",
                        "com.lesofn.archforge.blog.infrastructure..",
                        "com.lesofn.archforge.meta.table.domain..",
                        "com.lesofn.archforge.meta.table.infrastructure..")
                .check(classes);
    }

    /** Naming: MapStruct converters are {@code *Convertor}, never {@code *Mapper}. */
    @Test
    void noMapperNamedClasses() {
        noClasses().should().haveSimpleNameEndingWith("Mapper").check(classes);
    }

    /**
     * Naming: request/response types use {@code *Request}/{@code *Response}. The 12
     * legacy {@code *DTO} classes are frozen — this rule fails on NEW ones anywhere,
     * not just inside {@code dto} packages. The frozen violations live in
     * {@code archunit_store/} (committed); refreezing is a deliberate act: run with
     * {@code -Darchunit.freeze.refreeze=true} and commit the updated store.
     */
    @Test
    void noNewDtoSuffixedTypes() {
        FreezingArchRule.freeze(
                noClasses()
                        .should()
                        .haveSimpleNameEndingWith("DTO")
                        .orShould()
                        .haveSimpleNameEndingWith("ItemDTO"))
                .check(classes);
    }
}
