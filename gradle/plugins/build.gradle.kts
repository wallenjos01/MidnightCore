plugins {
    `kotlin-dsl`
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.5.21")
    implementation("org.wallentines:gradle-multi-version:0.3.0-SNAPSHOT")
    implementation("org.wallentines:gradle-patch:0.2.0")
    implementation("com.github.johnrengelman:shadow:8.1.1")
    implementation("net.fabricmc:fabric-loom:1.6-SNAPSHOT")
}