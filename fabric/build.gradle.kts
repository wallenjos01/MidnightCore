plugins {

    id("fabric-loom") version "1.3.8"

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

repositories {
    maven(url = "https://s01.oss.sonatype.org/content/repositories/snapshots/") {
        name = "sonatype-oss-snapshots1"
        mavenContent { snapshotsOnly() }
    }
    mavenCentral()
}

dependencies {

    api(project(":api"))
    api(project(":server"))
    api(project(":client"))

    minecraft("com.mojang:minecraft:1.20.1")
    mappings(loom.officialMojangMappings())

    modImplementation("net.fabricmc:fabric-loader:0.14.21")

    val apiModules = listOf("fabric-command-api-v2", "fabric-lifecycle-events-v1")

    for(mod in apiModules) {
        modImplementation(include(fabricApi.module(mod, "0.85.0+1.20.1"))!!)
    }

}