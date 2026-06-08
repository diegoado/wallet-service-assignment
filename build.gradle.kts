import kotlinx.kover.gradle.plugin.dsl.AggregationType
import kotlinx.kover.gradle.plugin.dsl.CoverageUnit
import kotlinx.kover.gradle.plugin.dsl.GroupingEntityType
import org.jlleitschuh.gradle.ktlint.reporter.ReporterType

buildscript {
  repositories {
    mavenLocal()
    mavenCentral()
  }
  dependencies {
    classpath("info.solidsoft.gradle.pitest:gradle-pitest-plugin:1.19.0")
  }
}

plugins {
  idea
  eclipse
  id("application")
  id("org.springframework.boot") version "4.0.6"
  id("io.spring.dependency-management") version "1.1.7"
  id("org.jetbrains.kotlinx.kover") version "0.9.8"
  id("org.jlleitschuh.gradle.ktlint") version "14.2.0"
  id("info.solidsoft.pitest") version "1.19.0"
  kotlin("jvm") version "2.2.21"
  kotlin("plugin.spring") version "2.2.21"
  kotlin("plugin.jpa") version "2.2.21"
}

application {
  mainClass.set("com.assignment.wallet.WalletApplicationKt")
}

java {
  toolchain {
    languageVersion = JavaLanguageVersion.of(25)
  }
  targetCompatibility = JavaVersion.VERSION_24
}

kotlin {
  compilerOptions {
    freeCompilerArgs.addAll(
      "-Xjsr305=strict",
      "-Xemit-jvm-type-annotations",
      "-Xannotation-default-target=param-property"
    )
  }
}

ktlint {
  version.set("1.4.1")
}

repositories {
  mavenLocal()
  mavenCentral()
}

sourceSets {
  create(
    "integrationTest",
    Action<SourceSet> {
      java {
        srcDir("src/integrationTest/java")
      }
      kotlin {
        srcDir("src/integrationTest/kotlin")
      }
      resources {
        srcDir("src/integrationTest/resources")
      }
      compileClasspath += sourceSets.main.get().output
      runtimeClasspath += sourceSets.main.get().output
    }
  )
}

extra["springCloudVersion"] = "2025.1.1"
extra["cucumber"] = "7.34.3"
extra["testContainers"] = "4.0.2"

configurations {
  compileOnly {
    extendsFrom(configurations.annotationProcessor.get())
  }
  getByName("integrationTestImplementation") {
    extendsFrom(configurations.testImplementation.get())
  }
  getByName("integrationTestRuntimeOnly") {
    extendsFrom(configurations.testRuntimeOnly.get())
  }
}

dependencyManagement {
  imports {
    mavenBom("org.springframework.cloud:spring-cloud-dependencies:${property("springCloudVersion")}")
    mavenBom("io.cucumber:cucumber-bom:${property("cucumber")}")
    mavenBom("com.playtika.testcontainers:testcontainers-spring-boot-bom:${property("testContainers")}")
  }
}

dependencies {
  implementation("org.springframework.boot:spring-boot-starter-actuator")
  implementation("org.springframework.boot:spring-boot-starter-web")
  implementation("org.springframework.boot:spring-boot-starter-opentelemetry")
  implementation("org.springframework.boot:spring-boot-starter-data-jpa")
  implementation("org.springframework.boot:spring-boot-starter-flyway")
  implementation("io.github.wimdeblauwe:error-handling-spring-boot-starter:5.1.1")
  implementation("org.flywaydb:flyway-database-postgresql")
  implementation("org.redisson:redisson-spring-boot-starter:4.4.0")
  implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:3.0.3")
  implementation("com.github.f4b6a3:uuid-creator:6.1.0")
  implementation(kotlin("reflect"))
  implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

  implementation("io.micrometer:micrometer-tracing")
  implementation("net.logstash.logback:logstash-logback-encoder:9.0")

  runtimeOnly("org.postgresql:postgresql")
  runtimeOnly("io.micrometer:micrometer-tracing-bridge-otel")
  runtimeOnly("io.micrometer:micrometer-registry-otlp")

  annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

  testImplementation("org.springframework.boot:spring-boot-starter-test")
  testImplementation(kotlin("test-junit5"))
  testImplementation("io.mockk:mockk:1.14.11")

  testRuntimeOnly("org.junit.platform:junit-platform-launcher")

  "integrationTestImplementation"("org.springframework.cloud:spring-cloud-starter-bootstrap")
  "integrationTestImplementation"("org.springframework.cloud:spring-cloud-starter-openfeign")
  "integrationTestImplementation"("io.cucumber:cucumber-java")
  "integrationTestImplementation"("io.cucumber:cucumber-spring")
  "integrationTestImplementation"("io.cucumber:cucumber-junit-platform-engine")
  "integrationTestImplementation"("org.junit.platform:junit-platform-suite")
  "integrationTestImplementation"("org.junit.platform:junit-platform-engine")
  "integrationTestImplementation"("com.playtika.testcontainers:embedded-postgresql")
  "integrationTestImplementation"("com.playtika.testcontainers:embedded-redis")

  "integrationTestRuntimeOnly"("org.junit.platform:junit-platform-console")
}

