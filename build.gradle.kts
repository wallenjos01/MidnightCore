import java.net.URI

plugins {
    id("java")
    id("java-library")
    id("maven-publish")
}

group = "org.wallentines"
version = "0.1.0-SNAPSHOT"


subprojects.forEach { sp ->

    sp.apply(plugin="java")
    sp.apply(plugin="java-library")
    sp.apply(plugin="maven-publish")

    sp.java.withSourcesJar()

    sp.repositories {
        mavenCentral()
        maven("https://maven.wallentines.org/")
        mavenLocal()
    }

    sp.dependencies {

        api("org.wallentines:midnightcfg:1.0.1")
        api("org.wallentines:midnightlib:1.2.0")

        implementation("org.slf4j:slf4j-api:2.0.7")
        implementation("io.netty:netty-buffer:4.1.94.Final")
        implementation("org.jetbrains:annotations:24.0.1")

        testImplementation(platform(libs.junit.bom))
        testImplementation(libs.junit.jupiter)
    }

    sp.tasks.test {
        useJUnitPlatform()
    }

    sp.publishing {
        publications {
            create<MavenPublication>("maven") {
                groupId = group as String
                version = version as String
                from(components["java"])
            }
        }

        repositories {
            if(project.hasProperty("pubUrl")) {
                maven {
                    name = "pub"
                    url = URI.create(project.properties["pubUrl"] as String)
                    credentials(PasswordCredentials::class.java)
                }
            }
        }
    }

}