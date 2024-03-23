plugins {
    id("java")
    id("java-library")
    id("maven-publish")
}

publishing {
    publications.create<MavenPublication>("maven") {
        if(rootProject == project) {
            artifactId = project.name
        } else {
            var name = project.name
            var currentParent = project.parent
            while(currentParent != rootProject) {
                name = currentParent!!.name + "-" + name
                currentParent = currentParent.parent
            }
            artifactId = rootProject.name + "-" + name
        }
        from(components["java"])
    }

    if (project.hasProperty("pubUrl")) {

        var url: String = project.properties["pubUrl"] as String
        url += if(GradleVersion.version(version as String).isSnapshot) {
            "snapshots"
        } else {
            "releases"
        }

        repositories.maven(url) {
            name = "pub"
            credentials(PasswordCredentials::class.java)
        }
    }
}