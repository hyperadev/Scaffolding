# Scaffolding

Scaffolding is a library for Minestom that allows you to load and place schematics.
> This library is very early in development and has too many bugs to count. For your own safety, you should not use it in a production environment.

> Also expect the API to change quite a bit in the future as this project works towards a 1.0.0 release.

## Getting Started

### Repository

As with Minestom, Scaffolding uses JitPack to distrubute releases.

#### Gradle (Kotlin)
Add JitPack as a repository to your ```build.gradle.kts``` file.
```kotlin
repositories {
    maven(url = "https://jitpack.io")
}
```

#### Gradle (Groovy)
Add JitPack as a repository to your ```build.gradle``` file.
```groovy
repositories {
    maven { url 'https://jitpack.io' }
}
```

#### Maven
Add JitPack as a repository to your ```pom.xml``` file.
```xml
<repositories>
    <repository>
        <id>jitpack</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```

### Dependency

Keep an eye out for new releases on [Jitpack](https://jitpack.io/#CrystalGamesMc/scaffolding).

#### Gradle (Kotlin)
Add Scaffolding as a dependency in your ```build.gradle.kts``` file.
```kts
dependencies {
    implementation("com.github.CrystalGamesMc:scaffolding:Tag")
}
```

#### Gradle (Groovy)
Add Scaffolding as a dependency in your ```build.gradle``` file.
```groovy
dependencies {
    implementation 'com.github.CrystalGamesMc:scaffolding:Tag'
}
```

#### Maven
Add Scaffolding as a dependency in your ```pom.xml``` file.
```xml
<dependencies>
    <dependency>
        <groupId>com.github.CrystalGamesMc</groupId>
        <artifactId>scaffolding</artifactId>
        <version>Tag</version>
    </dependency>
</dependencies>
```

## Usage
Read
```java
File file = new File("schematics/your_schematic.schematic");
Schematic schematic = Scaffolding.fromFileSync(file);
```

Build
```java
Schematic schematic = ...;
schematic.build(instance, new Pos(0, 64, 0)).join();
```
