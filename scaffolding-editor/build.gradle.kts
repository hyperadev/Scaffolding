plugins {
    id("java")
}

description = "Scaffolding Editor"
java.sourceCompatibility = JavaVersion.VERSION_17

var minestomVersion = "f774cc3b0f"

dependencies {
    implementation(project(":scaffolding-core"))

    implementation("me.alexpanov:free-port-finder:1.1.1")
    implementation("com.github.Minestom:Minestom:${minestomVersion}")
}