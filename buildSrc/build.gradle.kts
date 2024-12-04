plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
    maven {
        name = "Fabric"
        url = uri("https://maven.fabricmc.net/")
    }
    maven("https://maven.wallentines.org/plugins")
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.5.31")
    implementation("com.gradleup.shadow:shadow-gradle-plugin:8.3.5")
    implementation("net.fabricmc:fabric-loom:1.8-SNAPSHOT")
    implementation("org.wallentines:gradle-multi-version:0.3.0")
    implementation("org.wallentines:gradle-patch:0.2.0")
}
