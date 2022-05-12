package net.crystalgames.scaffolding.schematic;

import net.crystalgames.scaffolding.region.Region;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.batch.AbsoluteBlockBatch;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.generator.GenerationUnit;
import net.minestom.server.instance.generator.UnitModifier;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

@SuppressWarnings("unused")
public class Schematic implements Block.Setter {

    private short[] blocks;

    private int width, height, length;
    private int offsetX, offsetY, offsetZ;
    private int area;

    private boolean locked;

    public Schematic() {
        reset();
    }

    /**
     * Resets this schematic to its original state. This is useful if you want to reuse a schematic multiple times.
     * <br><br>
     * The schematic will be locked after this method is called.
     */
    public void reset() {
        width = height = length = 0;
        offsetX = offsetY = offsetZ = 0;

        blocks = null; // Does this actually have to be nulled out? This looks a bit too much like a deconstructor.
        setLocked(true);
    }

    /**
     * Copies blocks from the given region into this schematic.
     *
     * @param region the {@link Region} to copy from
     * @return a {@link CompletableFuture<Schematic>} that will complete once all blocks have been copied
     */
    public @NotNull CompletableFuture<Void> copy(@NotNull final Region region) {
        reset();

        return ScaffoldingUtils.loadChunks(region).thenRun(() -> {
            final Instance instance = region.instance();
            final Pos lower = region.lower();
            final int width = region.width();
            final int height = region.height();
            final int length = region.length();

            setSize(width, height, length);

            for (int x = 0; x < width; ++x) {
                for (int y = 0; y < height; ++y) {
                    for (int z = 0; z < length; ++z) {
                        final int blockX = lower.blockX() + x;
                        final int blockY = lower.blockY() + y;
                        final int blockZ = lower.blockZ() + z;

                        final Block block = region.instance().getBlock(blockX, blockY, blockZ, Block.Getter.Condition.TYPE);

                        if (block != null) blocks[getIndex(x, y, z)] = block.stateId();
                    }
                }
            }

            locked = false;
        });
    }

    public void setSize(int sizeX, int sizeY, int sizeZ) {
        this.width = sizeX;
        this.height = sizeY;
        this.length = sizeZ;

        area = sizeX * sizeY * sizeZ;
        blocks = new short[area];
    }

    public int getIndex(int x, int y, int z) {
        return y * width * length + z * width + x;
    }

    /**
     * Builds this schematic in the given {@link Instance} at the given {@link Pos}.
     *
     * @param instance the {@link Instance} to build this schematic in
     * @param position the {@link Pos} to build this schematic at (note: the schematics offset will be applied to this position to get the lower corner)
     * @return a {@link CompletableFuture<Schematic>} that will complete once the schematic has been built
     */
    public @NotNull CompletableFuture<Region> build(@NotNull final Instance instance, @NotNull final Pos position) {
        return build(instance, position, false, false, false);
    }

    /**
     * Builds this schematic in the given {@link Instance} at the given {@link Pos}. The schematic can be flipped along the X, Y, or Z axis using the {@code flipX}, {@code flipY}, and {@code flipZ} parameters.
     *
     * @param instance the {@link Instance} to build this schematic in
     * @param position the {@link Pos} to build this schematic at (note: the schematics offset will be applied to this position to get the lower corner)
     * @param flipX    whether to flip the schematic along the X axis
     * @param flipY    whether to flip the schematic along the Y axis
     * @param flipZ    whether to flip the schematic along the Z axis
     * @return a {@link CompletableFuture<Schematic>} that will complete once the schematic has been built
     */
    public @NotNull CompletableFuture<Region> build(@NotNull final Instance instance, @NotNull final Pos position, final boolean flipX, final boolean flipY, final boolean flipZ) {
        if (locked) throw new IllegalStateException("Cannot build a locked schematic.");

        final Region region = getContainingRegion(instance, position);
        if (!isPlaceable(region))
            throw new IllegalStateException("Cannot build schematic at this position since blocks would go outside of world boundaries. " + position);

        return ScaffoldingUtils.loadChunks(region).thenApply((ignored) -> {
            final AbsoluteBlockBatch blockBatch = new AbsoluteBlockBatch();

            apply(region.lower(), flipX, flipY, flipZ, blockBatch);

            final CompletableFuture<Region> future = new CompletableFuture<>();
            blockBatch.apply(instance, () -> future.complete(region));
            future.join();

            return region;
        });
    }

    /**
     * @param instance the {@link Instance} to check
     * @param position the {@link Pos} to check
     * @return the {@link Region} that this schematic would take up if placed at the given position
     */
    public @NotNull Region getContainingRegion(@NotNull final Instance instance, @NotNull final Pos position) {
        return new Region(instance, position.add(offsetX, offsetY, offsetZ), position.add(offsetX + width, offsetY + height, offsetZ + length));
    }

    @ApiStatus.Internal
    private boolean isPlaceable(@NotNull final Region region) {
        final Instance instance = region.instance();

        final boolean isAboveWorldBounds = region.upper().blockY() >= instance.getDimensionType().getMaxY();
        final boolean isBelowWorldBounds = region.lower().blockY() < instance.getDimensionType().getMinY();

        return !(isAboveWorldBounds || isBelowWorldBounds);
    }

