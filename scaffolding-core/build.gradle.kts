plugins {
    java
    `maven-publish`
}

description = "Schematic library for Minestom"
java.sourceCompatibility = JavaVersion.VERSION_17

var minestomVersion = "bfa2dbd3f7"

dependencies {
    compileOnly("com.github.Minestom:Minestom:${minestomVersion}")
    testImplementation("com.github.Minestom:Minestom:${minestomVersion}")

    testImplementation("me.alexpanov:free-port-finder:1.1.1")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.2")
}

publishing {
    publications.create<MavenPublication>("maven") {
        from(components["java"])
    }
}