import build.plugin.Common

plugins {
    id("java")
    id("java-library")
    id("com.github.johnrengelman.shadow")
}

tasks {
    build {
        dependsOn(shadowJar)
    }
    shadowJar {
        configurations = listOf(project.configurations.shadow.get())
    }
}

tasks.shadowJar {
    archiveBaseName.set(Common.getArchiveName(project, rootProject))
    archiveClassifier.set("")
}

tasks.jar {
    archiveClassifier.set("partial")
}

val javaComponent = components["java"] as AdhocComponentWithVariants
javaComponent.withVariantsFromConfiguration(configurations["shadowRuntimeElements"]) {
    skip()
}
