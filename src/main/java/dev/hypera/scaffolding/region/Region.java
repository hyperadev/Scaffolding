package dev.hypera.scaffolding.region;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

public record Region(@NotNull Instance instance, @NotNull Point lower, @NotNull Point upper) {

    public int sizeX() {
        return (upper.blockX() - lower.blockX()) + 1;
    }

    public int sizeY() {
        return (upper.blockY() - lower.blockY()) + 1;
    }

    public int sizeZ() {
        return (upper.blockZ() - lower.blockZ()) + 1;
    }

    public record Block(Pos position, short stateId) {}

}
