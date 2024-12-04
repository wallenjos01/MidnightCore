import buildlogic.Utils;
import org.gradle.api.component.AdhocComponentWithVariants

plugins {
    id("build.common")
    id("com.gradleup.shadow")
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
    archiveBaseName.set(Utils.getArchiveName(project, rootProject))
    archiveClassifier.set("")
}

tasks.jar {
    archiveClassifier.set("partial")
}

val javaComponent = components["java"] as AdhocComponentWithVariants
javaComponent.withVariantsFromConfiguration(configurations["shadowRuntimeElements"]) {
    skip()
}
