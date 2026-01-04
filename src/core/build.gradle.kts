plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm")
    id("jacoco")
    id("com.google.devtools.ksp")
    id("info.solidsoft.pitest") version "1.15.0"
}

group = "ronsijm.templater"
version = rootProject.property("projectVersion") as String

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.yaml:snakeyaml:2.2")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    
    // KSP for handler auto-registration
    ksp(project(":ksp-codegen"))
    
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.1")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.10.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.1")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.10.1")

    // Kotest for property-based testing
    testImplementation("io.kotest:kotest-runner-junit5:5.8.0")
    testImplementation("io.kotest:kotest-assertions-core:5.8.0")
    testImplementation("io.kotest:kotest-property:5.8.0")
}

kotlin {
    jvmToolchain(21)
    
    // Add KSP generated sources to the main source set
    sourceSets.main {
        kotlin.srcDir("build/generated/ksp/main/kotlin")
    }
}

// Check if slow tests should be included (via -PincludeSlow=true or -Pslow)
val includeSlow = project.hasProperty("includeSlow") || project.hasProperty("slow")

tasks.test {
    useJUnitPlatform {
        // Exclude slow tests by default (JUnit @Tag("slow"))
        if (!includeSlow) {
            excludeTags("slow")
        }
    }

    // Configure Kotest to exclude SlowTag when not running slow tests
    if (!includeSlow) {
        systemProperty("kotest.tags.exclude", "SlowTag")
    }

    testLogging {
        events("passed", "skipped", "failed")
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
        showStandardStreams = true
    }

    // Enable JaCoCo for code coverage
    extensions.configure(JacocoTaskExtension::class) {
        isEnabled = true
    }
}

// Task to run all tests including slow ones
tasks.register<Test>("testAll") {
    description = "Run all tests including slow tests (benchmarks, property tests, mermaid generation)"
    group = "verification"

    useJUnitPlatform()

    testLogging {
        events("passed", "skipped", "failed")
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
        showStandardStreams = true
    }

    // Enable JaCoCo for code coverage
    extensions.configure(JacocoTaskExtension::class) {
        isEnabled = true
    }
}

// Task to run only slow tests
tasks.register<Test>("testSlow") {
    description = "Run only slow tests (benchmarks, property tests, mermaid generation)"
    group = "verification"

    useJUnitPlatform {
        includeTags("slow")
    }

    // Configure Kotest to only run SlowTag tests
    systemProperty("kotest.tags.include", "SlowTag")

    testLogging {
        events("passed", "skipped", "failed")
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
        showStandardStreams = true
    }
}

// JaCoCo configuration for code coverage
jacoco {
    toolVersion = "0.8.11"
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)

    reports {
        xml.required.set(true)
        html.required.set(true)
        csv.required.set(false)
    }
}

// Pitest (Mutation Testing) configuration
pitest {
    junit5PluginVersion.set("1.2.1")
    targetClasses.set(listOf("ronsijm.templater.*"))
    targetTests.set(listOf("ronsijm.templater.*"))
    threads.set(Runtime.getRuntime().availableProcessors())
    outputFormats.set(listOf("HTML", "XML"))
    mutationThreshold.set(0) // Start with 0, increase as coverage improves
    coverageThreshold.set(0)
    timestampedReports.set(false)

    // Exclude test infrastructure and generated code
    excludedClasses.set(listOf(
        "ronsijm.templater.benchmarks.*",
        "ronsijm.templater.property.*",
        "ronsijm.templater.generated.*"
    ))

    // Use default mutators for balanced mutation testing
    mutators.set(listOf("DEFAULTS"))
}

