plugins {
    // GraalVM native 插件提升到根 scope（apply false）：server-admin 与 server-web 共用同一
    // 插件 classloader，否则两侧插件 classpath 不同时 GraalVMReachabilityMetadataService 报
    // "loaded with different classloader"（Gradle 官方建议的修法）。
    id("org.graalvm.buildtools.native") apply false
    id("org.sonarqube") version "7.3.0.8198"
    id("com.diffplug.spotless") version "6.13.0" apply false
    id("net.ltgt.errorprone") version "5.1.1" apply false
    id("com.github.spotbugs") version "6.5.11" apply false
    id("org.owasp.dependencycheck") version "13.0.0" apply false
    id("de.thetaphi.forbiddenapis") version "3.10" apply false
    jacoco
    idea
}

// ---- Static analysis toolchain ----
// Error Prone: bug detection inside javac — runs on EVERY compile automatically.
val errorproneToolVersion = "2.50.0"

// NullAway ratchet — modules whose sources are already NullAway-clean are enforced
// at ERROR for that source set. Drive both lists to cover every module, then flip
// the default severity to ERROR and delete these lists.
val nullAwayCleanMain =
    setOf(
            "archforge-admin-user",
            "archforge-blog",
            "archforge-cache-starter",
            "archforge-cli",
            "archforge-common-error",
            "archforge-common-jpa",
            "archforge-example-task",
            "archforge-lock-starter")
val nullAwayCleanTest = setOf<String>()
// Checkstyle: semantic style gate — runs before `test` and as part of `check`/`build`
// (division of labor: formatting stays with Spotless, see config/checkstyle/checkstyle.xml)
val checkstyleToolVersion = "10.26.1"
// SpotBugs: bytecode-level bug patterns; engine 4.10.x (Java 25 classes)
val spotbugsToolVersion = "4.10.4"

// JaCoCo 0.8.15+: 官方支持 Java 25/26 class 文件
val jacocoToolVersion = "0.8.15"

group = "com.lesofn.archforge"
version = "0.1.0-SNAPSHOT"

allprojects {
    repositories {
        // 阿里云镜像（首选）
        maven { url = uri("https://maven.aliyun.com/repository/public/") }
        maven { url = uri("https://maven.aliyun.com/repository/spring/") } // Spring 生态专用
        maven { url = uri("https://maven.aliyun.com/repository/google/") } // Google 依赖专用

        // 腾讯云镜像（备选）
        maven { url = uri("https://mirrors.cloud.tencent.com/nexus/repository/maven-public/") }
 
        // 华为云镜像（备选）
        maven { url = uri("https://repo.huaweicloud.com/repository/maven/") }
 
        // 原始仓库（如果镜像源找不到依赖，回退到中央仓库）
        mavenCentral()
        google()
    }
}

sonarqube {
    properties {
        property("sonar.projectKey", "ArchForge")
        property(
            "sonar.coverage.jacoco.xmlReportPaths",
            layout.buildDirectory.file("reports/jacoco/jacocoAggregateReport.xml").get().asFile.absolutePath
        )
    }
}

