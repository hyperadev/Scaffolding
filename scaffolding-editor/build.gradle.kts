plugins {
    id("java")
}

repositories {
    mavenLocal()
    maven {
        url = uri("https://jitpack.io")
    }

    maven {
        url = uri("https://repo.maven.apache.org/maven2/")
    }
}

var minestomVersion = "f774cc3b0f"

dependencies {
    implementation(project(":"))

    implementation("me.alexpanov:free-port-finder:1.1.1")
    implementation("com.github.Minestom:Minestom:${minestomVersion}")
}

group = "net.crystalgames"
version = "0.1.3-SNAPSHOT"
description = "Scaffolding"
java.sourceCompatibility = JavaVersion.VERSION_17