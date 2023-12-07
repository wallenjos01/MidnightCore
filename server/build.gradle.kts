plugins {
    id("midnightcore-build")
    id("midnightcore-publish")
    alias(libs.plugins.multiversion)
    alias(libs.plugins.patch)
}

multiVersion {
    defaultVersion(17)
    additionalVersions(8)
}

patch {
    patchSet("java8", sourceSets["main"], sourceSets["main"].java, multiVersion.getCompileTask(8))
}

dependencies {

    api(project(":common"))
    api(libs.midnight.cfg.binary)
}