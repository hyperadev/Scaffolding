package net.crystalgames.scaffolding.schematic.impl;

import kotlin.Pair;
import net.crystalgames.scaffolding.region.Region;
import net.crystalgames.scaffolding.schematic.Schematic;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.batch.AbsoluteBlockBatch;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jglrxavpok.hephaistos.collections.ImmutableByteArray;
import org.jglrxavpok.hephaistos.nbt.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

// https://github.com/EngineHub/WorldEdit/blob/version/5.x/src/main/java/com/sk89q/worldedit/schematic/MCEditSchematicFormat.java
public class MCEditSchematic implements Schematic {

    private final File file;

    private final List<Region.Block> regionBlocks = new ArrayList<>();

    private short width;
    private short height;
    private short length;

    private String materials;
    private byte[] addId;
    private short[] blocks;

    public MCEditSchematic(File file) {
        this.file = file;
    }

    @Override
    public void read() throws IOException, NBTException {
        try (NBTReader reader = new NBTReader(file, CompressedProcesser.GZIP)) {
            Pair<String, NBT> pair = reader.readNamed();
            NBTCompound nbtTag = (NBTCompound) pair.getSecond();

            if (!pair.getFirst().equals("Schematic")) throw new NBTException("Invalid Schematic: Not a Schematic");
            if (!nbtTag.containsKey("Blocks")) throw new NBTException("Invalid Schematic: No Blocks");

            readSizes(nbtTag);
            readBlocksData(nbtTag);
            readBlocks();
        }
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
        if (this.materials == null || !this.materials.equals("Alpha")) throw new NBTException("Invalid Schematic: Invalid Materials");
        this.materials = materials;

        ImmutableByteArray blockId = nbtTag.getByteArray("Blocks");
        if (blockId == null) throw new NBTException("Invalid Schematic: No Blocks");
        byte[] blockId1 = blockId.copyArray();

        ImmutableByteArray blocksData = nbtTag.getByteArray("Data");
        if (blocksData == null) throw new NBTException("Invalid Schematic: No Block Data");
        blocksData.copyArray();

        if (nbtTag.containsKey("AddBlocks")) addId = Objects.requireNonNull(nbtTag.getByteArray("AddBlocks")).copyArray();

        blocks = new short[blockId1.length];
        for (int index = 0; index < blockId1.length; index++) {
            if ((index >> 1) >= this.addId.length) {
                this.blocks[index] = (short) (blockId1[index] & 0xFF);
            } else {
                if ((index & 1) == 0) {
                    this.blocks[index] = (short) (((this.addId[index >> 1] & 0x0F) << 8) + (blockId1[index] & 0xFF));
                } else {
                    this.blocks[index] = (short) (((this.addId[index >> 1] & 0xF0) << 4) + (blockId1[index] & 0xFF));
                }
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
    public void write(Region region) {
        // TODO: Complete
    }

    @Override
    public CompletableFuture<Region> build(Instance instance, Pos position) {
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
