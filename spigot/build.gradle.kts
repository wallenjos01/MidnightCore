import org.gradle.jvm.tasks.Jar

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

val allCompileOnly = configurations.create("allCompileOnly")
configurations.compileOnly.get().extendsFrom(allCompileOnly)


tasks.jar {
    archiveClassifier.set("1.17-1.20")
}

tasks.named<Jar>("java8Jar") {
    val id = project.properties["id"]
    archiveBaseName = "${id}-${project.name}"
    archiveClassifier.set("1.8-1.16")
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
    shadow(libs.midnight.cfg)
    shadow(libs.midnight.cfg.json)
    shadow(libs.midnight.cfg.binary)
    shadow(libs.midnight.lib)

    compileOnly("org.spigotmc:spigot-api:1.20.1-R0.1-SNAPSHOT")

    // Dependencies which apply to adapters too
    allCompileOnly(libs.midnight.cfg)
    allCompileOnly(libs.jetbrains.annotations)
    allCompileOnly(project(":common"))
    allCompileOnly(project(":server"))
    allCompileOnly(project(":spigot:adapter"))
}


fun setupVersion(version: VersionInfo, javaVersion: Int) {
    val set = sourceSets.create("v${version.name}")
    tasks.named<JavaCompile>("compileV${version.name}Java") {
        javaCompiler.set(project.javaToolchains.compilerFor {
            languageVersion.set(JavaLanguageVersion.of(javaVersion))
        })
    }

    configurations.getByName("v${version.name}CompileOnly").extendsFrom(allCompileOnly)

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
        } else {
            compileOnly(set.output)
        }
        "v${version.name}CompileOnly"("org.spigotmc:spigot-api:${version.version}-R0.1-SNAPSHOT")
        "v${version.name}CompileOnly"("org.spigotmc:spigot:${version.version}-R0.1-SNAPSHOT")
    }
}

class VersionInfo(val name: String, val version: String)

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
        VersionInfo("1_20_R1","1.20.1")
)
for(version in modernVersions) {
    setupVersion(version, 17)
}