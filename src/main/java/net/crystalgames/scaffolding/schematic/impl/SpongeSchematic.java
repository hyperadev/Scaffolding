package net.crystalgames.scaffolding.schematic.impl;

import net.crystalgames.scaffolding.region.Region;
import net.crystalgames.scaffolding.schematic.AbstractSchematic;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.batch.AbsoluteBlockBatch;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jglrxavpok.hephaistos.collections.ImmutableByteArray;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;
import org.jglrxavpok.hephaistos.nbt.NBTException;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.CompletableFuture;

// https://github.com/EngineHub/WorldEdit/blob/303f5a76b2df70d63480f2126c9ef4b228eb3c59/worldedit-core/src/main/java/com/sk89q/worldedit/extent/clipboard/io/SpongeSchematicReader.java#L261-L297
public class SpongeSchematic extends AbstractSchematic {

    private final List<Region.Block> regionBlocks = new ArrayList<>();

    private short width;
    private short height;
    private short length;
    private Map<String, Integer> palette = new HashMap<>();

    private byte[] blocksData;

    public SpongeSchematic(InputStream inputStream) throws IOException, NBTException {
        super(inputStream);
    }

    public SpongeSchematic(NBTCompound nbtTag) {
        super(nbtTag);
    }

    @Override
    public void read() throws NBTException {
        readSizes(nbtTag);
        readBlockPalette(nbtTag);
        readBlocks();
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

    private void readBlockPalette(@NotNull NBTCompound nbtTag) throws NBTException {
        Integer maxPalette = nbtTag.getInt("PaletteMax");
        if (maxPalette == null) throw new NBTException("Invalid Schematic: No PaletteMax");

        NBTCompound nbtPalette = (NBTCompound) nbtTag.get("Palette");
        if (nbtPalette == null) throw new NBTException("Invalid Schematic: No Palette");

        Set<String> keys = nbtPalette.getKeys();
        if (keys.size() != maxPalette) throw new NBTException("Invalid Schematic: PaletteMax does not match Palette size");

        for(String key : keys) {
            Integer value = nbtPalette.getInt(key);
            if (value == null) throw new NBTException("Invalid Schematic: Palette contains invalid value");

            palette.put(key, value);
        }

        palette = palette.entrySet().stream()
                .sorted(Map.Entry.comparingByValue())
                .collect(LinkedHashMap::new,(map, entry) -> map.put(entry.getKey(), entry.getValue()), LinkedHashMap::putAll);

        ImmutableByteArray blocksData = nbtTag.getByteArray("BlockData");
        if (blocksData == null || blocksData.getSize() == 0) throw new NBTException("Invalid Schematic: No BlockData");
        this.blocksData = blocksData.copyArray();
    }

    private void readBlocks() throws NBTException {
        int index = 0;
        int i = 0;
        int value;
        int varIntLength;
        List<String> paletteKeys = new ArrayList<>(palette.keySet());

        while (i < this.blocksData.length) {
            value = 0;
            varIntLength = 0;

            while (true) {
                value |= (this.blocksData[i] & 127) << (varIntLength++ * 7);
                if (varIntLength > 5) throw new NBTException("Invalid Schematic: BlockData has invalid length");
                if ((this.blocksData[i] & 128) != 128) {
                    i++;
                    break;
                }
                i++;
            }

            int x = (index % (width * length)) % width;
            int y = index / (width * length);
            int z = (index % (width * length)) / width;

            String block = paletteKeys.get(value);
            short stateId = getStateId(block);

            //                                               Is adding the height to y needed?
            this.regionBlocks.add(new Region.Block(new Pos(x, y + height, z), stateId));

            index++;
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

    private Block getBlock(@NotNull String input) {
        String namespaceId = input.split("\\[")[0];

        return Block.fromNamespaceId(namespaceId);
    }

    private short getStateId(@NotNull String input) {
        Block block = getBlock(input);
        if (block == null) return 0;
        String states = input.replaceAll(block.name(), "");

        if (states.startsWith("[")) {
            String[] stateArray = states.substring(1, states.length() - 1).split(",");
            Map<String, String> properties = new HashMap<>(block.properties());
            for (String state : stateArray) {
                String[] split = state.split("=");
                properties.replace(split[0], split[1]);
            }
            try {
                return block.withProperties(properties).stateId();
            } catch (Exception e) {
                e.printStackTrace();
                return block.stateId();
            }
        } else return block.stateId();
    }

}