subprojects {
    // 为除了 archforge-dependencies 之外的所有子项目应用插件
    if (name != "archforge-dependencies") {
        apply(plugin = "java-library")
        apply(plugin = "groovy")
        apply(plugin = "com.diffplug.spotless")
        apply(plugin = "jacoco")
        apply(plugin = "idea")
        apply(plugin = "checkstyle")
        apply(plugin = "net.ltgt.errorprone")
        apply(plugin = "com.github.spotbugs")
        apply(plugin = "de.thetaphi.forbiddenapis")
        apply(plugin = "org.owasp.dependencycheck")

        configure<JacocoPluginExtension> {
            toolVersion = jacocoToolVersion
        }

        tasks.withType<JacocoReport> {
            dependsOn(tasks.named("test"))
            reports {
                xml.required.set(true)
                html.required.set(true)
                csv.required.set(false)
            }
        }

        // 让 IDEA 自动识别 annotation processor 生成的源码目录 (Hibernate Metamodel 等)
        configure<org.gradle.plugins.ide.idea.model.IdeaModel> {
            module {
                generatedSourceDirs.add(file("build/generated/sources/annotationProcessor/java/main"))
            }
        }

        // Spotless 代码格式化 - Eclipse JDT Formatter
        configure<com.diffplug.gradle.spotless.SpotlessExtension> {
            lineEndings = com.diffplug.spotless.LineEnding.UNIX
            java {
                target("src/*/java/**/*.java")
                eclipse().configFile(rootProject.file("config/spotless/eclipse-format.xml"))
                trimTrailingWhitespace()
                endWithNewline()
            }
        }

        // ---- Checkstyle（语义风格门禁）----
        // 规则与 Spotless 分工：格式归 Spotless，Checkstyle 管 import/命名/控制流等语义规则。
        // 违规阻断 test/check/build（见下方 tasks.test 的 dependsOn 挂接）。
        configure<CheckstyleExtension> {
            toolVersion = checkstyleToolVersion
            // configDirectory sets config_loc (Gradle 9 rejects a manual configProperties entry)
            configDirectory = rootProject.file("config/checkstyle")
            configFile = rootProject.file("config/checkstyle/checkstyle.xml")
            isShowViolations = true
        }

        // ---- Error Prone（编译期 bug 检测，随 javac 自动执行）----
        dependencies {
            "errorprone"("com.google.errorprone:error_prone_core:$errorproneToolVersion")
            // NullAway: null-safety analysis on top of Error Prone (jspecify @NullMarked base)
            "errorprone"("com.uber.nullaway:nullaway:0.14.1")
        }
        // 5.x DSL: the options extension lives on CompileOptions (options.errorprone { … })
        tasks.withType<JavaCompile>().configureEach {
            (options as org.gradle.api.plugins.ExtensionAware).extensions
                .configure<net.ltgt.gradle.errorprone.ErrorProneOptions>("errorprone") {
                    // Baseline severity strategy: default (ERROR) blocks compile for genuine bug
                    // patterns. Project-specific downgrades live here and must carry a reason.

                    // UnusedVariable crashes Error Prone 2.50.0 itself (overlapping SuggestedFix
                    // ranges, upstream bug — see TokenService.java) — OFF until an upgrade fixes it.
                    check("UnusedVariable", net.ltgt.gradle.errorprone.CheckSeverity.OFF)

                    // NullAway: 分析 jspecify @NullMarked 的包（项目全量 @NullMarked）。
                    // Ratchet 策略：已清零的模块/source-set 立即升 ERROR 锁死进度，
                    // 存量模块保持 WARN 直到清零（见 nullAwayCleanMain/Test 集合）。
                    val nullAwayClean =
                        when (name) {
                            "compileJava" -> nullAwayCleanMain.contains(project.name)
                            "compileTestJava" -> nullAwayCleanTest.contains(project.name)
                            else -> false
                        }
                    check(
                            "NullAway",
                            if (nullAwayClean) net.ltgt.gradle.errorprone.CheckSeverity.ERROR
                            else net.ltgt.gradle.errorprone.CheckSeverity.WARN)
                    option("NullAway:AnnotatedPackages", "com.lesofn.archforge")
                    // Framework-injected fields are initialized outside javac's view:
                    // Spring DI, JPA, picocli command-line binding. NullAway reads this
                    // flag as a single comma-separated value (repeated options don't
                    // accumulate).
                    option(
                            "NullAway:ExcludedFieldAnnotations",
                            listOf(
                                    "org.springframework.beans.factory.annotation.Autowired",
                                    "org.springframework.beans.factory.annotation.Value",
                                    "jakarta.annotation.Resource",
                                    "jakarta.inject.Inject",
                                    "jakarta.persistence.PersistenceContext",
                                    "picocli.CommandLine.Option",
                                    "picocli.CommandLine.Parameters",
                                    "picocli.CommandLine.ParentCommand",
                                    "picocli.CommandLine.Mixin",
                                    "picocli.CommandLine.Spec")
                                .joinToString(","))
                    // Generated code (MapStruct impls etc.) is not hand-written — exclude from all checks.
                    excludedPaths.set(".*/build/generated/sources/.*")
                }
        }

        // Hand-written annotation stubs are an accepted test idiom; the equals/hashCode
        // contract they technically violate is irrelevant for read-only stubs.
        tasks.named<JavaCompile>("compileTestJava") {
            (options as org.gradle.api.plugins.ExtensionAware).extensions
                .configure<net.ltgt.gradle.errorprone.ErrorProneOptions>("errorprone") {
                    check("BadAnnotationImplementation", net.ltgt.gradle.errorprone.CheckSeverity.WARN)
                }
        }

        // ---- Forbidden APIs（禁用 API 门禁，语义级防退化）----
        // 禁：System.out/err、Runtime.exec、Thread.sleep、java.util.Date/Calendar/SimpleDateFormat、
        // 内部 JDK API、sun.misc.Unsafe（bundled）+ 项目自定义签名（见 config/forbiddenapis）。
        // archforge-cli 豁免 system-out/untime（CLI/MCP stdio 协议需要直写 stdout、起进程）。
        configure<de.thetaphi.forbiddenapis.gradle.CheckForbiddenApisExtension> {
            bundledSignatures.addAll(
                listOf("jdk-system-out", "jdk-deprecated", "jdk-internal")
            )
            signaturesFiles = rootProject.files("config/forbiddenapis/forbidden-signatures.txt")
            // 豁免须用 de.thetaphi.forbiddenapis.SuppressForbidden（勿把任意 @SuppressWarnings 当跳过）
            ignoreFailures = false
        }
        // 豁免：archforge-cli（MCP stdio 协议直写 stdout）与全部测试源集
        //（测试的调试打印是普遍惯例——main 源集保持严格禁止）
        val projectName = name
        tasks.matching { it.name == "forbiddenApisMain" || it.name == "forbiddenApisTest" }.configureEach {
            if (this !is de.thetaphi.forbiddenapis.gradle.CheckForbiddenApis) {
                return@configureEach // groovy 聚合 task 等非标准实现
            }
            if (projectName == "archforge-cli" || name == "forbiddenApisTest") {
                // CLI 直写 stdout（MCP stdio 协议）；测试的调试打印是惯例——main 源集保持严格
                bundledSignatures.remove("jdk-system-out")
            }
            if (name == "forbiddenApisTest") {
                // 测试正当使用 sleep（等待调度触发）/stdout/Date fixture：
                // 换用宽松签名集，仅保留 bundled 的 deprecated/internal 禁令
                signaturesFiles = rootProject.files("config/forbiddenapis/forbidden-signatures-test.txt")
            }
        }
        // ForbiddenApis 在编译后即可运行，挂到 test 前与 check 链
        tasks.named("test") {
            dependsOn(tasks.named("forbiddenApisMain"), tasks.named("forbiddenApisTest"))
        }

        // ---- SpotBugs（字节码级 bug 检测）----
        configure<com.github.spotbugs.snom.SpotBugsExtension> {
            toolVersion.set(spotbugsToolVersion)
            excludeFilter.set(rootProject.file("config/spotbugs/spotbugs-exclude.xml"))
        }
        // SpotBugs 属于 check 链（check 汇总 spotbugsMain/spotbugsTest）；不挂 test 前置（字节码分析较重，
        // 与编译产物解耦，保留 test 的快速反馈环）。
        // 测试源集使用更宽的排除（mock/spock/测试 fixture 的模式噪音），见 spotbugs-exclude-test.xml
        tasks.matching { it.name == "spotbugsTest" }.configureEach {
            if (this is com.github.spotbugs.snom.SpotBugsTask) {
                excludeFilter.set(rootProject.file("config/spotbugs/spotbugs-exclude-test.xml"))
            }
        }

        // ---- OWASP Dependency Check（CVE/transitive 依赖漏洞扫描）----
        // 独立 task：./gradlew dependencyCheckAnalyze（不挂 check/build 链——NVD 数据
        // 首次下载量大，会拖慢日常构建；CI 走独立 job）。
        // 注：dependencyCheckAggregate 在 Gradle 9 的跨项目 configuration 解析锁下
        // 不可用（upstream 兼容问题），故用逐模块 analyze。
        configure<org.owasp.dependencycheck.gradle.extension.DependencyCheckExtension> {
            // CVSS 7.0+（高危及以上）才阻断；中低危进报告人工评估
            setFailBuildOnCVSS(7.0f)
            suppressionFile = "config/dependency-check/suppressions.xml"
            nvd {
                // NVD API 需要 key（13.x 强制）；CI 注入 OWASP_NVD_API_KEY。
                // 注意：无 key 环境下引擎的 NVD update 异常无法通过 DSL 关闭
                // （上游 13.0.0 限制），本地跑需联网+key，见 ci.yml 的专用 job。
                val nvdKey = providers.environmentVariable("OWASP_NVD_API_KEY").getOrElse("")
                if (nvdKey.isNotBlank()) {
                    apiKey = nvdKey
                }
            }
            analyzers {
                // OSS Index（Sonatype，聚合 OSV/CVE 数据，匿名可用）作为无 NVD key 环境的
                // 主数据源；远端故障降级为告警而非失败
                ossIndex {
                    enabled = true
                    warnOnlyOnRemoteErrors = true
                }
            }
            // 数据源远端不可达（离线沙箱/无 key）时不阻断构建——报告会标注数据陈旧
            failOnError = false
            formats = listOf("HTML", "JSON")
            // 只扫运行时 classpath，不扫测试工具链
            scanConfigurations = listOf("runtimeClasspath")
        }

        // ---- 静态分析执行时机 ----
        // 编译：Error Prone 内嵌于 javac，任何 compileJava/compileTestJava 即执行。
        // 测试：跑 test 前必须先过 Checkstyle（风格失败不进入测试阶段）。
        tasks.named("test") {
            dependsOn(tasks.named("checkstyleMain"), tasks.named("checkstyleTest"))
        }

        // 配置 Java 25
        configure<JavaPluginExtension> {
            toolchain {
                languageVersion.set(JavaLanguageVersion.of(25))
            }
        }

        tasks.withType<JavaCompile> {
            options.compilerArgs.addAll(listOf("-Xlint:deprecation", "-parameters", "--enable-preview"))
        }
        
        // 配置测试任务使用JUnit Platform
        tasks.withType<Test> {
            jvmArgs("--enable-preview", "--enable-native-access=ALL-UNNAMED")
            // JUnit @Tag 体系 (P0/P1/contract/slow):
            //   ./gradlew test -Ptags=P0,contract          只跑指定 tag
            //   ./gradlew build -PexcludeTags=slow         跳过慢速集成测试
            useJUnitPlatform {
                (rootProject.findProperty("tags") as String?)?.let { value ->
                    includeTags(*value.split(',').map { it.trim() }.filter { it.isNotEmpty() }
                        .toTypedArray())
                }
                (rootProject.findProperty("excludeTags") as String?)?.let { value ->
                    excludeTags(*value.split(',').map { it.trim() }.filter { it.isNotEmpty() }
                        .toTypedArray())
                }
            }
        }

        // 全局排除冲突的日志依赖
        configurations.all {
            exclude(group = "org.springframework.boot", module = "spring-boot-starter-logging")
            exclude(group = "ch.qos.logback", module = "logback-classic")
            exclude(group = "ch.qos.logback", module = "logback-core")
            exclude(group = "org.apache.logging.log4j", module = "log4j-to-slf4j")

            // 项目标准: 禁止 EasyExcel - 必须使用 org.dhatim:fastexcel
            // 任何模块（包括传递依赖）引入 com.alibaba:easyexcel* 都会导致构建失败
            exclude(group = "com.alibaba", module = "easyexcel")
            exclude(group = "com.alibaba", module = "easyexcel-core")
            resolutionStrategy.eachDependency {
                if (requested.group == "com.alibaba" && requested.name.startsWith("easyexcel")) {
                    throw GradleException(
                        "EasyExcel is forbidden in ArchForge. Use org.dhatim:fastexcel instead. " +
                            "Pulled in: ${requested.group}:${requested.name}:${requested.version}"
                    )
                }
            }
        }
        
        dependencies {
            // 引入 Spring Boot dependencies
            add("implementation", platform("org.springframework.boot:spring-boot-dependencies:4.1.0"))
            // 引入自定义 dependencies
            add("implementation", platform(project(":archforge-dependencies")))
            // annotationProcessor 不继承 implementation——平台需单独挂，版本单源在 BOM
            add("annotationProcessor", platform(project(":archforge-dependencies")))
            add("testAnnotationProcessor", platform(project(":archforge-dependencies")))

            // compile - Lombok配置（版本由 BOM 管理）
            add("annotationProcessor", "org.projectlombok:lombok")
            add("testAnnotationProcessor", "org.projectlombok:lombok")

            // 全局测试依赖 - Spock 2.4 (Groovy 5.x)
            add("testImplementation", "org.junit.jupiter:junit-jupiter-api")
            add("testRuntimeOnly", "org.junit.jupiter:junit-jupiter-engine")
            add("testImplementation", "org.spockframework:spock-core")
            add("testImplementation", "org.spockframework:spock-spring")
            add("testImplementation", "org.springframework.boot:spring-boot-starter-test") {
                exclude(group = "org.springframework.boot", module = "spring-boot-starter-logging")
                exclude(group = "ch.qos.logback", module = "logback-classic")
                exclude(group = "org.apache.logging.log4j", module = "log4j-to-slf4j")
            }
            add("testImplementation", "org.apache.groovy:groovy")
            add("testImplementation", "org.junit.platform:junit-platform-launcher")
        }
    }
}

