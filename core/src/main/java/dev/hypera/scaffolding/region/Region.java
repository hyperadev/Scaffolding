/*
 * Scaffolding - Schematic library for Minestom
 *  Copyright (c) 2022-latest The Scaffolding Library Authors
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the “Software”), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in
 *  all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  THE SOFTWARE.
 */
package dev.hypera.scaffolding.region;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;

/**
 * Represents a rectangle 3 dimensional region of blocks within an {@link Instance}.
 */
public final class Region {

    private final @NotNull Instance instance;
    private final @NotNull Point lower;
    private final @NotNull Point upper;

    /**
     * Constructs a new region. The region is defined by the two provided positions. As long as the two positions are opposite of each other in the region, {@code lower} and {@code upper} will be calculated automatically.
     *
     * @param instance The instance that the region is in.
     * @param p1       The first point of the region.
     * @param p2       The second point of the region.
     */
    public Region(@NotNull Instance instance, @NotNull Point p1, @NotNull Point p2) {
        this.instance = Objects.requireNonNull(instance);
        this.lower = calcPoint(p1, p2, Math::min);
        this.upper = calcPoint(p1, p2, Math::max);
    }


    /**
     * Force loads all {@link Chunk}s this region.
     *
     * @return a {@link CompletableFuture<Region>} that will complete once all chunks in the region have been loaded. The future will give the region as the result so that you can chain it.
     */
    public @NotNull CompletableFuture<Region> loadChunks() {
        int lengthX = getUpperChunkX() - getLowerChunkX() + 1;
        int lengthZ = getUpperChunkZ() - getLowerChunkZ() + 1;

        CompletableFuture<?>[] futures = new CompletableFuture[lengthX * lengthZ];
        int index = 0;

        for (int x = getLowerChunkX(); x <= getUpperChunkX(); ++x) {
            for (int z = getLowerChunkZ(); z <= getUpperChunkZ(); ++z) {
                futures[index++] = instance.loadChunk(x, z);
            }
        }

        return CompletableFuture.allOf(futures).thenApply(v -> this);
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
     * @return the upper {@link Point} of this region.
     */
    @Contract(pure = true)
    public @NotNull Point getUpper() {
        return upper;
    }

    /**
     * @return the lower {@link Point} of this region.
     */
    @Contract(pure = true)
    public @NotNull Point getLower() {
        return lower;
    }


    private @NotNull Point calcPoint(@NotNull Point p1, @NotNull Point p2, BiFunction<Integer, Integer, Integer> operation) {
        return new Vec(operation.apply(p1.blockX(), p2.blockX()), operation.apply(p1.blockY(), p2.blockY()), operation.apply(p1.blockZ(), p2.blockZ()));
    }


    @Override
    public int hashCode() {
        return Objects.hash(instance, lower, upper);
    }

    @Override
    public boolean equals(Object obj) {
        return obj == this || (
                obj instanceof Region region
                        && Objects.equals(this.instance, region.getInstance())
                        && Objects.equals(this.lower, region.getLower())
                        && Objects.equals(this.upper, region.getUpper())
        );
    }

    @Override
    public String toString() {
        return "Region[" + "instance=" + instance + ", " + "lower=" + lower + ", " + "upper=" + upper + ']';
    }

}
