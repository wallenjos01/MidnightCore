pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://plugins.gradle.org/m2/")
        maven("https://maven.fabricmc.net/")
        maven("https://maven.wallentines.org/plugins")
    }

    includeBuild("gradle/plugins")
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

// Building Spigot requires many Spigot versions from 1.8 to 1.21.1 to be available in the local repository. To do this,
// the user will need to build all required Spigot versions using the Spigot BuildTools. (https://www.spigotmc.org/wiki/buildtools/)
// In the case where building Spigot jars is not necessary, this flag can be disabled.
// See spigot/build.gradle.kts for the list of versions.
val buildSpigot = false

rootProject.name = "midnightcore"

include("common")

include("server")
include("client")
include("proxy")

include("fabric")

if(buildSpigot) {
    include("spigot")
    include("spigot:adapter")
}

include("velocity")
