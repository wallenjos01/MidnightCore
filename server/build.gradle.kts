plugins {
    id("midnightcore-build")
    id("midnightcore-multi-version")
    id("midnightcore-publish")
}

dependencies {

    api(project(":common"))
    api(libs.midnight.cfg.binary)
}