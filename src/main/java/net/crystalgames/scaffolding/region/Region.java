package net.crystalgames.scaffolding.region;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@SuppressWarnings("unused")
public final class Region {

    private final Instance instance;
    private final Pos lower;
    private final Pos upper;

    /**
     * Constructs a new region. The region is defined by the two provided positions. As long as the two positions are opposite of each other in the region, {@code lower} and {@code upper} will be calculated automatically.
     *
     * @param instance The instance that the region is in.
     * @param p1 The first point of the region.
     * @param p2 The second point of the region.
     */
    public Region(@NotNull final Instance instance, @NotNull final Pos p1, @NotNull final Pos p2) {
        this.instance = instance;
        this.lower = min(p1, p2);
        this.upper = max(p1, p2);
    }

    private @NotNull Pos min(@NotNull Pos p1, @NotNull Pos p2) {
        final int x = Math.min(p1.blockX(), p2.blockX());
        final int y = Math.min(p1.blockY(), p2.blockY());
        final int z = Math.min(p1.blockZ(), p2.blockZ());

        return new Pos(x, y, z);
    }

    private Pos max(Pos p1, Pos p2) {
        final int x = Math.max(p1.blockX(), p2.blockX());
        final int y = Math.max(p1.blockY(), p2.blockY());
        final int z = Math.max(p1.blockZ(), p2.blockZ());

        return new Pos(x, y, z);
    }

    /**
     * @return the width of the region.
     */
    public int width() {
        return (upper.blockX() - lower.blockX()) + 1;
    }

    /**
     * @return the height of the region.
     */
    public int height() {
        return (upper.blockY() - lower.blockY()) + 1;
    }

    /**
     * @return the length of the region.
     */
    public int length() {
        return (upper.blockZ() - lower.blockZ()) + 1;
    }

    public int chunkSizeX() {
        return upperChunkX() - lowerChunkX() + 1;
    }

    public int upperChunkX() {
        return upper.blockX() >> 4;
    }

    public int lowerChunkX() {
        return lower.blockX() >> 4;
    }

    public int chunkSizeZ() {
        return upperChunkZ() - lowerChunkZ() + 1;
    }

    public int upperChunkZ() {
        return upper.blockZ() >> 4;
    }

    public int lowerChunkZ() {
        return lower.blockZ() >> 4;
    }

    public Instance instance() {
        return instance;
    }

    public Pos lower() {
        return lower;
    }

    public Pos upper() {
        return upper;
    }

    @Override
    public int hashCode() {
        return Objects.hash(instance, lower, upper);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (Region) obj;
        return Objects.equals(this.instance, that.instance) &&
                Objects.equals(this.lower, that.lower) &&
                Objects.equals(this.upper, that.upper);
    }

    @Override
    public String toString() {
        return "Region[" +
                "instance=" + instance + ", " +
                "lower=" + lower + ", " +
                "upper=" + upper + ']';
    }
}