group = "com.assignment"
version = "0.0.1"

val excludedAnalysisFiles =
  listOf(
    "**/*\$Companion**",
    "**/*\$DefaultImpls**",
    "**/WalletApplication**",
    "**/application/controller/**",
    "**/domain/dto/**",
    "**/domain/exception/**",
    "**/infrastructure/configuration/**",
    "**/infrastructure/persistence/**",
    "**/Loggable**",
    "**/WalletFactory**",
    "**/Utilities**"
  )

tasks {
  withType<Test> {
    useJUnitPlatform()
    testLogging {
      events("passed", "skipped", "failed")
    }
  }

  withType<Copy> {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
  }

  springBoot {
    buildInfo()
  }

  bootJar {
    doFirst {
      manifest {
        attributes("Implementation-Title" to project.name, "Implementation-Version" to project.version)
      }
    }
  }

  getByName<Zip>("distZip").enabled = false
  getByName<Tar>("distTar").enabled = false

  test {
    exclude("integrationTest")
  }

  ktlint {
    android.set(false)
    outputToConsole.set(true)
    outputColorName.set("RED")
    ignoreFailures.set(false)
    enableExperimentalRules.set(false)
    reporters {
      reporter(ReporterType.HTML)
    }
  }

  val consoleLauncherTest by registering(JavaExec::class) {
    description = "Runs the Cucumber tests using JUnit Platform Console Launcher."

    dependsOn(testClasses)
    val reportsDir = file("${layout.buildDirectory.get()}/reports/cucumber")
    outputs.dir(reportsDir)
    classpath = sourceSets["integrationTest"].runtimeClasspath
    mainClass.set("org.junit.platform.console.ConsoleLauncher")
    args(
      "execute",
      "--scan-class-path",
      "--reports-dir=$reportsDir",
      "--include-engine=cucumber",
      "--details=tree",
      "--disable-banner",
      "--disable-ansi-colors"
    )
  }

  register("integrationTest", Test::class) {
    description = "Runs the integration test suite."
    group = "verification"

    testClassesDirs = sourceSets["integrationTest"].output.classesDirs

    dependsOn(consoleLauncherTest)
    exclude("**/*")
  }
}

kover {
  useJacoco("0.8.14")

  currentProject {
    instrumentation {
      disabledForTestTasks.addAll("integrationTest")
    }
    sources {
      excludedSourceSets.addAll("integrationTest")
    }
  }
  reports {
    total {
      filters {
        excludes {
          classes(excludedAnalysisFiles.map { it.replace("/", ".") })
        }
      }
      xml {
        onCheck = true
      }
      html {
        title = "${project.name} Coverage Report"
        onCheck = true
        charset = "UTF-8"
      }
      verify {
        onCheck = true

        rule("lines") {
          disabled = false
          groupBy = GroupingEntityType.CLASS

          bound {
            minValue = 90
            coverageUnits = CoverageUnit.LINE
            aggregationForGroup = AggregationType.COVERED_PERCENTAGE
          }
        }
        rule("branches") {
          disabled = false
          groupBy = GroupingEntityType.CLASS

          bound {
            minValue = 85
            coverageUnits = CoverageUnit.LINE
            aggregationForGroup = AggregationType.COVERED_PERCENTAGE
          }
        }
      }
    }
  }
}

pitest {
  junit5PluginVersion.set("1.2.1")
  avoidCallsTo.set(setOf("kotlin.jvm.internal", "org.slf4j"))
  mutators.set(setOf("DEFAULTS"))
  mainSourceSets.set(listOf(sourceSets.main.get()))
  testSourceSets.set(listOf(sourceSets.test.get()))
  jvmArgs.set(listOf("-Xmx2g"))
  threads.set(Runtime.getRuntime().availableProcessors())
  outputFormats.set(setOf("XML", "HTML"))
  excludedClasses.set(excludedAnalysisFiles.map { it.replace("/", ".") })
  mutationThreshold.set(80)
  coverageThreshold.set(80)
}
