plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm")
    id("application")
    id("com.gradleup.shadow") version "9.0.0-beta4"
}

group = "ronsijm.templater"
version = rootProject.property("projectVersion") as String

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":core"))
    implementation(project(":common-ui"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")

    // FlatLaf - IntelliJ-like Look and Feel
    implementation("com.formdev:flatlaf:3.5.4")
    implementation("com.formdev:flatlaf-intellij-themes:3.5.4")
    implementation("com.formdev:flatlaf-extras:3.5.4")

    // Markdown rendering
    implementation("org.commonmark:commonmark:0.21.0")
    implementation("org.commonmark:commonmark-ext-gfm-tables:0.21.0")
    implementation("org.commonmark:commonmark-ext-task-list-items:0.21.0")

    // Syntax highlighting
    implementation("com.fifesoft:rsyntaxtextarea:3.3.4")

    // File watching
    implementation("io.methvin:directory-watcher:0.18.0")

    // Docking framework
    // Note: Version 0.12.1 uses deprecated API. Upgrading to 1.x requires significant refactoring
    // due to package structure changes. Keeping @file:Suppress("DEPRECATION") in docking files.
    implementation("io.github.andrewauclair:modern-docking-single-app:0.12.1")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.1")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.10.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.1")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.10.1")
}

kotlin {
    jvmToolchain(21)
}

application {
    mainClass.set("ronsijm.templater.standalone.TemplaterAppKt")
}

tasks.shadowJar {
    archiveBaseName.set("templater")
    archiveClassifier.set("")
    archiveVersion.set("")
    destinationDirectory.set(rootProject.layout.buildDirectory.dir("cli"))

    manifest {
        attributes["Main-Class"] = "ronsijm.templater.standalone.TemplaterAppKt"
    }
}

tasks.test {
    useJUnitPlatform()
}

// Create Windows batch file wrapper
tasks.register("createBatchFile") {
    dependsOn(tasks.shadowJar)

    val outputDir = rootProject.layout.buildDirectory.dir("cli").get().asFile
    val batchFile = File(outputDir, "templater.bat")

    doLast {
        batchFile.writeText("""
            @echo off
            REM Templater CLI Launcher
            REM This batch file launches the Templater JAR with appropriate Java options

            REM Check if Java is available
            where java >nul 2>nul
            if %ERRORLEVEL% NEQ 0 (
                echo Error: Java is not installed or not in PATH
                echo Please install Java 11 or later and try again
                exit /b 1
            )

            REM Get the directory where this batch file is located
            set "SCRIPT_DIR=%~dp0"

            REM Launch the JAR with all arguments passed through
            java -Xmx512m -jar "%SCRIPT_DIR%templater.jar" %*
        """.trimIndent())

        println("Created batch file: ${batchFile.absolutePath}")
    }
}

// Build the fat JAR and batch file by default
tasks.build {
    dependsOn(tasks.shadowJar, tasks.named("createBatchFile"))
}