// 聚合覆盖率报告: ./gradlew jacocoAggregateReport (先报告不门禁, 门禁在 CI diff coverage 阶段)
val coverageModules = subprojects.filter { it.name != "archforge-dependencies" }

tasks.register<JacocoReport>("jacocoAggregateReport") {
    group = "verification"
    description = "Aggregated JaCoCo coverage report across all modules"
    dependsOn(coverageModules.map { it.tasks.named("test") })
    // 源码目录同时被 spotless 任务写入, 需显式声明依赖避免隐式依赖告警
    coverageModules.forEach { module ->
        dependsOn(module.tasks.matching { it.name.startsWith("spotless") })
    }
    executionData.setFrom(
        rootProject.fileTree(rootDir) {
            include("**/build/jacoco/*.exec")
            exclude("build/**", ".worktrees/**")
        }
    )
    val mainSourceSets = coverageModules.mapNotNull { module ->
        module.extensions.findByType(JavaPluginExtension::class.java)?.sourceSets?.findByName("main")
    }
    additionalSourceDirs.setFrom(mainSourceSets.flatMap { it.allJava.srcDirs })
    sourceDirectories.setFrom(mainSourceSets.flatMap { it.allSource.srcDirs })
    classDirectories.setFrom(mainSourceSets.flatMap { it.output.classesDirs })
    reports {
        xml.required.set(true)
        xml.outputLocation.set(layout.buildDirectory.file("reports/jacoco/jacocoAggregateReport.xml"))
        html.required.set(true)
        html.outputLocation.set(layout.buildDirectory.dir("reports/jacoco/html"))
        csv.required.set(false)
    }
}
