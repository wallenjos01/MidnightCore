plugins {
    id("build.library")
    id("build.multiversion")
}


repositories {
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://libraries.minecraft.net/")
}

dependencies {

    // MidnightCore
    api(project(":common"))
    api(project(":server"))

    java8CompileOnly("org.spigotmc:spigot-api:1.16.5-R0.1-SNAPSHOT")
    java17CompileOnly("org.spigotmc:spigot-api:1.20.4-R0.1-SNAPSHOT")
    java21CompileOnly("org.spigotmc:spigot-api:1.20.5-R0.1-SNAPSHOT")

    compileOnly(libs.midnight.cfg)
    compileOnly(libs.jetbrains.annotations)

}
