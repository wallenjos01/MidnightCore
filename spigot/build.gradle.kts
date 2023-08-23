plugins {
    id("midnightcore-build")
    alias(libs.plugins.multiversion)
    alias(libs.plugins.patch)
    alias(libs.plugins.shadow)
}

multiVersion {
    defaultVersion(17)
    additionalVersions(8)
}

patch {
    patchSet("java8", sourceSets["main"], sourceSets["main"].java, multiVersion.getCompileTask(8))
}

dependencies {

    // MidnightCore
    api(project(":common"))
    api(project(":server"))

    shadow(project(":common").setTransitive(false))
    shadow(project(":server").setTransitive(false))

    // Shadowed Library Dependencies
    shadow(libs.midnight.cfg)
    shadow(libs.midnight.cfg.json)
    shadow(libs.midnight.cfg.binary)
    shadow(libs.midnight.lib)
}
