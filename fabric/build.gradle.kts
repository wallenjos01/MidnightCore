import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("midnightcore-build")
    id("midnightcore-publish")
    id("midnightcore-shadow")
    alias(libs.plugins.loom)
}

loom {
    runs {
        getByName("client") {
            runDir = "run/client"
            ideConfigGenerated(false)
            client()
        }
        getByName("server") {
            runDir = "run/server"
            ideConfigGenerated(false)
            server()
        }
    }
}


val finalShadow = tasks.register<ShadowJar>("finalShadow") {
    dependsOn(tasks.remapJar)
    val id = rootProject.name
    archiveClassifier.set("")
    archiveBaseName.set("${id}-${project.name}")
    configurations = listOf(project.configurations["shadow"])
    from(tasks.remapJar)
}


tasks {
    build {
        dependsOn(finalShadow)
    }
    jar {
        archiveClassifier.set("dev")
    }
    remapJar {
        archiveClassifier.set("remap")
        inputFile.set(jar.get().archiveFile)
    }
}


dependencies {

    // MidnightCore
    api(project(":common"))
    api(project(":server"))
    api(project(":client"))

    shadow(project(":common").setTransitive(false))
    shadow(project(":server").setTransitive(false))
    shadow(project(":client").setTransitive(false))

    // Minecraft
    minecraft("com.mojang:minecraft:1.20.5")
    mappings(loom.officialMojangMappings())

    // Fabric Loader
    modImplementation("net.fabricmc:fabric-loader:0.15.10")

    // Fabric API
    val apiModules = listOf(
            "fabric-api-base",
            "fabric-lifecycle-events-v1",
            "fabric-networking-api-v1",
            "fabric-command-api-v2"
    )
    for(mod in apiModules) {
        modApi(include(fabricApi.module(mod, "0.97.0+1.20.5"))!!)
    }

    // Included Library Dependencies
    modApi(libs.midnight.cfg)
    modApi(libs.midnight.cfg.sql)
    modApi(libs.midnight.cfg.json)
    modApi(libs.midnight.cfg.binary)
    modApi(libs.midnight.cfg.gson)
    modApi(libs.midnight.cfg.nbt)
    modApi(libs.midnight.lib)
    modApi(libs.zstd.jni)

    include(libs.midnight.cfg)
    include(libs.midnight.cfg.sql)
    include(libs.midnight.cfg.json)
    include(libs.midnight.cfg.binary)
    include(libs.midnight.cfg.gson)
    include(libs.midnight.cfg.nbt)
    include(libs.midnight.lib)
    include(libs.zstd.jni)

    include(modApi("me.lucko:fabric-permissions-api:0.3.1") {
        isTransitive = false
    })
}


tasks.withType<ProcessResources>() {
    filesMatching("fabric.mod.json") {
        expand(mapOf(
                Pair("version", project.version as String),
                Pair("id", rootProject.name)
        ))
    }
}
