import org.gradle.jvm.tasks.Jar

plugins {
    id("midnightcore-build")
    id("midnightcore-publish")
    alias(libs.plugins.multiversion)
    alias(libs.plugins.patch)
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


dependencies {

    // MidnightCore
    api(project(":common"))
    api(project(":server"))

    compileOnly("org.spigotmc:spigot-api:1.20.1-R0.1-SNAPSHOT")
    compileOnly(libs.midnight.cfg)
    compileOnly(libs.jetbrains.annotations)

}
