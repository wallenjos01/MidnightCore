plugins {
    id("midnightcore-build")
    id("midnightcore-publish")
    alias(libs.plugins.loom)
    //alias(libs.plugins.shadow)
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
        dependsOn(remapJar)
    }
    jar {
        archiveClassifier.set("dev")
    }
    remapJar {
        archiveClassifier.set("")
        val id = project.properties["id"]
        archiveBaseName.set("${id}-${project.name}")
        inputFile.set(jar.get().archiveFile)
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
    minecraft("com.mojang:minecraft:1.20.4")
    mappings(loom.officialMojangMappings())

    // Fabric Loader
    modImplementation("net.fabricmc:fabric-loader:0.15.0")

    // Fabric API
    val apiModules = listOf(
            "fabric-api-base",
            "fabric-lifecycle-events-v1",
            "fabric-networking-api-v1"
    )
    for(mod in apiModules) {
        modApi(include(fabricApi.module(mod, "0.91.1+1.20.4"))!!)
    }

    // Included Library Dependencies
    modApi(libs.midnight.cfg)
    modApi(libs.midnight.cfg.json)
    modApi(libs.midnight.cfg.binary)
    modApi(libs.midnight.cfg.gson)
    modApi(libs.midnight.cfg.nbt)
    modApi(libs.midnight.lib)
    modApi(libs.zstd.jni)

    include(libs.midnight.cfg)
    include(libs.midnight.cfg.json)
    include(libs.midnight.cfg.binary)
    include(libs.midnight.cfg.gson)
    include(libs.midnight.cfg.nbt)
    include(libs.midnight.lib)
    include(libs.zstd.jni)

    modApi(include("org.wallentines:fabric-events:0.3.0-SNAPSHOT")!!)
    modApi(include("me.lucko:fabric-permissions-api:0.3-SNAPSHOT")!!)
}


tasks.withType<ProcessResources>() {
    filesMatching("fabric.mod.json") {
        expand(mapOf(
                Pair("version", project.version as String),
                Pair("id", project.properties["id"] as String)
        ))
    }
}
