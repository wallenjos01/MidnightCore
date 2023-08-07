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
    accessWidenerPath = File("src/main/resources/midnightcore.accesswidener")
}

repositories {
    maven(url = "https://s01.oss.sonatype.org/content/repositories/snapshots/") {
        name = "sonatype-oss-snapshots1"
        mavenContent { snapshotsOnly() }
    }
    mavenCentral()
}

dependencies {

    api(project(":common"))
    api(project(":server"))
    api(project(":client"))

    minecraft("com.mojang:minecraft:1.20.1")
    mappings(loom.officialMojangMappings())

    modImplementation("net.fabricmc:fabric-loader:0.14.21")

    val apiModules = listOf(
            "fabric-command-api-v2",
            "fabric-lifecycle-events-v1",
            "fabric-networking-api-v1"
    )

    for(mod in apiModules) {
        modApi(include(fabricApi.module(mod, "0.86.1+1.20.1"))!!)
    }

}