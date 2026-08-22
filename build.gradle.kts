plugins {
    id("org.sonarqube") version "7.3.0.8198"
    id("com.diffplug.spotless") version "6.13.0" apply false
    jacoco
    idea
}

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

            // compile - Lombok配置
            add("annotationProcessor", "org.projectlombok:lombok:1.18.46")
            add("testAnnotationProcessor", "org.projectlombok:lombok:1.18.46")

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
