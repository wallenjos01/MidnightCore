pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://maven.fabricmc.net/")
        mavenLocal()
    }

    includeBuild("gradle/plugins")
}


rootProject.name = "midnightcore"

include("common")

include("server")
include("client")
include("proxy")

include("fabric")
include("spigot")
include("velocity")
