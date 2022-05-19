plugins {
    java
    `maven-publish`
}

description = "Schematic library for Minestom"
java.sourceCompatibility = JavaVersion.VERSION_17

var minestomVersion = "f774cc3b0f"

dependencies {
    compileOnly("com.github.Minestom:Minestom:${minestomVersion}")
    testImplementation("com.github.Minestom:Minestom:${minestomVersion}")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.2")
}

publishing {
    publications.create<MavenPublication>("maven") {
        from(components["java"])
    }
}