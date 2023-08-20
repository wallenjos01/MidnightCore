plugins {
    id("midnightcore-build")
}

repositories {
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {

    implementation("com.velocitypowered:velocity-api:3.2.0-SNAPSHOT")

    api(project(":common"))
    api(project(":proxy"))

}