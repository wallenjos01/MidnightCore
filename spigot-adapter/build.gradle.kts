import org.gradle.jvm.tasks.Jar

plugins {
    id("midnightcore-build")
    alias(libs.plugins.multiversion)
}


// MultiVersion
multiVersion {
    defaultVersion(17)
    additionalVersions(8)
}

repositories {
    maven("https://libraries.minecraft.net/")
}

class VersionInfo(val name: String, val version: String, val dependencies: List<String>) {
}

// Versions compiled against Java 8
val legacyVersions = listOf(
        VersionInfo("1_8_R1","1.8", listOf("com.mojang:authlib:1.5.21")),
        VersionInfo("1_8_R2","1.8.3",listOf("com.mojang:authlib:1.5.21")),
        VersionInfo("1_8_R3","1.8.8",listOf("com.mojang:authlib:1.5.21")),
        VersionInfo("1_9_R1","1.9",listOf("com.mojang:authlib:1.5.22")),
        VersionInfo("1_9_R2","1.9.4",listOf("com.mojang:authlib:1.5.22")),
        VersionInfo("1_10_R1","1.10.2",listOf("com.mojang:authlib:1.5.22")),
        VersionInfo("1_11_R1","1.11.2",listOf("com.mojang:authlib:1.5.24")),
        VersionInfo("1_12_R1","1.12.2",listOf("com.mojang:authlib:1.5.25")),
        VersionInfo("1_13_R1","1.13",listOf("com.mojang:authlib:1.5.25")),
        VersionInfo("1_13_R2_1131","1.13.1",listOf("com.mojang:authlib:1.5.25")),
        VersionInfo("1_13_R2","1.13.2",listOf("com.mojang:authlib:1.5.25")),
        VersionInfo("1_14_R1","1.14.4",listOf("com.mojang:authlib:1.5.25")),
        VersionInfo("1_15_R1","1.15.2",listOf("com.mojang:authlib:1.5.25")),
        VersionInfo("1_16_R1","1.16.1",listOf("com.mojang:authlib:1.6.25")),
        VersionInfo("1_16_R2","1.16.2",listOf("com.mojang:authlib:1.6.25")),
        VersionInfo("1_16_R3","1.16.4",listOf("com.mojang:authlib:2.1.28"))
)

// Versions compiled against Java 17
val modernVersions = listOf(
        VersionInfo("1_17_R1","1.17.1",listOf("com.mojang:authlib:2.3.31")),
        VersionInfo("1_18_R1","1.18",listOf("com.mojang:authlib:3.2.38")),
        VersionInfo("1_18_R2","1.18.2",listOf("com.mojang:authlib:3.3.39")),
        VersionInfo("1_19_R1_1190","1.19",listOf("com.mojang:authlib:3.5.41")),
        VersionInfo("1_19_R1","1.19.2",listOf("com.mojang:authlib:3.11.49")),
        VersionInfo("1_19_R2","1.19.3",listOf("com.mojang:authlib:3.16.29")),
        VersionInfo("1_19_R3","1.19.4",listOf("com.mojang:authlib:3.18.38")),
        VersionInfo("1_20_R1","1.20.1",listOf("com.mojang:authlib:4.0.43", "com.mojang:datafixerupper:6.0.8"))
)

val allCompileOnly = configurations.create("allCompileOnly")

configurations.compileOnly.get().extendsFrom(allCompileOnly)


fun setupVersion(version: VersionInfo, javaVersion: Int) {
    sourceSets.create("v${version.name}")
    tasks.named<JavaCompile>("compileV${version.name}Java") {
        javaCompiler.set(project.javaToolchains.compilerFor {
            languageVersion.set(JavaLanguageVersion.of(javaVersion))
        })
    }

    configurations.getByName("v${version.name}CompileOnly").extendsFrom(allCompileOnly)

    dependencies {
        if(javaVersion == 8) {
            "v${version.name}CompileOnly"(sourceSets["java8"].output)
            "v${version.name}CompileOnly"(sourceSets["main"].output)
        } else {
            "v${version.name}CompileOnly"(sourceSets["main"].output)
        }
        "v${version.name}CompileOnly"("org.spigotmc:spigot-api:${version.version}-R0.1-SNAPSHOT")
        "v${version.name}CompileOnly"("org.spigotmc:spigot:${version.version}-R0.1-SNAPSHOT") { isTransitive = false }

        for(dep in version.dependencies) {
            "v${version.name}CompileOnly"(dep)
        }
    }
}

for(version in legacyVersions) {
    setupVersion(version, 8)
}

for(version in modernVersions) {
    setupVersion(version, 17)
}


dependencies {

    // MidnightCore
    api(project(":common"))
    api(project(":server"))

    allCompileOnly(libs.midnight.cfg)
    allCompileOnly(libs.jetbrains.annotations)
    allCompileOnly(project(":common"))
    allCompileOnly(project(":server"))
    compileOnly("org.spigotmc:spigot-api:1.20.1-R0.1-SNAPSHOT")

}

tasks.jar {
    for(version in modernVersions) {
        from(sourceSets["v${version.name}"].output)
    }
}
tasks.named<Jar>("java8Jar") {
    for(version in legacyVersions) {
        from(sourceSets["v${version.name}"].output)
    }
}
