plugins {
    id("build.library")
    id("build.multiversion")
}

dependencies {

    compileOnlyApi(libs.jetbrains.annotations)

    api(libs.midnight.cfg)
    api(libs.midnight.cfg.sql)
    api(libs.midnight.cfg.binary)
    api(libs.midnight.cfg.json)
    api(libs.midnight.cfg.nbt)
    api(libs.midnight.lib)
    api(libs.smi.api)
    api(libs.smi.base)

    api(libs.netty.codec)
    api(libs.netty.buffer)

    api(libs.slf4j.api)

    testImplementation(libs.midnight.cfg)
    testImplementation(libs.midnight.cfg.json)
    testImplementation(libs.midnight.cfg.nbt)
    testImplementation(libs.midnight.lib)

}