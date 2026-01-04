plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm")
    id("jacoco")
}

group = "ronsijm.templater"
version = "1.0.2"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":core"))
    
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.1")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.10.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.1")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.10.1")
}

kotlin {
    jvmToolchain(21)
}

tasks.test {
    useJUnitPlatform()
    
    testLogging {
        events("passed", "skipped", "failed")
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
        showStandardStreams = true
    }
    
    extensions.configure(JacocoTaskExtension::class) {
        isEnabled = true
    }
}

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

