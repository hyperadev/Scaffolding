package net.crystalgames.scaffolding.schematic;

import net.crystalgames.scaffolding.region.Region;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.batch.AbsoluteBlockBatch;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.generator.GenerationUnit;
import net.minestom.server.instance.generator.UnitModifier;
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

    public @NotNull CompletableFuture<Void> copy(@NotNull Instance instance, Region region) {
        reset();

        return CompletableFuture.runAsync(() -> {
            CompletableFuture<Void> loadChunksFuture = ScaffoldingUtils.loadChunks(instance, region);

            setSize(width, height, length);

            loadChunksFuture.join();

            for (int x = 0; x < region.sizeX(); ++x) {
                for (int y = 0; y < region.sizeY(); ++y) {
                    for (int z = 0; z < region.sizeZ(); ++z) {
                        final int blockX = region.lower().blockX() + x;
                        final int blockY = region.lower().blockY() + y;
                        final int blockZ = region.lower().blockZ() + z;

                        Block block = instance.getBlock(blockX, blockY, blockZ, Block.Getter.Condition.TYPE);
                        if (block == null) return;

                        blocks[getIndex(x, y, z)] = block.stateId();
                    }
                }
            }

            locked = false;
        });
    }

    public @NotNull CompletableFuture<Region> build(@NotNull Instance instance, @NotNull Pos position, boolean flipX, boolean flipY, boolean flipZ) {
        if (locked) throw new IllegalStateException("Cannot build a locked schematic.");

        final Region region = getContainingRegion(instance, position);

        // TODO: make this error message better
        if(!isPlaceable(instance, region)) throw new IllegalStateException("Cannot build schematic at this position since blocks would go outside of world boundaries. " + position);

        final CompletableFuture<Void> loadChunks = ScaffoldingUtils.loadChunks(instance, region);
        final AbsoluteBlockBatch blockBatch = new AbsoluteBlockBatch();

        apply(region.lower(), flipX, flipY, flipZ, blockBatch);

        final CompletableFuture<Region> future = new CompletableFuture<>();
        loadChunks.thenRun(() -> {
            try {
                blockBatch.apply(instance, () -> future.complete(region));
            } catch (Exception e) {
                future.completeExceptionally(e);
            }
        });
        return future;
    }

    public @NotNull CompletableFuture<Region> build(@NotNull Instance instance, @NotNull Pos position) {
        return build(instance, position, false, false, false);
    }

    private void apply(@NotNull Pos start, boolean flipX, boolean flipY, boolean flipZ, @NotNull Block.Setter setter) {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                for (int z = 0; z < length; z++) {
                    // Will the JVM optimize out the ternary operator? I hope so.
                    final int indexX = flipX ? width - x - 1 : x;
                    final int indexY = flipY ? height - y - 1 : y;
                    final int indexZ = flipZ ? length - z - 1 : z;

                    int blockX = start.blockX() + x;
                    int blockY = start.blockY() + y;
                    int blockZ = start.blockZ() + z;

                    Block block = getBlock(indexX, indexY, indexZ);

                    if (block != null) setter.setBlock(blockX, blockY, blockZ, block);
                }
            }
        }
    }

    public void fork(@NotNull GenerationUnit unit, @NotNull Pos position, boolean flipX, boolean flipY, boolean flipZ) {
        if (locked) throw new IllegalStateException("Cannot fork a locked schematic.");

        final Pos start = position.sub(offsetX, offsetY, offsetZ);
        final Pos end = start.add(width, height, length);

        UnitModifier forkModifier = unit.fork(start, end).modifier();

        apply(start, flipX, flipY, flipZ, forkModifier);
    }

    public void reset() {
        width = height = length = 0;
        offsetX = offsetY = offsetZ = 0;

        blocks = null; // Does this actually have to be nulled out? This looks a bit too much like a deconstructor.
        locked = true;
    }

    public short getStateId(int x, int y, int z) {
        return blocks[getIndex(x, y, z)];
    }

    @Nullable
    public Block getBlock(int indexX, int indexY, int indexZ) {
        short stateId = getStateId(indexX, indexY, indexZ);
        return Block.fromStateId(stateId);
    }

    public int getIndex(int x, int y, int z) {
        return y * width * length + z * width + x;
    }

    public void setSize(int sizeX, int sizeY, int sizeZ) {
        this.width = sizeX;
        this.height = sizeY;
        this.length = sizeZ;

        area = sizeX * sizeY * sizeZ;
        blocks = new short[area];
    }

    public void setBlock(int x, int y, int z, short stateId) {
        blocks[getIndex(x, y, z)] = stateId;
    }

    public void setBlock(int x, int y, int z, @NotNull Block block) {
        setBlock(x, y, z, block.stateId());
    }

    public void setBlock(@NotNull Pos position, @NotNull Block block) {
        setBlock(position.blockX(), position.blockY(), position.blockZ(), block);
    }

    public void setBlock(@NotNull Pos position, short stateId) {
        setBlock(position.blockX(), position.blockY(), position.blockZ(), stateId);
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getLength() {
        return length;
    }

    public int getOffsetX() {
        return offsetX;
    }

    public int getOffsetY() {
        return offsetY;
    }

    public int getOffsetZ() {
        return offsetZ;
    }

    public int getArea() {
        return area;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public void setOffset(int x, int y, int z) {
        offsetX = x;
        offsetY = y;
        offsetZ = z;
    }

    public void setOffset(@NotNull Pos position) {
        setOffset(position.blockX(), position.blockY(), position.blockZ());
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

    private boolean isPlaceable(@NotNull Instance instance, @NotNull Region region) {
        return region.upper().blockY() <= instance.getDimensionType().getMaxY() && region.lower().blockY() >= instance.getDimensionType().getMinY();
    }

    public boolean isPlaceable(@NotNull Instance instance, @NotNull Pos position) {
        return isPlaceable(instance, getContainingRegion(instance, position));
    }

    public Region getContainingRegion(@NotNull Instance instance, @NotNull Pos position) {
        return new Region(instance, position.add(offsetX, offsetY, offsetZ), position.add(offsetX + width, offsetY + height, offsetZ + length));
    }
}
