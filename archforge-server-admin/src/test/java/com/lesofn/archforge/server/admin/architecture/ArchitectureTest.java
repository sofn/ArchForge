package com.lesofn.archforge.server.admin.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.library.freeze.FreezingArchRule;
import com.tngtech.archunit.core.importer.ImportOption;
import java.util.Set;
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

    /**
     * ARCH-014: meta-designer types in packages shared with meta-runtime
     * ({@code api.service} / {@code internal.service}) — enumerated because package
     * patterns alone cannot separate the two jars.
     */
    private static final Set<String> META_DESIGNER_SHARED_TYPES = Set.of(
            "com.lesofn.archforge.meta.table.api.service.MetaTableAdminService",
            "com.lesofn.archforge.meta.table.api.service.MetaTableImportService",
            "com.lesofn.archforge.meta.table.api.service.MetaTableMigrationService",
            "com.lesofn.archforge.meta.table.api.service.MetaTableMigrationExporter",
            "com.lesofn.archforge.meta.table.internal.service.MetaTableAdminServiceImpl",
            "com.lesofn.archforge.meta.table.internal.service.MetaTableImportServiceImpl");

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
                .because("ARCH-001: domain modules (meta.table) must not depend on infrastructure")
                .check(classes);
    }

    @Test
    void cmsShouldNotDependOnInfrastructure() {
        noClasses()
                .that()
                .resideInAPackage("..archforge.cms..")
                .should()
                .dependOnClassesThat()
                .resideInAPackage("..archforge.infrastructure..")
                .because("ARCH-002: domain modules (cms) must not depend on infrastructure")
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
                .because("ARCH-003: api packages expose contracts only — never depend on internal")
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
                .because("ARCH-004: controllers must go through service, never touch repositories")
                .check(classes);
    }

    @Test
    void domainModulesShouldNotDependOnServerApplications() {
        noClasses()
                .that()
                .resideInAnyPackage("..archforge.domain..", "..archforge.user..", "..archforge.cms..",
                        "..archforge.meta.table..")
                .should()
                .dependOnClassesThat()
                .resideInAPackage("..archforge.server..")
                .because("ARCH-005: domain modules must not depend on server applications")
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
                .because("ARCH-006: common modules must not depend on business layers")
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
                .because("ARCH-007: starters must not depend on business modules")
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
                        "com.lesofn.archforge.cms.domain..",
                        "com.lesofn.archforge.cms.infrastructure..",
                        "com.lesofn.archforge.meta.table.domain..",
                        "com.lesofn.archforge.meta.table.infrastructure..")
                .because("ARCH-008: domain modules contain exactly api+internal (ADR-0001)")
                .check(classes);
    }

    /** Naming: MapStruct converters are {@code *Convertor}, never {@code *Mapper}. */
    @Test
    void noMapperNamedClasses() {
        noClasses().should().haveSimpleNameEndingWith("Mapper").because(
                "ARCH-009: MapStruct converters are *Convertor, never *Mapper").check(classes);
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
                        .haveSimpleNameEndingWith("ItemDTO")
                        .because("ARCH-010: request/response types use *Request/*Response (frozen legacy *DTO set)"))
                .check(classes);
    }

    /**
     * ARCH-011: L3 builtin modules (admin-user, meta-table) meet each other via
     * {@code api} named interfaces only — never reach into the OTHER module's
     * {@code internal}. Written directionally: a blanket {@code ..internal..} target
     * would also flag a module's own internal-to-internal calls, which are legal.
     */
    @Test
    void builtinModulesShouldNotReachIntoEachOtherInternals() {
        FreezingArchRule.freeze(
                noClasses()
                        .that()
                        .resideInAPackage("..archforge.user..")
                        .should()
                        .dependOnClassesThat()
                        .resideInAPackage("..archforge.meta.table.internal..")
                        .because("ARCH-011a: L3 builtin modules meet via api, never the other module's internal"))
                .check(classes);
        FreezingArchRule.freeze(
                noClasses()
                        .that()
                        .resideInAPackage("..archforge.meta.table..")
                        .should()
                        .dependOnClassesThat()
                        .resideInAPackage("..archforge.user.internal..")
                        .because("ARCH-011b: L3 builtin modules meet via api, never the other module's internal"))
                .check(classes);
    }

    /**
     * ARCH-012: the kernel (common + infrastructure) must not reverse-depend on
     * apps (server), L3 builtin (user, meta.table), or L4 modules (cms, task).
     */
    @Test
    void kernelShouldNotDependOnAppsBuiltinOrModules() {
        FreezingArchRule.freeze(
                noClasses()
                        .that()
                        .resideInAnyPackage("..archforge.common..", "..archforge.infrastructure..")
                        .should()
                        .dependOnClassesThat()
                        .resideInAnyPackage(
                                "..archforge.user..",
                                "..archforge.meta.table..",
                                "..archforge.server..",
                                "..archforge.cms..",
                                "..archforge.task..")
                        .because("ARCH-012: Kernel must not depend on Apps / L3 builtin / L4 modules"))
                .check(classes);
    }

    /**
     * ARCH-013: L3 builtin modules must not depend on L4 business modules — the
     * dependency direction is L4 → L3 → Kernel, never the reverse.
     */
    @Test
    void builtinModulesShouldNotDependOnL4Modules() {
        FreezingArchRule.freeze(
                noClasses()
                        .that()
                        .resideInAnyPackage("..archforge.user..", "..archforge.meta.table..")
                        .should()
                        .dependOnClassesThat()
                        .resideInAnyPackage("..archforge.cms..", "..archforge.task..")
                        .because("ARCH-013: L3 builtin must not depend on L4 business modules"))
                .check(classes);
    }

    /**
     * ARCH-014: meta-designer types (codegen / ddl / schema / introspect / designer services)
     * are design-time only — reachable inside {@code meta.table} or from the {@code server-admin}
     * design shell. The meta-runtime jar cannot violate this at all (designer is absent from its
     * compile classpath); this rule guards the assembled application classpath.
     */
    @Test
    void metaDesignerIsOnlyReachedInsideMetaTableOrFromAdminShell() {
        DescribedPredicate<JavaClass> designerTypes = JavaClass.Predicates.resideInAnyPackage(
                "..meta.table.api.codegen..",
                "..meta.table.api.ddl..",
                "..meta.table.internal.ddl..",
                "..meta.table.internal.schema..",
                "..meta.table.internal.introspect..")
                .or(DescribedPredicate.describe(
                        "meta-designer types in shared packages",
                        javaClass -> META_DESIGNER_SHARED_TYPES.contains(javaClass.getName())));
        FreezingArchRule.freeze(
                classes().that(designerTypes)
                        .should()
                        .onlyBeAccessed()
                        .byAnyPackage("..meta.table..", "..archforge.server.admin..")
                        .because("ARCH-014: meta-designer is design-time only — reached inside meta.table" +
                                " or from the server-admin design shell"))
                .check(classes);
    }
}
