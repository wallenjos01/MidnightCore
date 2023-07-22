pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://maven.fabricmc.net/")
    }
}


rootProject.name = "midnightcore"

include("api")

include("server")
include("client")

include("fabric")
