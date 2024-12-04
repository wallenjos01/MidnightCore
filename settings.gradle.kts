
// Building Spigot requires many Spigot versions from 1.8 to 1.21.1 to be available in the local repository. To do this,
// the user will need to build all required Spigot versions using the Spigot BuildTools. (https://www.spigotmc.org/wiki/buildtools/)
// In the case where building Spigot jars is not necessary, this flag can be disabled.
// See spigot/build.gradle.kts for the list of versions.
val buildSpigot = true

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
