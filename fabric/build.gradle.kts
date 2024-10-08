import build.plugin.Common

plugins {
    id("mod-build")
    id("mod-publish")
    id("mod-shadow")
    id("mod-fabric")
}

Common.setupResources(project, rootProject, "fabric.mod.json")

dependencies {

    // MidnightCore
    api(project(":common"))
    api(project(":server"))
    api(project(":client"))

    shadow(project(":common").setTransitive(false))
    shadow(project(":server").setTransitive(false))
    shadow(project(":client").setTransitive(false))

    // Minecraft
    minecraft("com.mojang:minecraft:1.21.1")
    mappings(loom.officialMojangMappings())

    // Fabric Loader
    modImplementation("net.fabricmc:fabric-loader:0.16.5")

    // Fabric API
    val apiModules = listOf(
            "fabric-api-base",
            "fabric-lifecycle-events-v1",
            "fabric-networking-api-v1",
            "fabric-command-api-v2"
    )
    for(mod in apiModules) {
        modApi(include(fabricApi.module(mod, "0.104.0+1.21.1"))!!)
    }

    include(modApi("me.lucko:fabric-permissions-api:0.3.1") {
        isTransitive = false
    })

    // Shadowed Library Dependencies
    modApi(libs.midnight.cfg)
    modApi(libs.midnight.cfg.sql)
    modApi(libs.midnight.cfg.json)
    modApi(libs.midnight.cfg.binary)
    modApi(libs.midnight.cfg.gson)
    modApi(libs.midnight.cfg.nbt)
    modApi(libs.midnight.lib)
    modApi(libs.zstd.jni)

    shadow(libs.midnight.cfg) { isTransitive = false }
    shadow(libs.midnight.cfg.sql) { isTransitive = false }
    shadow(libs.midnight.cfg.json) { isTransitive = false }
    shadow(libs.midnight.cfg.binary) { isTransitive = false }
    shadow(libs.midnight.cfg.gson) { isTransitive = false }
    shadow(libs.midnight.cfg.nbt) { isTransitive = false }
    shadow(libs.midnight.lib) { isTransitive = false }
    shadow(libs.zstd.jni) { isTransitive = false }

}