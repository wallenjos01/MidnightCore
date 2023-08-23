plugins {
    id("midnightcore-build")
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
        minimize {
            exclude("org.wallentines.*")
        }
    }
    remapJar {
        dependsOn(shadowJar)
        inputFile.set(shadowJar.get().archiveFile)

        val id = project.properties["id"]
        archiveBaseName.set("${id}-${project.name}")
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

    shadow(project(":common").setTransitive(false))
    shadow(project(":server").setTransitive(false))
    shadow(project(":client").setTransitive(false))

    // Minecraft
    minecraft("com.mojang:minecraft:1.20.1")
    mappings(loom.officialMojangMappings())

    // Fabric Loader
    modImplementation("net.fabricmc:fabric-loader:0.14.21")

    // Fabric API
    val apiModules = listOf(
            "fabric-command-api-v2",
            "fabric-lifecycle-events-v1",
            "fabric-networking-api-v1"
    )
    for(mod in apiModules) {
        modApi(include(fabricApi.module(mod, "0.86.1+1.20.1"))!!)
    }

    // Included Library Dependencies
    include(libs.midnight.cfg)
    include(libs.midnight.cfg.json)
    include(libs.midnight.cfg.binary)
    include(libs.midnight.cfg.gson)
    include(libs.midnight.lib)

    // Included Mod Dependencies
    modApi(include("org.wallentines:fabric-events:0.1.0-SNAPSHOT")!!)
    modApi(include("me.lucko:fabric-permissions-api:0.2-SNAPSHOT")!!)
}
