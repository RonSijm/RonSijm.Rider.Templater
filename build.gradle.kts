plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "2.1.0" apply false
    id("org.jetbrains.intellij.platform") version "2.10.5" apply false
    id("jacoco")
    id("com.google.devtools.ksp") version "2.1.0-1.0.29" apply false
    id("io.gitlab.arturbosch.detekt") version "1.23.7"
}

group = "ronsijm.templater"
version = project.property("projectVersion") as String

// Configure all subprojects
subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "io.gitlab.arturbosch.detekt")

    repositories {
        mavenCentral()
    }

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions {
            jvmTarget = "21"
        }
    }
}

// Detekt configuration for root project
detekt {
    buildUponDefaultConfig = true
    allRules = false
    config.setFrom("$rootDir/detekt.yml")
}

// Configure detekt for each subproject to use its own baseline
subprojects {
    afterEvaluate {
        extensions.findByType<io.gitlab.arturbosch.detekt.extensions.DetektExtension>()?.apply {
            buildUponDefaultConfig = true
            allRules = false
            config.setFrom("$rootDir/detekt.yml")
            baseline = file("$projectDir/detekt-baseline.xml")
        }
    }
}

tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
    reports {
        html.required.set(true)
        xml.required.set(true)
        txt.required.set(false)
        sarif.required.set(false)
    }
}

// Aggregate test task for all modules
tasks.register("allTests") {
    group = "verification"
    description = "Run all tests across all modules"
    dependsOn(":core:test", ":cli:test", ":plugin:test")
}

// Aggregate coverage task
tasks.register("allCoverage") {
    group = "verification"
    description = "Generate coverage reports for all modules"
    dependsOn(":core:jacocoTestReport", ":plugin:jacocoTestReport")
}

// Build everything
tasks.register("buildAll") {
    group = "build"
    description = "Build all modules including CLI JAR, CLI batch file, and plugin"
    dependsOn(":core:build", ":cli:shadowJar", ":cli:createBatchFile", ":plugin:buildPlugin")
}

