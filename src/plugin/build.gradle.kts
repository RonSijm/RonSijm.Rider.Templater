plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.intellij.platform")
    id("org.jetbrains.changelog") version "2.2.1"
    id("jacoco")
}

group = "ronsijm.templater"
version = rootProject.property("projectVersion") as String

repositories {
    mavenCentral()
    
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    implementation(project(":core"))

    testImplementation("junit:junit:4.13.2")

    intellijPlatform {
        intellijIdeaCommunity("2025.2.3")
        instrumentationTools()
        pluginVerifier()
        zipSigner()
        testFramework(org.jetbrains.intellij.platform.gradle.TestFrameworkType.Platform)

        // Optional dependency on Markdown plugin for preview styling
        bundledPlugin("org.intellij.plugins.markdown")
    }
}

kotlin {
    jvmToolchain(21)
}

// Configure changelog plugin
changelog {
    version.set(rootProject.property("projectVersion") as String)
    path.set(rootProject.file("CHANGELOG.md").canonicalPath)
    header.set(provider { "[${version.get()}]" })
    headerParserRegex.set("""(\d+\.\d+\.\d+)""".toRegex())
    itemPrefix.set("-")
    keepUnreleasedSection.set(true)
    unreleasedTerm.set("[Unreleased]")
    groups.set(listOf("Added", "Changed", "Deprecated", "Removed", "Fixed", "Security"))
}

intellijPlatform {
    buildSearchableOptions = false
    instrumentCode = false

    pluginConfiguration {
        id = "ronsijm.templater"
        name = "Templater"
        version = rootProject.property("projectVersion") as String
        description = "Template engine for JetBrains IDEs with Obsidian Templater-like syntax"

        // Add change notes from CHANGELOG.md
        changeNotes.set(provider {
            changelog.renderItem(
                changelog.get(rootProject.property("projectVersion") as String)
                    .withHeader(false)
                    .withEmptySections(false),
                org.jetbrains.changelog.Changelog.OutputType.HTML
            )
        })

        ideaVersion {
            sinceBuild = "252"
            untilBuild = provider { null }
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
    destinationDirectory.set(rootProject.layout.buildDirectory.dir("plugin"))
}

tasks {
    test {
        testLogging {
            events("passed", "skipped", "failed")
            exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
            showStandardStreams = true
        }
    }
}

jacoco {
    toolVersion = "0.8.11"
}

tasks.jacocoTestReport {
    dependsOn(tasks.named("test"))
    executionData(fileTree(layout.buildDirectory).include("jacoco/*.exec"))
    sourceSets(sourceSets["main"])

    reports {
        xml.required.set(true)
        html.required.set(true)
        csv.required.set(false)
    }
}

