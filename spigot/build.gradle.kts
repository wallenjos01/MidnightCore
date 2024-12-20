import buildlogic.Utils
import buildlogic.MultiShadow
import org.jetbrains.kotlin.parsing.parseBoolean

plugins {
    id("build.library")
    id("build.shadow")
    id("build.multiversion")
}

repositories {
    mavenLocal()
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://hub.spigotmc.org/nexus/repository/sonatype-nexus-snapshots/")
    maven("https://libraries.minecraft.net/")
}

Utils.setupResources(project, rootProject, "plugin.yml")
MultiShadow.setupShadow(project, multiVersion, 17, "1.17-1.20.4")
MultiShadow.setupShadow(project, multiVersion, 8, "1.8-1.16")

tasks.shadowJar {
    archiveClassifier.set("1.20.5-1.21")
}

// Versions compiled against Java 8
val versions = listOf(
    VersionInfo("1_8_R1","1.8", 8),
    VersionInfo("1_8_R2","1.8.3", 8),
    VersionInfo("1_8_R3","1.8.8", 8),
    VersionInfo("1_9_R1","1.9", 8),
    VersionInfo("1_9_R2","1.9.4", 8),
    VersionInfo("1_10_R1","1.10.2", 8),
    VersionInfo("1_11_R1","1.11.2", 8),
    VersionInfo("1_12_R1","1.12.2", 8),
    VersionInfo("1_13_R1","1.13", 8),
    VersionInfo("1_13_R2","1.13.1", 8),
    VersionInfo("1_13_R2v2","1.13.2", 8),
    VersionInfo("1_14_R1","1.14.4", 8),
    VersionInfo("1_15_R1","1.15.2", 8),
    VersionInfo("1_16_R1","1.16.1", 8),
    VersionInfo("1_16_R2","1.16.2", 8),
    VersionInfo("1_16_R3","1.16.4", 8),
    VersionInfo("1_17_R1","1.17.1", 17),
    VersionInfo("1_18_R1","1.18", 17),
    VersionInfo("1_18_R2","1.18.2", 17),
    VersionInfo("1_19_R1","1.19", 17),
    VersionInfo("1_19_R1v2","1.19.2", 17),
    VersionInfo("1_19_R2","1.19.3", 17),
    VersionInfo("1_19_R3","1.19.4", 17),
    VersionInfo("1_20_R1","1.20.1", 17),
    VersionInfo("1_20_R2","1.20.2", 17),
    VersionInfo("1_20_R3","1.20.4", 17),
    VersionInfo("1_20_R4","1.20.6", 21),
    VersionInfo("1_21_R1","1.21.1", 21),
    VersionInfo("1_21_R2","1.21.3", 21),
)


for(version in versions) {
    setupVersion(version)
}


dependencies {

    // MidnightCore
    implementation(project(":common"))
    implementation(project(":server"))
    implementation(project(":spigot:adapter"))

    shadow(project(":common").setTransitive(false))
    shadow(project(":server").setTransitive(false))
    shadow(project(":spigot:adapter").setTransitive(false))

    // Shadowed Library Dependencies
    shadow(libs.midnight.cfg) { isTransitive = false }
    shadow(libs.midnight.cfg.sql) { isTransitive = false }
    shadow(libs.midnight.cfg.json) { isTransitive = false }
    shadow(libs.midnight.cfg.gson) { isTransitive = false }
    shadow(libs.midnight.cfg.binary) { isTransitive = false }
    shadow(libs.midnight.cfg.nbt) { isTransitive = false }
    shadow(libs.midnight.lib) { isTransitive = false }
    shadow(libs.zstd.jni)

    "shadow8"(libs.slf4j.api)
    "shadow8"(libs.slf4j.simple)

    java8CompileOnly("org.spigotmc:spigot-api:1.16.5-R0.1-SNAPSHOT")
    java17CompileOnly("org.spigotmc:spigot-api:1.20.4-R0.1-SNAPSHOT")
    java21CompileOnly("org.spigotmc:spigot-api:1.21.3-R0.1-SNAPSHOT")

    compileOnly(libs.jetbrains.annotations)
}



fun setupVersion(version: VersionInfo) {

    val javaVersion: Int = version.javaVersion

    val set = sourceSets.create("v${version.name}")
    tasks.named<JavaCompile>("compileV${version.name}Java") {
        javaCompiler.set(project.javaToolchains.compilerFor {
            languageVersion.set(JavaLanguageVersion.of(javaVersion))
        })
    }

    dependencies {

        "java${javaVersion}CompileOnly"(set.output)

        if(javaVersion == multiVersion.defaultVersion) {
            shadow(set.output)
        } else {
            "shadow${javaVersion}"(set.output)
        }

        "v${version.name}CompileOnly"("org.spigotmc:spigot-api:${version.version}-R0.1-SNAPSHOT")
        "v${version.name}CompileOnly"("org.spigotmc:spigot:${version.version}-R0.1-SNAPSHOT")

        "v${version.name}CompileOnly"(libs.jetbrains.annotations)
        "v${version.name}CompileOnly"(libs.midnight.cfg)
        "v${version.name}CompileOnly"(libs.midnight.cfg.gson)

        // For whatever reason, intellisense cannot find project dependencies on source sets for other java versions.
        // Gradle still can, and compilation works fine, but intelliJ complains about it. To work around this, those
        // dependencies can be pulled from repositories instead. If this ever becomes a problem, set
        // spigot_intellisense_workaround to false in gradle.properties.
        if(javaVersion != multiVersion.defaultVersion && parseBoolean(project.properties["spigot_intellisense_workaround"] as String)) {
            "v${version.name}CompileOnly"("org.wallentines:midnightcore-common:${project.version}")
            "v${version.name}CompileOnly"("org.wallentines:midnightcore-server:${project.version}")
            "v${version.name}CompileOnly"("org.wallentines:midnightcore-spigot-adapter:${project.version}")
        }

        "v${version.name}CompileOnly"(project(":common"))
        "v${version.name}CompileOnly"(project(":server"))
        "v${version.name}CompileOnly"(project(":spigot:adapter"))
    }
}

class VersionInfo(val name: String, val version: String, val javaVersion: Int)
