# Scaffolding

Scaffolding is a library for Minestom that allows you to load and place schematics.
> This library is still under heavy development and has too many bugs to count. For your own safety, this should not be
> used in a production environment.
> This library's API is likely to change a lot as this project works towards a stable 1.0.0 release.

## Usage

```java
// Load a schematic from File.
public void method1(){
        Schematic schematic=Scaffolding.fromFile(new File("schematics/my_schematic.schematic"));
        }

public void method2(){
        Schematic schematic=new SpongeSchematic();
        schematic.read(new FileInputStream(new File("schematics/my_schematic.schematic")));
        }
```

```java
// Place a schematic at a location.
Instance instance=player.getInstance();
        Pos position=player.getPosition();
        schematic.build(instance,position).thenRun(()->player.sendMessage("Schematic placed!"));
```

```java
// Write a schematic (Soon:tm:)
Region region=new Region(new Pos(0,0,0),new Pos(10,10,10));
        Schematic schematic=new SpongeSchematic();
        schematic.write(new FileOutputStream("schematics/my_schematic.schematic"),region);
```

## Dependency

### Gradle (Kotlin)

Add Scaffolding as a dependency in your `build.gradle.kts` file.

```kt
repositories {
    maven("https://repo.hypera.dev/snapshots/")
}

dependencies {
    implementation("dev.hypera:Scaffolding:VERSION")
}
```

### Gradle (Groovy)

Add Scaffolding as a dependency in your `build.gradle` file.

```groovy
repositories {
    maven { url 'https://repo.hypera.dev/snapshots/' }
}

dependencies {
    implementation("dev.hypera:Scaffolding:VERSION")
}
```

### Maven

Add Scaffolding as a dependency in your `pom.xml` file.

```xml
<repositories>
    <repository>
        <id>hypera-snapshots</id>
        <url>https://repo.hypera.dev/snapshots/</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>dev.hypera</groupId>
        <artifactId>Scaffolding</artifactId>
        <version>VERSION</version>
    </dependency>
</dependencies>
```
