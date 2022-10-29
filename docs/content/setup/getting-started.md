# Getting Started

This guide describes how to setup Scaffolding as a dependency for your project.

::: info
Scaffolding is intended to be used as a library for the Minestom framework. You can learn how to setup
Minestom [here](https://wiki.minestom.net/setup/dependencies/).
:::

## Repository

As with Minestom, Scaffolding uses JitPack to distribute releases.

### Gradle (Kotlin)

Add JitPack as a repository to your ```build.gradle.kts``` file.

```kotlin{2}
repositories {
    maven(url = "https://jitpack.io")
}
```

### Gradle (Groovy)

Add JitPack as a repository to your ```build.gradle``` file.

```groovy{2}
repositories {
    maven { url 'https://jitpack.io' }
}
```

### Maven

Add JitPack as a repository to your ```pom.xml``` file.

```xml{2-5}
<repositories>
    <repository>
        <id>jitpack</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```

## Dependency

Keep an eye out for new releases on [Jitpack](https://jitpack.io/#CrystalGamesMc/scaffolding).

### Gradle (Kotlin)

Add Scaffolding as a dependency in your ```build.gradle.kts``` file.

```kts{2}
dependencies {
    implementation("com.github.CrystalGamesMc:scaffolding:Tag")
}
```

### Gradle (Groovy)

Add Scaffolding as a dependency in your ```build.gradle``` file.

```groovy{2}
dependencies {
    implementation 'com.github.CrystalGamesMc:scaffolding:Tag'
}
```

### Maven

Add Scaffolding as a dependency in your ```pom.xml``` file.

```xml{2-6}
<dependencies>
    <dependency>
        <groupId>com.github.CrystalGamesMc</groupId>
        <artifactId>scaffolding</artifactId>
        <version>Tag</version>
    </dependency>
</dependencies>
```