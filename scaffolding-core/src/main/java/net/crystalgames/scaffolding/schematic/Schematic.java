package net.crystalgames.scaffolding.schematic;

import net.crystalgames.scaffolding.region.Region;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.batch.AbsoluteBlockBatch;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.generator.GenerationUnit;
import net.minestom.server.instance.generator.UnitModifier;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

/**
 * A parsed schematic.
 */
@SuppressWarnings("unused")
public class Schematic implements Block.Setter {

    private short[] blocks;

    private int width, height, length;
    private int offsetX, offsetY, offsetZ;
    private int area;

    private boolean locked;

    /**
     * Constructs a new schematic. The schematic will be locked and have an area of 0.
     */
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
    public @NotNull CompletableFuture<Schematic> copy(@NotNull final Region region) {
        reset();

        return ScaffoldingUtils.loadChunks(region).thenApply((v) -> {
            final Instance instance = region.getInstance();
            final Point lower = region.getLower();
            final int width = region.getWidth();
            final int height = region.getHeight();
            final int length = region.getLength();

            setSize(width, height, length);

            for (int x = 0; x < width; ++x) {
                for (int y = 0; y < height; ++y) {
                    for (int z = 0; z < length; ++z) {
                        final int blockX = lower.blockX() + x;
                        final int blockY = lower.blockY() + y;
                        final int blockZ = lower.blockZ() + z;

                        final Block block = region.getInstance().getBlock(blockX, blockY, blockZ, Block.Getter.Condition.TYPE);

                        if (block != null) blocks[getBlockIndex(x, y, z)] = block.stateId();
                    }
                }
            }

            locked = false;
            return this;
        });
    }

    /**
     * Sets the size of this schematic. {@code area} will be updated accordingly.
     *
     * @param width  new width
     * @param height new height
     * @param length new length
     */
    public void setSize(int width, int height, int length) {
        this.width = width;
        this.height = height;
        this.length = length;

        area = width * height * length;
        blocks = new short[area];
    }

    /**
     * Gets the index of a block in this schematic if blocks were stored in a 1-dimensional array.
     *
     * @param x block x coordinate
     * @param y block y coordinate
     * @param z block z coordinate
     * @return the index of the block at the given coordinates
     */
    public int getBlockIndex(int x, int y, int z) {
        return y * width * length + z * width + x;
    }

    /**
     * Builds this schematic in the given {@link Instance} at the given {@link Point}.
     *
     * @param instance the {@link Instance} to build this schematic in
     * @param position the {@link Point} to build this schematic at (note: the schematics offset will be applied to this position to get the lower corner)
     * @return a {@link CompletableFuture<Schematic>} that will complete once the schematic has been built
     */
    public @NotNull CompletableFuture<Region> build(@NotNull final Instance instance, @NotNull final Point position) {
        return build(instance, position, false, false, false);
    }

    /**
     * Builds this schematic in the given {@link Instance} at the given {@link Point}. The schematic can be flipped along the X, Y, or Z axis using the {@code flipX}, {@code flipY}, and {@code flipZ} parameters.
     *
     * @param instance the {@link Instance} to build this schematic in
     * @param position the {@link Point} to build this schematic at (note: the schematics offset will be applied to this position to get the lower corner)
     * @param flipX    whether to flip the schematic along the X axis
     * @param flipY    whether to flip the schematic along the Y axis
     * @param flipZ    whether to flip the schematic along the Z axis
     * @return a {@link CompletableFuture<Schematic>} that will complete once the schematic has been built
     */
    public @NotNull CompletableFuture<Region> build(@NotNull final Instance instance, @NotNull final Point position, final boolean flipX, final boolean flipY, final boolean flipZ) {
        if (locked) throw new IllegalStateException("Cannot build a locked schematic.");

        final Region region = getContainingRegion(instance, position);
        if (!isPlaceable(region))
            throw new IllegalStateException("Cannot build schematic at this position since blocks would go outside of world boundaries. " + position);

        return ScaffoldingUtils.loadChunks(region).thenApplyAsync((ignored) -> {
            final AbsoluteBlockBatch blockBatch = new AbsoluteBlockBatch();

            apply(region.getLower(), flipX, flipY, flipZ, blockBatch);
            System.out.println("Building schematic.");

            final CompletableFuture<Region> future = new CompletableFuture<>();
            blockBatch.apply(instance, () -> future.complete(null));
            return future.join();
        });
    }

    /**
     * @param instance the {@link Instance} to check
     * @param position the {@link Point} to check
     * @return the {@link Region} that this schematic would take up if placed at the given position
     */
    public @NotNull Region getContainingRegion(@NotNull final Instance instance, @NotNull final Point position) {
        return new Region(instance, position.add(offsetX, offsetY, offsetZ), position.add(offsetX + width, offsetY + height, offsetZ + length));
    }

    @ApiStatus.Internal
    private boolean isPlaceable(@NotNull final Region region) {
        final Instance instance = region.getInstance();

        final boolean isAboveWorldBounds = region.getUpper().blockY() >= instance.getDimensionType().getMaxY();
        final boolean isBelowWorldBounds = region.getLower().blockY() < instance.getDimensionType().getMinY();

        return !(isAboveWorldBounds || isBelowWorldBounds);
    }

