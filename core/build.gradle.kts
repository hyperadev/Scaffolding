plugins {
    `java-library`
    `maven-publish`
}

description = "Schematic library for Minestom"
java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    compileOnlyApi("com.github.Minestom:Minestom:7be96b7679")
    compileOnlyApi("space.vectrix.flare:flare-fastutil:2.0.1")

    testImplementation("com.github.Minestom:Minestom:7be96b7679")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.2")
}

publishing {
    publications.create<MavenPublication>("maven") {
        from(components["java"])
    }
}