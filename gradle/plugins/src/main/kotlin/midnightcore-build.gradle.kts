plugins {
    id("java")
    id("java-library")
    id("maven-publish")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
    withSourcesJar()
}

repositories {
    mavenCentral()
    maven("https://maven.wallentines.org/")
    mavenLocal()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.1")
}

tasks.withType<Jar>() {
    val id = project.properties["id"]
    archiveBaseName = "${id}-${project.name}"
}

tasks.test {
    useJUnitPlatform()
    workingDir("run/test")
}

publishing {
    publications.create<MavenPublication>("maven") {
        artifactId = if(rootProject == project) {
            project.name
        } else {
            rootProject.name + "-" + project.name
        }
        from(components["java"])
    }

    if (project.hasProperty("pubUrl")) {
        repositories.maven(project.properties["pubUrl"] as String) {
            name = "pub"
            credentials(PasswordCredentials::class.java)
        }
    }
}