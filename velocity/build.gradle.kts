repositories {
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {

    implementation("com.velocitypowered:velocity-api:3.2.0-SNAPSHOT")

    api(project(":proxy"))
    api(project(":api"))

}