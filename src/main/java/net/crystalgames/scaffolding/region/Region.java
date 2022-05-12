package net.crystalgames.scaffolding.region;

import net.crystalgames.scaffolding.schematic.ScaffoldingUtils;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;

/**
 * Represents a rectangle 3 dimensional region of blocks withing an {@link Instance}.
 */
@SuppressWarnings("unused")
public final class Region {

    private final Instance instance;
    private final Pos lower;
    private final Pos upper;

    /**
     * Constructs a new region. The region is defined by the two provided positions. As long as the two positions are opposite of each other in the region, {@code lower} and {@code upper} will be calculated automatically.
     *
     * @param instance The instance that the region is in.
     * @param p1       The first point of the region.
     * @param p2       The second point of the region.
     */
    public Region(@NotNull final Instance instance, @NotNull final Pos p1, @NotNull final Pos p2) {
        this.instance = Objects.requireNonNull(instance);
        this.lower = calcPos(p1, p2, Math::min);
        this.upper = calcPos(p1, p2, Math::max);
    }

    /**
     * Force loads all {@link Chunk}s in this region.
     *
     * @return a {@link CompletableFuture<Region>} that will complete once all chunks in the region have been loaded. The future will give the region as the result so that you can chain it.
     */
    public CompletableFuture<Region> loadChunksWithinRegion() {
        return ScaffoldingUtils.loadChunks(this);
    }

    /**
     * @return the width of this region.
     */
    public int getWidth() {
        return (upper.blockX() - lower.blockX()) + 1;
    }

    /**
     * @return the height of this region.
     */
    public int getHeight() {
        return (upper.blockY() - lower.blockY()) + 1;
    }

    /**
     * @return the length of this region.
     */
    public int getLength() {
        return (upper.blockZ() - lower.blockZ()) + 1;
    }

    /**
     * @return the x coordinate of the upper {@link Chunk} of this region.
     */
    @Contract(pure = true)
    public int getUpperChunkX() {
        return upper.blockX() >> 4;
    }

    /**
     * @return the z coordinate of the upper {@link Chunk} of this region.
     */
    @Contract(pure = true)
    public int getUpperChunkZ() {
        return upper.blockZ() >> 4;
    }

    /**
     * @return the x coordinate of the lower {@link Chunk} of this region.
     */
    @Contract(pure = true)
    public int getLowerChunkX() {
        return lower.blockX() >> 4;
    }

    /**
     * @return the z coordinate of the lower {@link Chunk} of this region.
     */
    @Contract(pure = true)
    public int getLowerChunkZ() {
        return lower.blockZ() >> 4;
    }

    /**
     * @return the number of {@link Chunk}s along the x coordinate of this region.
     */
    @Contract(pure = true)
    public int getChunkSizeX() {
        return getUpperChunkX() - getLowerChunkX() + 1;
    }

    /**
     * @return the number of {@link Chunk}s along the z coordinate of this region.
     */
    @Contract(pure = true)
    public int getChunkSizeZ() {
        return getUpperChunkZ() - getLowerChunkZ() + 1;
    }

    /**
     * @return the instance that this region is in
     */
    @Contract(pure = true)
    public @NotNull Instance getInstance() {
        return instance;
    }

    /**
     * @return the upper {@link Pos} of this region.
     */
    @Contract(pure = true)
    public @NotNull Pos getUpper() {
        return upper;
    }

    /**
     * @return the lower {@link Pos} of this region.
     */
    @Contract(pure = true)
    public @NotNull Pos getLower() {
        return lower;
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

    private @NotNull Pos calcPos(@NotNull Pos p1, @NotNull Pos p2, BiFunction<Integer, Integer, Integer> operation) {
        final int x = operation.apply(p1.blockX(), p2.blockX());
        final int y = operation.apply(p1.blockY(), p2.blockY());
        final int z = operation.apply(p1.blockZ(), p2.blockZ());

        return new Pos(x, y, z);
    }
}