    /**
     * Applies this schematic to the given {@link Block.Setter} at the given {@link Point}. The schematic can be flipped along the X, Y, or Z axis using the {@code flipX}, {@code flipY}, and {@code flipZ} parameters.
     *
     * @param position the {@link Point} to apply this schematic at within the given {@link Block.Setter}. Acts like an offset.
     * @param flipX    whether to flip the schematic along the X axis
     * @param flipY    whether to flip the schematic along the Y axis
     * @param flipZ    whether to flip the schematic along the Z axis
     * @param setter   the {@link Block.Setter} to apply this schematic to
     */
    public void apply(@NotNull final Point position, final boolean flipX, final boolean flipY, final boolean flipZ, @NotNull final Block.Setter setter) {
        if(locked) throw new IllegalStateException("Cannot apply a locked schematic.");

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                for (int z = 0; z < length; z++) {
                    // Will the JVM optimize out the ternary operator? I hope so.
                    final int relativeBlockX = flipX ? width - x - 1 : x;
                    final int relativeBlockY = flipY ? height - y - 1 : y;
                    final int relativeBlockZ = flipZ ? length - z - 1 : z;

                    final int absoluteX = position.blockX() + x;
                    final int absoluteY = position.blockY() + y;
                    final int absoluteZ = position.blockZ() + z;

                    final Block block = getBlock(relativeBlockX, relativeBlockY, relativeBlockZ);

                    if (block != null) setter.setBlock(absoluteX, absoluteY, absoluteZ, block);
                }
            }
        }
    }

    /**
     * @param x block x coordinate
     * @param y block y coordinate
     * @param z block z coordinate
     * @return the block at the given coordinates
     */
    @Nullable
    public Block getBlock(final int x, final int y, final int z) {
        short stateId = getStateId(x, y, z);
        return Block.fromStateId(stateId);
    }

    /**
     * @param x block x coordinate
     * @param y block y coordinate
     * @param z block z coordinate
     * @return the state ID at the given coordinates
     */
    public short getStateId(int x, int y, int z) {
        return blocks[getBlockIndex(x, y, z)];
    }

    /**
     * @param unit     the {@link GenerationUnit} to fork
     * @param position the {@link Point} to place the schematic at. Offsets will be applied to this position to get the lower corner.
     * @param flipX    whether to flip the schematic along the X axis
     * @param flipY    whether to flip the schematic along the Y axis
     * @param flipZ    whether to flip the schematic along the Z axis
     */
    public void fork(@NotNull GenerationUnit unit, @NotNull Point position, boolean flipX, boolean flipY, boolean flipZ) {
        if (locked) throw new IllegalStateException("Cannot fork a locked schematic.");

        final Point start = position.sub(offsetX, offsetY, offsetZ);
        final Point end = start.add(width, height, length);

        UnitModifier forkModifier = unit.fork(start, end).modifier();

        apply(position, flipX, flipY, flipZ, forkModifier);
    }

    /**
     * @param position the {@link Point} of the block to set
     * @param block    the {@link Block} to set
     */
    public void setBlock(@NotNull Point position, @NotNull Block block) {
        setBlock(position.blockX(), position.blockY(), position.blockZ(), block);
    }

    public void setBlock(int x, int y, int z, @NotNull Block block) {
        setBlock(x, y, z, block.stateId());
    }

    /**
     * @param x       the X coordinate
     * @param y       the Y coordinate
     * @param z       the Z coordinate
     * @param stateId the state ID
     */
    public void setBlock(int x, int y, int z, short stateId) {
        blocks[getBlockIndex(x, y, z)] = stateId;
    }

    /**
     * @param position the {@link Point} to place the block at
     * @param stateId  the state id of the block to place.
     */
    public void setBlock(@NotNull Point position, short stateId) {
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
     * Gets the offset in the x-axis used when {@link #build(Instance, Point)} or {@link #apply(Point, boolean, boolean, boolean, Block.Setter)} are called.
     *
     * @return the x offset
     */
    public int getOffsetX() {
        return offsetX;
    }

    /**
     * Gets the offset in the y-axis used when {@link #build(Instance, Point)} or {@link #apply(Point, boolean, boolean, boolean, Block.Setter)} are called.
     *
     * @return the y offset
     */
    public int getOffsetY() {
        return offsetY;
    }

    /**
     * Gets the offset in the z-axis used when {@link #build(Instance, Point)} or {@link #apply(Point, boolean, boolean, boolean, Block.Setter)} are called.
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

    /**
     * @return true if this schematic is locked, false otherwise
     */
    public boolean isLocked() {
        return locked;
    }

    /**
     * Sets the locked state of this schematic. Locked schematics can't be built, applied or forked, or saved.
     *
     * @param locked whether to lock this schematic
     */
    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    /**
     * @param offset the {@link Point} to offset this schematic by
     */
    public void setOffset(@NotNull final Point offset) {
        setOffset(offset.blockX(), offset.blockY(), offset.blockZ());
    }

    /**
     * @param x new x offset
     * @param y new y offset
     * @param z new z offset
     */
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
        apply(Vec.ZERO, false, false, false, setter);
    }

    /**
     * @param instance the {@link Instance} to check
     * @param position the {@link Point} to check
     * @return {@code true} if the given position is within the bounds of the given instance, {@code false} otherwise. If either the instance or the position is null, false is returned.
     */
    public boolean isPlaceable(@Nullable final Instance instance, @Nullable final Point position) {
        if (instance == null || position == null) return false;

        return isPlaceable(getContainingRegion(instance, position));
    }

    @Override
    public String toString() {
        return "Schematic{" +
                ", width=" + width +
                ", height=" + height +
                ", length=" + length +
                ", offsetX=" + offsetX +
                ", offsetY=" + offsetY +
                ", offsetZ=" + offsetZ +
                ", area=" + area +
                ", locked=" + locked +
                '}';
    }
}
