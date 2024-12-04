plugins {
    id("build.library")
    id("build.multiversion")
}


dependencies {

    api(project(":common"))
    api(libs.midnight.cfg.binary)

    compileOnly(libs.jetbrains.annotations)
}