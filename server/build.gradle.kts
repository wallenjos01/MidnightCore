plugins {
    id("mod-build")
    id("mod-multi-version")
    id("mod-publish")
}

dependencies {

    api(project(":common"))
    api(libs.midnight.cfg.binary)

    compileOnly(libs.jetbrains.annotations)
}