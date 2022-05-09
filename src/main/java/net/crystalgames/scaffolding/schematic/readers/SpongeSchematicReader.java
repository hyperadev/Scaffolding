package net.crystalgames.scaffolding.schematic.readers;

import net.crystalgames.scaffolding.schematic.NBTSchematicReader;
import net.crystalgames.scaffolding.schematic.Schematic;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jglrxavpok.hephaistos.collections.ImmutableByteArray;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;
import org.jglrxavpok.hephaistos.nbt.NBTException;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

// https://github.com/EngineHub/WorldEdit/blob/303f5a76b2df70d63480f2126c9ef4b228eb3c59/worldedit-core/src/main/java/com/sk89q/worldedit/extent/clipboard/io/SpongeSchematicReader.java#L261-L297
public class SpongeSchematicReader implements NBTSchematicReader {

    private Map<String, Integer> palette = new HashMap<>();
    private byte[] blocksData;

    @Override
    public CompletableFuture<Schematic> read(@NotNull Schematic schematic, @NotNull NBTCompound nbtTag) {
        schematic.reset();

        return CompletableFuture.supplyAsync(() -> {
            try {
                readSizes(schematic, nbtTag);
                readBlockPalette(schematic, nbtTag);
                readOffsets(schematic, nbtTag);
                readBlocks(schematic);

                schematic.setLocked(false);

                return schematic;
            } catch (Exception e) {
                throw new CompletionException(e);
            }
        });
    }

    private void readOffsets(@NotNull Schematic schematic, @NotNull NBTCompound nbtTag) throws NBTException {
        NBTCompound metaData = nbtTag.getCompound("Metadata");
        if (metaData == null) throw new NBTException("Invalid Schematic: No Metadata");

        Integer weOffsetX = metaData.getInt("WEOffsetX");
        if (weOffsetX == null) throw new NBTException("Invalid Schematic: No WEOffsetX In Metadata");

        Integer weOffsetY = metaData.getInt("WEOffsetY");
        if (weOffsetY == null) throw new NBTException("Invalid Schematic: No WEOffsetY In Metadata");

        Integer weOffsetZ = metaData.getInt("WEOffsetZ");
        if (weOffsetZ == null) throw new NBTException("Invalid Schematic: No WEOffsetZ In Metadata");

        schematic.setOffset(weOffsetX, weOffsetY, weOffsetZ);
    }

    private void readSizes(@NotNull Schematic schematic, @NotNull NBTCompound nbtTag) throws NBTException {
        Short width = nbtTag.getShort("Width");
        if (width == null) throw new NBTException("Invalid Schematic: No Width");

        Short height = nbtTag.getShort("Height");
        if (height == null) throw new NBTException("Invalid Schematic: No Height");

        Short length = nbtTag.getShort("Length");
        if (length == null) throw new NBTException("Invalid Schematic: No Length");

        schematic.setSize(width, height, length);
    }

    private void readBlockPalette(@NotNull Schematic schematic, @NotNull NBTCompound nbtTag) throws NBTException {
        Integer maxPalette = nbtTag.getInt("PaletteMax");
        if (maxPalette == null) throw new NBTException("Invalid Schematic: No PaletteMax");

        NBTCompound nbtPalette = (NBTCompound) nbtTag.get("Palette");
        if (nbtPalette == null) throw new NBTException("Invalid Schematic: No Palette");

        Set<String> keys = nbtPalette.getKeys();
        if (keys.size() != maxPalette)
            throw new NBTException("Invalid Schematic: PaletteMax does not match Palette size");

        for (String key : keys) {
            Integer value = nbtPalette.getInt(key);
            if (value == null) throw new NBTException("Invalid Schematic: Palette contains invalid value");

            palette.put(key, value);
        }

        palette = palette.entrySet().stream()
                .sorted(Map.Entry.comparingByValue())
                .collect(LinkedHashMap::new, (map, entry) -> map.put(entry.getKey(), entry.getValue()), LinkedHashMap::putAll);

        ImmutableByteArray blocksData = nbtTag.getByteArray("BlockData");
        if (blocksData == null || blocksData.getSize() == 0) throw new NBTException("Invalid Schematic: No BlockData");
        this.blocksData = blocksData.copyArray();
    }

    private void readBlocks(@NotNull Schematic schematic) throws NBTException {
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

            int x = (index % (schematic.getWidth() * schematic.getLength())) % schematic.getWidth();
            int y = index / (schematic.getWidth() * schematic.getLength());
            int z = (index % (schematic.getWidth() * schematic.getLength())) / schematic.getWidth();

            String block = paletteKeys.get(value);
            short stateId = getStateId(block);

            schematic.setBlock(x, y, z, stateId);

            index++;
        }
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
