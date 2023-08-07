pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://maven.fabricmc.net/")
    }
}


rootProject.name = "midnightcore"

include("common")

include("server")
include("client")
include("proxy")

include("fabric")
include("velocity")
