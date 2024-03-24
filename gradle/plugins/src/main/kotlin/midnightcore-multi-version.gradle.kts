plugins {
    id("java")
    id("java-library")
    id("org.wallentines.gradle-multi-version")
    id("org.wallentines.gradle-patch")
}

multiVersion {
    defaultVersion(17)
    additionalVersions(8)
}

patch {
    patchSet("java8", sourceSets["main"], sourceSets["main"].java, multiVersion.getCompileTask(8))
}