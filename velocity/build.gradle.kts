plugins {
    id("midnightcore-build")
    id("midnightcore-publish")
    alias(libs.plugins.shadow)
}

tasks {
    build {
        dependsOn(shadowJar)
    }
    shadowJar {
        configurations = listOf(project.configurations.shadow.get())
    }
}

repositories {
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {

    implementation("com.velocitypowered:velocity-api:3.2.0-SNAPSHOT")

    api(project(":common"))
    api(project(":proxy"))

    shadow(project(":common").setTransitive(false))
    shadow(project(":proxy").setTransitive(false))

    shadow(libs.midnight.cfg) { isTransitive = false }
    shadow(libs.midnight.cfg.json) { isTransitive = false }
    shadow(libs.midnight.cfg.binary) { isTransitive = false }
    shadow(libs.midnight.cfg.gson) { isTransitive = false }
    shadow(libs.midnight.lib) { isTransitive = false }
    shadow(libs.zstd.jni)

}

tasks.withType<ProcessResources>() {
    filesMatching("velocity-plugin.json") {
        expand(mapOf(
                Pair("version", project.version as String),
                Pair("id", project.properties["id"] as String)
        ))
    }
}
