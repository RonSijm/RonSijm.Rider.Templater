plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "2.1.0"
    id("org.jetbrains.intellij.platform") version "2.1.0"
    id("jacoco")
    id("com.google.devtools.ksp") version "2.1.0-1.0.29"
}

group = "ronsijm.templater"
version = "1.0.2"

repositories {
    mavenCentral()

    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    implementation("org.yaml:snakeyaml:2.2")
    implementation("com.google.code.gson:gson:2.10.1")
    // Note: kotlinx-coroutines is already provided by IntelliJ Platform
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.1")

    // KSP for handler auto-registration
    ksp(project(":ksp-codegen"))

    intellijPlatform {
        // Build against IntelliJ IDEA Community (works for all JetBrains IDEs)
        intellijIdeaCommunity("2025.2.3")
        instrumentationTools()
        pluginVerifier()
        zipSigner()
    }
}

kotlin {
    jvmToolchain(21)

    // Add KSP generated sources to the main source set
    sourceSets.main {
        kotlin.srcDir("build/generated/ksp/main/kotlin")
    }
}

intellijPlatform {
    buildSearchableOptions = false
    instrumentCode = false

    pluginConfiguration {
        id = "ronsijm.templater"
        name = "Templater"
        version = "1.0.2"
        description = "Template engine for JetBrains IDEs with Obsidian Templater-like syntax"

        ideaVersion {
            sinceBuild = "252"
            untilBuild = provider { null } // No upper bound - allow any future version
        }
    }

    signing {
        certificateChain = providers.environmentVariable("CERTIFICATE_CHAIN")
        privateKey = providers.environmentVariable("PRIVATE_KEY")
        password = providers.environmentVariable("PRIVATE_KEY_PASSWORD")
    }

    publishing {
        token = providers.environmentVariable("PUBLISH_TOKEN")
    }

    pluginVerification {
        ides {
            ide(org.jetbrains.intellij.platform.gradle.IntelliJPlatformType.IntellijIdeaCommunity, "2025.2.3")
        }
    }
}

tasks.named<Zip>("buildPlugin") {
    archiveBaseName.set("templater")
}



tasks {
    test {
        useJUnitPlatform()
        // NOTE: There's a known issue with the IntelliJ Platform Gradle Plugin 2.1.0
        // where unit tests fail with "Index: 1, Size: 1" error due to IDE home variable resolution.
        // The tests themselves are correct, but the plugin's test infrastructure has a bug.
        // Use the 'unitTest' task instead which bypasses the IntelliJ Platform plugin.
        enabled = false
    }

    // Custom unit test task that bypasses IntelliJ Platform plugin issues
    register<Test>("unitTest") {
        group = "verification"
        description = "Run unit tests without IntelliJ Platform dependencies"

        testClassesDirs = sourceSets["test"].output.classesDirs
        classpath = sourceSets["test"].runtimeClasspath

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

    // Custom task to test date functions without IntelliJ Platform
    register<JavaExec>("testDateFunctions") {
        group = "verification"
        description = "Test date functions standalone"
        dependsOn("compileKotlin")
        classpath = sourceSets["main"].runtimeClasspath
        mainClass.set("com.github.templater.modules.date.DateUtilsTestKt")
        standardOutput = System.out
        errorOutput = System.err
    }
}

// JaCoCo configuration for code coverage
jacoco {
    toolVersion = "0.8.11"
}

tasks.jacocoTestReport {
    dependsOn(tasks.named("unitTest"))

    // Use execution data from unitTest task
    executionData(fileTree(project.buildDir).include("jacoco/*.exec"))

    sourceSets(sourceSets["main"])

    reports {
        xml.required.set(true)
        html.required.set(true)
        csv.required.set(false)
    }

    // Exclude generated code and test code
    classDirectories.setFrom(
        files(classDirectories.files.map {
            fileTree(it) {
                exclude(
                    "**/ArgumentParser*",  // Requires IDE runtime
                    "**/META-INF/**"
                )
            }
        })
    )
}

tasks.jacocoTestCoverageVerification {
    dependsOn(tasks.named("unitTest"))

    violationRules {
        rule {
            limit {
                minimum = "0.80".toBigDecimal()  // 80% coverage target
            }
        }
    }
}

// Add coverage task that generates report
tasks.register("coverage") {
    group = "verification"
    description = "Generate coverage report"
    dependsOn(tasks.jacocoTestReport)

    doLast {
        val reportFile = file("build/reports/jacoco/test/html/index.html")
        if (reportFile.exists()) {
            println("\n" + "=".repeat(80))
            println("Coverage report generated successfully!")
            println("Open in browser: file:///${reportFile.absolutePath}")
            println("=".repeat(80) + "\n")
        }
    }
}