    /**
     * Applies this schematic to the given {@link Block.Setter} at the given {@link Pos}. The schematic can be flipped along the X, Y, or Z axis using the {@code flipX}, {@code flipY}, and {@code flipZ} parameters.
     *
     * @param position the {@link Pos} to apply this schematic at within the given {@link Block.Setter}. Acts like an offset.
     * @param flipX    whether to flip the schematic along the X axis
     * @param flipY    whether to flip the schematic along the Y axis
     * @param flipZ    whether to flip the schematic along the Z axis
     * @param setter   the {@link Block.Setter} to apply this schematic to
     */
    public void apply(@NotNull final Pos position, final boolean flipX, final boolean flipY, final boolean flipZ, @NotNull final Block.Setter setter) {
        final Pos lower = position.add(offsetX, offsetY, offsetZ);

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                for (int z = 0; z < length; z++) {
                    // Will the JVM optimize out the ternary operator? I hope so.
                    final int indexX = flipX ? width - x - 1 : x;
                    final int indexY = flipY ? height - y - 1 : y;
                    final int indexZ = flipZ ? length - z - 1 : z;

                    final int blockX = lower.blockX() + x;
                    final int blockY = lower.blockY() + y;
                    final int blockZ = lower.blockZ() + z;

                    final Block block = getBlock(indexX, indexY, indexZ);

                    if (block != null) setter.setBlock(blockX, blockY, blockZ, block);
                }
            }
        }
    }

    @Nullable
    public Block getBlock(int indexX, int indexY, int indexZ) {
        short stateId = getStateId(indexX, indexY, indexZ);
        return Block.fromStateId(stateId);
    }

    public short getStateId(int x, int y, int z) {
        return blocks[getIndex(x, y, z)];
    }

    public void fork(@NotNull GenerationUnit unit, @NotNull Pos position, boolean flipX, boolean flipY, boolean flipZ) {
        if (locked) throw new IllegalStateException("Cannot fork a locked schematic.");

        final Pos start = position.sub(offsetX, offsetY, offsetZ);
        final Pos end = start.add(width, height, length);

        UnitModifier forkModifier = unit.fork(start, end).modifier();

        apply(position, flipX, flipY, flipZ, forkModifier);
    }

    public void setBlock(@NotNull Pos position, @NotNull Block block) {
        setBlock(position.blockX(), position.blockY(), position.blockZ(), block);
    }

    public void setBlock(int x, int y, int z, @NotNull Block block) {
        setBlock(x, y, z, block.stateId());
    }

    public void setBlock(int x, int y, int z, short stateId) {
        blocks[getIndex(x, y, z)] = stateId;
    }

    /**
     * @param position the {@link Pos} to place the block at
     * @param stateId  the state id of the block to place.
     */
    public void setBlock(@NotNull Pos position, short stateId) {
        setBlock(position.blockX(), position.blockY(), position.blockZ(), stateId);
    }

    /**
     * @return the width of this schematic
     */
    public int getWidth() {
        return width;
    }

    /**
     * @return the height of this schematic
     */
    public int getHeight() {
        return height;
    }

    /**
     * @return the length of the schematic
     */
    public int getLength() {
        return length;
    }

    /**
     * Gets the offset in the x-axis used when {@link #build(Instance, Pos)} or {@link #apply(Pos, boolean, boolean, boolean, Block.Setter)} are called.
     *
     * @return the x offset
     */
    public int getOffsetX() {
        return offsetX;
    }

    /**
     * Gets the offset in the y-axis used when {@link #build(Instance, Pos)} or {@link #apply(Pos, boolean, boolean, boolean, Block.Setter)} are called.
     *
     * @return the y offset
     */
    public int getOffsetY() {
        return offsetY;
    }

    /**
     * Gets the offset in the z-axis used when {@link #build(Instance, Pos)} or {@link #apply(Pos, boolean, boolean, boolean, Block.Setter)} are called.
     *
     * @return the z offset
     */
    public int getOffsetZ() {
        return offsetZ;
    }

    /**
     * Gets the area of this schematic. ({@code width} * {@code height} * {@code length})
     *
     * @return the area of this schematic
     */
    public int getArea() {
        return area;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    /**
     * @param offset the {@link Point} to offset this schematic by
     */
    public void setOffset(@NotNull final Point offset) {
        setOffset(offset.blockX(), offset.blockY(), offset.blockZ());
    }

    public void setOffset(int x, int y, int z) {
        offsetX = x;
        offsetY = y;
        offsetZ = z;
    }

    /**
     * Applies the schematic to the given block setter.
     *
     * @param setter the block setter
     */
    @Deprecated
    public void apply(@NotNull Block.Setter setter) {
        apply(Pos.ZERO, false, false, false, setter);
    }

    /**
     * @param instance the {@link Instance} to check
     * @param position the {@link Pos} to check
     * @return {@code true} if the given position is within the bounds of the given instance, {@code false} otherwise
     */
    public boolean isPlaceable(@NotNull final Instance instance, @NotNull final Pos position) {
        return isPlaceable(getContainingRegion(instance, position));
    }
}
