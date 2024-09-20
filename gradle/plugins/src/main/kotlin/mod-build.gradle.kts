import build.plugin.Common

plugins {
    id("java")
    id("java-library")
    id("maven-publish")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
    withSourcesJar()
}

repositories {
    mavenCentral()
    maven("https://maven.wallentines.org/releases")

    if ((version as String).endsWith("-SNAPSHOT")) {
        maven("https://maven.wallentines.org/snapshots")
        mavenLocal()
    }
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.11.0"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.0")
}

tasks.withType<Jar> {
    archiveBaseName.set(Common.getArchiveName(project, rootProject))
}

tasks.withType<Test> {
    useJUnitPlatform()
    workingDir("run/test")
}