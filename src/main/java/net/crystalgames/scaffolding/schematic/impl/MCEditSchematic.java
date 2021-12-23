package net.crystalgames.scaffolding.schematic.impl;

import net.crystalgames.scaffolding.region.Region;
import net.crystalgames.scaffolding.schematic.Schematic;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.batch.AbsoluteBlockBatch;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jglrxavpok.hephaistos.collections.ImmutableByteArray;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;
import org.jglrxavpok.hephaistos.nbt.NBTException;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

// https://github.com/EngineHub/WorldEdit/blob/version/5.x/src/main/java/com/sk89q/worldedit/schematic/MCEditSchematicFormat.java
public class MCEditSchematic implements Schematic {

    private final List<Region.Block> regionBlocks = new ArrayList<>();

    private short width;
    private short height;
    private short length;
    private short[] blocks;

    private boolean read = false;

    @Override
    public void read(NBTCompound nbtTag) throws NBTException {
        if (!nbtTag.containsKey("Blocks")) throw new NBTException("Invalid Schematic: No Blocks");

        readSizes(nbtTag);
        readBlocksData(nbtTag);
        readBlocks();

        read = true;
    }

    private void readSizes(@NotNull NBTCompound nbtTag) throws NBTException {
        Short width = nbtTag.getShort("Width");
        if (width == null) throw new NBTException("Invalid Schematic: No Width");
        this.width = width;

        Short height = nbtTag.getShort("Height");
        if (height == null) throw new NBTException("Invalid Schematic: No Height");
        this.height = height;

        Short length = nbtTag.getShort("Length");
        if (length == null) throw new NBTException("Invalid Schematic: No Length");
        this.length = length;
    }


    private void readBlocksData(@NotNull NBTCompound nbtTag) throws NBTException {
        String materials = nbtTag.getString("Materials");
        if (materials == null || !materials.equals("Alpha")) throw new NBTException("Invalid Schematic: Invalid Materials");

        ImmutableByteArray blockIdPre = nbtTag.getByteArray("Blocks");
        if (blockIdPre == null) throw new NBTException("Invalid Schematic: No Blocks");
        byte[] blockId = blockIdPre.copyArray();

        ImmutableByteArray blocksData = nbtTag.getByteArray("Data");
        if (blocksData == null) throw new NBTException("Invalid Schematic: No Block Data");
        blocksData.copyArray();

        byte[] addId;
        if (nbtTag.containsKey("AddBlocks")) addId = Objects.requireNonNull(nbtTag.getByteArray("AddBlocks")).copyArray();
        else addId = new byte[0];

        blocks = new short[blockId.length];
        for (int index = 0; index < blockId.length; index++) {
            if ((index >> 1) >= addId.length) this.blocks[index] = (short) (blockId[index] & 0xFF);
            else {
                if ((index & 1) == 0) this.blocks[index] = (short) (((addId[index >> 1] & 0x0F) << 8) + (blockId[index] & 0xFF));
                else this.blocks[index] = (short) (((addId[index >> 1] & 0xF0) << 4) + (blockId[index] & 0xFF));
            }
        }
    }

    public void readBlocks() {
        for (int x = 0; x < width; ++x) {
            for (int y = 0; y < height; ++y) {
                for (int z = 0; z < length; ++z) {
                    int index = y * width * length + z * width + x;
                    short stateId = this.blocks[index];
                    regionBlocks.add(new Region.Block(new Pos(x, y, z), stateId));
                }
            }
        }
    }

    @Override
    public void write(OutputStream outputStream, Region region) {
        // TODO: Complete
    }

    @Override
    public CompletableFuture<Region> build(Instance instance, Pos position) {
        if (!read) throw new IllegalStateException("Schematic not read");
        CompletableFuture<Region> future = new CompletableFuture<>();
        CompletableFuture.runAsync(() -> {
            AbsoluteBlockBatch blockBatch = new AbsoluteBlockBatch();

            for (Region.Block regionBlock : regionBlocks) {
                Pos blockPosition = regionBlock.position();
                short stateId = regionBlock.stateId();

                Block block = Block.fromStateId(stateId);
                if (block != null) blockBatch.setBlock(blockPosition, block);
            }

            blockBatch.apply(instance, () -> future.complete(new Region(instance, position, position.add(width, height, length))));
        });
        return future;
    }

}
