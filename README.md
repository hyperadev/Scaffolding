# Scaffolding
Scaffolding is a library for Minestom that allows you to load and place schematics.
> This library is very early in development and has too many bugs to count. For your own safety, you should not use it in a production environment.

## Usage
```java
// Load a schematic from File.
public void method1() {
    Schematic schematic = Scaffolding.fromFile(new File("schematics/my_schematic.schematic"));
}

public void method2() {
    Schematic schematic = new SpongeSchematic();
    schematic.read(new FileInputStream(new File("schematics/my_schematic.schematic")));
}
```
```java
// Place a schematic at a location.
Instance instance = player.getInstance();
Pos position = player.getPosition();
schematic.build(instance, position).thenRun(() -> player.sendMessage("Schematic placed!"));
```
```java
// Write a schematic (SOONtm)
Region region = new Region(new Pos(0, 0, 0), new Pos(10, 10, 10));
Schematic schematic = new SpongeSchematic();
schematic.write(new FileOutputStream("schematics/my_schematic.schematic"), region);
```