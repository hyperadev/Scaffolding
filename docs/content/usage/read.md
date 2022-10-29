# Loading Schematics

Scaffolding offers many ways to load schematics.

## Automatically parsing type

```java
File file = new File("schematics/your_schematic.schematic");
Schematic schematic = Scaffolding.fromFileSync(file);
```
