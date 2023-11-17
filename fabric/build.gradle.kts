plugins {
    id("midnightcore-build")
    id("midnightcore-publish")
    alias(libs.plugins.loom)
    alias(libs.plugins.shadow)
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


tasks {
    build {
        dependsOn(shadowJar)
    }
    shadowJar {
        archiveClassifier.set("dev")
        configurations = listOf(project.configurations.shadow.get())
    }
    remapJar {
        val id = project.properties["id"]
        archiveBaseName.set("${id}-${project.name}")
        dependsOn(shadowJar)
        inputFile.set(shadowJar.get().archiveFile)
    }
}


repositories {
    maven(url = "https://s01.oss.sonatype.org/content/repositories/snapshots/") {
        name = "sonatype-oss-snapshots1"
        mavenContent { snapshotsOnly() }
    }
    maven("https://oss.sonatype.org/content/repositories/snapshots")
}


dependencies {

    // MidnightCore
    api(project(":common"))
    api(project(":server"))
    api(project(":client"))

    include(project(":common").setTransitive(false))
    include(project(":server").setTransitive(false))
    include(project(":client").setTransitive(false))

    // Minecraft
    minecraft("com.mojang:minecraft:1.20.2")
    mappings(loom.officialMojangMappings())

    // Fabric Loader
    modImplementation("net.fabricmc:fabric-loader:0.14.21")

    // Fabric API
    val apiModules = listOf(
            "fabric-api-base",
            "fabric-command-api-v2",
            "fabric-lifecycle-events-v1",
            "fabric-networking-api-v1"
    )
    for(mod in apiModules) {
        modApi(include(fabricApi.module(mod, "0.89.1+1.20.2"))!!)
    }

    // Shadowed Library Dependencies
    shadow(libs.midnight.cfg) { isTransitive = false }
    shadow(libs.midnight.cfg.json) { isTransitive = false }
    shadow(libs.midnight.cfg.binary) { isTransitive = false }
    shadow(libs.midnight.cfg.gson) { isTransitive = false }
    shadow(libs.midnight.lib) { isTransitive = false }
    shadow(libs.zstd.jni)

    // Included Mod Dependencies
    modApi(include("org.wallentines:fabric-events:0.3.0-SNAPSHOT")!!)
    modApi(include("me.lucko:fabric-permissions-api:0.2-SNAPSHOT")!!)
}


tasks.withType<ProcessResources>() {
    filesMatching("fabric.mod.json") {
        expand(mapOf(
                Pair("version", project.version as String),
                Pair("id", project.properties["id"] as String)
        ))
    }
}