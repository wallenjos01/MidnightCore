plugins {
    id("midnightcore-build")
}

dependencies {

    api(project(":common"))

    implementation(libs.midnight.cfg.binary)

}