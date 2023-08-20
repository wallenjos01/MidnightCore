plugins {
    id("midnightcore-build")
    alias(libs.plugins.multiversion)
    alias(libs.plugins.patch)
}

multiVersion {
    defaultVersion(17)
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

}