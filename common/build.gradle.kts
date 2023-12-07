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

repositories {
    mavenCentral()
    maven("https://maven.wallentines.org/")
    mavenLocal()
}

dependencies {

    api(libs.midnight.cfg)
    api(libs.midnight.cfg.json)
    api(libs.midnight.lib)

    api(libs.netty.codec)
    api(libs.netty.buffer)

    api(libs.slf4j.api)

    testImplementation(libs.midnight.cfg)
    testImplementation(libs.midnight.cfg.json)
    testImplementation(libs.midnight.lib)

}