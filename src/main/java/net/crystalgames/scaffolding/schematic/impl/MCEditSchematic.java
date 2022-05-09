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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

// https://github.com/EngineHub/WorldEdit/blob/version/5.x/src/main/java/com/sk89q/worldedit/schematic/MCEditSchematicFormat.java
public class MCEditSchematic implements Schematic {

    private static final HashMap<String, Short> STATE_ID_LOOKUP = new HashMap<>();

    static {
        try {
            // Load state IDS from lookup table
            InputStream is = MCEditSchematic.class.getClassLoader().getResourceAsStream("MCEditBlockStateLookup.txt");
            BufferedInputStream bis = new BufferedInputStream(Objects.requireNonNull(is));
            String raw = new String(bis.readAllBytes());
            for (String line : raw.split("\n")) {
                String[] split = line.split("=");
                STATE_ID_LOOKUP.put(split[0], Short.parseShort(split[1]));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private final List<Region.Block> regionBlocks = new ArrayList<>();

    private short width;
    private short height;
    private short length;
    private short[] blocks;
    private byte[] blockData;

    private boolean read = false;

    private int offsetX;
    private int offsetY;
    private int offsetZ;

    @Override
    public void read(NBTCompound nbtTag) throws NBTException {
        if (!nbtTag.containsKey("Blocks")) throw new NBTException("Invalid Schematic: No Blocks");

        readSizes(nbtTag);
        readBlocksData(nbtTag);
        readOffsets(nbtTag);
        readBlocks();

        read = true;
    }

    private void readOffsets(@NotNull NBTCompound nbtTag) throws NBTException {
        Integer weOffsetX = nbtTag.getInt("WEOffsetX");
        if (weOffsetX == null) throw new NBTException("Invalid Schematic: No WEOffsetX");
        this.offsetX = weOffsetX;

        Integer weOffsetY = nbtTag.getInt("WEOffsetY");
        if (weOffsetY == null) throw new NBTException("Invalid Schematic: No WEOffsetY");
        this.offsetY = weOffsetY;

        Integer weOffsetZ = nbtTag.getInt("WEOffsetZ");
        if (weOffsetZ == null) throw new NBTException("Invalid Schematic: No WEOffsetZ");
        this.offsetZ = weOffsetZ;
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
        if (materials == null || !materials.equals("Alpha"))
            throw new NBTException("Invalid Schematic: Invalid Materials");

        ImmutableByteArray blockIdPre = nbtTag.getByteArray("Blocks");
        if (blockIdPre == null) throw new NBTException("Invalid Schematic: No Blocks");
        byte[] blockId = blockIdPre.copyArray();

        ImmutableByteArray blocksData = nbtTag.getByteArray("Data");
        if (blocksData == null) throw new NBTException("Invalid Schematic: No Block Data");
        this.blockData = blocksData.copyArray();

        // Each "add block" contains the upper 4 bits for 2 blocks packed in one byte
        // addBlocks.length / 2 = number of blocks
        byte[] addBlocks = nbtTag.containsKey("AddBlocks") ? Objects.requireNonNull(nbtTag.getByteArray("AddBlocks")).copyArray() : new byte[0];

        blocks = new short[blockId.length];
        for (int index = 0; index < blockId.length; index++) {
            final int halfIndex = index >> 1; // same as 'index / 2'
            short addAmount = 0;

            if (halfIndex < addBlocks.length) {
                final short rawAdd = (short) (addBlocks[halfIndex] & 0b11111111);
                // If index is even, we want to shift 8 bits (a full byte) to the left, otherwise 4 since a single byte holds 2 blocks.
                // The MCEdit format is weird and uses the upper 4 bits for even blocks and the lower 4 bits for odd blocks
                final int leftShiftAmount = (index % 2 == 0) ? 8 : 4;
                addAmount = (short) (rawAdd << leftShiftAmount);
            }

            this.blocks[index] = (short) (addAmount + (blockId[index] & 0b11111111));
        }
    }

    public void readBlocks() {
        for (int x = 0; x < width; ++x) {
            for (int y = 0; y < height; ++y) {
                for (int z = 0; z < length; ++z) {
                    int index = y * width * length + z * width + x;
                    String legacyId = this.blocks[index] + ":" + this.blockData[index];

                    // Let's just ignore unknown blocks for now
                    // TODO: log when unknown blocks are encountered?
                    short stateId = STATE_ID_LOOKUP.get(legacyId);
                    regionBlocks.add(new Region.Block(new Pos(x + offsetX, y + offsetY, z + offsetZ), stateId));
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

            List<CompletableFuture<Void>> futures = new ArrayList<>();
            for (Region.Block regionBlock : regionBlocks) {
                Pos absoluteBlockPosition = regionBlock.position().add(position);
                short stateId = regionBlock.stateId();

                Block block = Block.fromStateId(stateId);
                if (block != null)
                    futures.add(instance.loadOptionalChunk(absoluteBlockPosition).thenRun(() -> blockBatch.setBlock(absoluteBlockPosition, block)));
            }

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[]{})).join();
            blockBatch.apply(instance, () -> future.complete(new Region(instance, position, position.add(width, height, length))));
        });
        return future;
    }

    @Override
    public short getWidth() {
        return width;
    }

    @Override
    public short getHeight() {
        return height;
    }

    @Override
    public short getLength() {
        return length;
    }

    @Override
    public int getOffsetX() {
        return offsetX;
    }

    @Override
    public int getOffsetY() {
        return offsetY;
    }

    @Override
    public int getOffsetZ() {
        return offsetZ;
    }

    @Override
    public void apply(Block.@NotNull Setter setter) {
        for (Region.Block block : regionBlocks) {
            Pos pos = block.position();
            Block minestomBlock = Block.fromStateId(block.stateId());
            if (minestomBlock != null) {
                setter.setBlock(pos, minestomBlock);
            } else {
                throw new IllegalStateException("Invalid block state id: " + block.stateId());
            }
        }
    }
}
