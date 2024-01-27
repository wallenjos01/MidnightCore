import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.jvm.tasks.Jar
import org.jetbrains.kotlin.parsing.parseBoolean

plugins {
    id("midnightcore-build")
    id("midnightcore-publish")
    alias(libs.plugins.multiversion)
    alias(libs.plugins.patch)
    alias(libs.plugins.shadow)
}


// MultiVersion
multiVersion {
    defaultVersion(17)
    additionalVersions(8)
}

patch {
    patchSet("java8", sourceSets["main"], sourceSets["main"].java, multiVersion.getCompileTask(8))
}

repositories {
    maven("https://libraries.minecraft.net/")
}

configurations.create("shadow17").extendsFrom(configurations.shadow.get())
configurations.create("shadow8") {
    extendsFrom(configurations.shadow.get())
    attributes.attribute(TargetJvmVersion.TARGET_JVM_VERSION_ATTRIBUTE, 8)
}

tasks.named<Jar>("java8Jar") {
    val id = project.properties["id"]
    archiveBaseName = "${id}-${project.name}"
}

tasks.shadowJar {
    archiveClassifier.set("1.17-1.20")
    configurations = listOf(project.configurations["shadow17"])
}

val java8ShadowJar = tasks.register<ShadowJar>("java8ShadowJar") {
    archiveClassifier.set("1.8-1.16")
    configurations = listOf(project.configurations["shadow8"])

    dependsOn(tasks.processResources)

    from(sourceSets["java8"].output)
    from(tasks.processResources.get().destinationDir)
}


tasks.build {
    dependsOn(tasks.shadowJar)
    dependsOn(java8ShadowJar)
}

// Versions compiled against Java 8
val legacyVersions = listOf(
        VersionInfo("1_8_R1","1.8"),
        VersionInfo("1_8_R2","1.8.3"),
        VersionInfo("1_8_R3","1.8.8"),
        VersionInfo("1_9_R1","1.9"),
        VersionInfo("1_9_R2","1.9.4"),
        VersionInfo("1_10_R1","1.10.2"),
        VersionInfo("1_11_R1","1.11.2"),
        VersionInfo("1_12_R1","1.12.2"),
        VersionInfo("1_13_R1","1.13"),
        VersionInfo("1_13_R2","1.13.1"),
        VersionInfo("1_13_R2v2","1.13.2"),
        VersionInfo("1_14_R1","1.14.4"),
        VersionInfo("1_15_R1","1.15.2"),
        VersionInfo("1_16_R1","1.16.1"),
        VersionInfo("1_16_R2","1.16.2"),
        VersionInfo("1_16_R3","1.16.4")
)
for(version in legacyVersions) {
    setupVersion(version, 8)
}

// Versions compiled against Java 17
val modernVersions = listOf(
        VersionInfo("1_17_R1","1.17.1"),
        VersionInfo("1_18_R1","1.18"),
        VersionInfo("1_18_R2","1.18.2"),
        VersionInfo("1_19_R1","1.19"),
        VersionInfo("1_19_R1v2","1.19.2"),
        VersionInfo("1_19_R2","1.19.3"),
        VersionInfo("1_19_R3","1.19.4"),
        VersionInfo("1_20_R1","1.20.1"),
        VersionInfo("1_20_R2","1.20.2"),
        VersionInfo("1_20_R3","1.20.4"),
)
for(version in modernVersions) {
    setupVersion(version, 17)
}


dependencies {

    // MidnightCore
    api(project(":common"))
    api(project(":server"))
    api(project(":spigot:adapter"))

    shadow(project(":common").setTransitive(false))
    shadow(project(":server").setTransitive(false))
    shadow(project(":spigot:adapter").setTransitive(false))

    // Shadowed Library Dependencies
    shadow(libs.midnight.cfg) { isTransitive = false }
    shadow(libs.midnight.cfg.json) { isTransitive = false }
    shadow(libs.midnight.cfg.gson) { isTransitive = false }
    shadow(libs.midnight.cfg.binary) { isTransitive = false }
    shadow(libs.midnight.lib) { isTransitive = false }
    shadow(libs.zstd.jni)


    "shadow8"(libs.slf4j.simple)

    compileOnly("org.spigotmc:spigot-api:1.20.2-R0.1-SNAPSHOT")
    testImplementation("org.yaml:snakeyaml:2.2")
}

tasks.withType<ProcessResources>() {
    filesMatching("plugin.yml") {
        expand(mapOf(
                Pair("version", project.version as String),
                Pair("id", project.properties["id"] as String)
        ))
    }
}


fun setupVersion(version: VersionInfo, javaVersion: Int) {
    val set = sourceSets.create("v${version.name}")
    tasks.named<JavaCompile>("compileV${version.name}Java") {
        javaCompiler.set(project.javaToolchains.compilerFor {
            languageVersion.set(JavaLanguageVersion.of(javaVersion))
        })
    }

    if(javaVersion == 8) {
        tasks.named<Jar>("java8Jar") {
            from(set.output)
        }
    } else {
        tasks.jar {
            from(set.output)
        }
    }

    dependencies {
        if(javaVersion == 8) {
            "java8CompileOnly"(set.output)
            "shadow8"(set.output)
        } else {
            compileOnly(set.output)
            "shadow17"(set.output)
        }
        "v${version.name}Implementation"("org.spigotmc:spigot-api:${version.version}-R0.1-SNAPSHOT")
        "v${version.name}Implementation"("org.spigotmc:spigot:${version.version}-R0.1-SNAPSHOT")

        "v${version.name}CompileOnly"(libs.jetbrains.annotations)
        "v${version.name}Implementation"(libs.midnight.cfg)
        "v${version.name}Implementation"(libs.midnight.cfg.gson)

        // For whatever reason, intellisense cannot find project dependencies on java 8 sources sets. Gradle still can,
        // and compilation works fine, but intelliJ complains about it. To work around this, those dependencies can be
        // pulled from repositories instead. In the case where this becomes a problem, set spigot_intellisense_workaround
        // to false in gradle.properties.
        if(javaVersion == 8 && parseBoolean(project.properties["spigot_intellisense_workaround"] as String)) {
            "v${version.name}Implementation"("org.wallentines:midnightcore-common:${project.version}")
            "v${version.name}Implementation"("org.wallentines:midnightcore-server:${project.version}")
            "v${version.name}Implementation"("org.wallentines:midnightcore-spigot-adapter:${project.version}")
        }

        "v${version.name}Implementation"(project(":common"))
        "v${version.name}Implementation"(project(":server"))
        "v${version.name}Implementation"(project(":spigot:adapter"))
    }
}

class VersionInfo(val name: String, val version: String)
