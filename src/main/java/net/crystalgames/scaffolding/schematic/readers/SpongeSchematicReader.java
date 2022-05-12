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
public class SpongeSchematicReader extends NBTSchematicReader {

    @Override
    public CompletableFuture<Schematic> read(@NotNull final NBTCompound nbtTag, @NotNull final Schematic schematic) {
        schematic.reset();

        return CompletableFuture.supplyAsync(() -> {
            try {
                readSizes(schematic, nbtTag);
                readOffsets(schematic, nbtTag);
                readBlockPalette(schematic, nbtTag);

                schematic.setLocked(false);
                return schematic;
            } catch (Exception e) {
                throw new CompletionException(e);
            }
        });
    }

    private void readSizes(@NotNull Schematic schematic, @NotNull NBTCompound nbtTag) throws NBTException {
        short width = getShort(nbtTag, "Width", "Invalid Schematic: No Width");
        short height = getShort(nbtTag, "Height", "Invalid Schematic: No Height");
        short length = getShort(nbtTag, "Length", "Invalid Schematic: No Length");
        schematic.setSize(width, height, length);
    }

    private void readOffsets(@NotNull Schematic schematic, @NotNull NBTCompound nbtTag) throws NBTException {
        NBTCompound metadata = getCompound(nbtTag, "Metadata", "Invalid Schematic: No Metadata");
        int weOffsetX = getInteger(metadata, "WEOffsetX", "Invalid Schematic: No WEOffsetX In Metadata");
        int weOffsetY = getInteger(metadata, "WEOffsetY", "Invalid Schematic: No WEOffsetY In Metadata");
        int weOffsetZ = getInteger(metadata, "WEOffsetZ", "Invalid Schematic: No WEOffsetZ In Metadata");
        schematic.setOffset(weOffsetX, weOffsetY, weOffsetZ);
    }

    private void readBlockPalette(@NotNull Schematic schematic, @NotNull NBTCompound nbtTag) throws NBTException {
        int maxPalette = getInteger(nbtTag, "PaletteMax", "Invalid Schematic: No PaletteMax");
        NBTCompound nbtPalette = getCompound(nbtTag, "Palette", "Invalid Schematic: No Palette");

        Set<String> keys = nbtPalette.getKeys();
        if (keys.size() != maxPalette)
            throw new NBTException("Invalid Schematic: PaletteMax does not match Palette size");

        final Map<String, Integer> unsortedPalette = new HashMap<>();
        for (String key : keys)
            unsortedPalette.put(key, getInteger(nbtPalette, key, "Invalid Schematic: Palette contains invalid value"));


        final Map<String, Integer> palette = unsortedPalette.entrySet().stream()
                .sorted(Map.Entry.comparingByValue())
                .collect(LinkedHashMap::new, (map, entry) -> map.put(entry.getKey(), entry.getValue()), LinkedHashMap::putAll);

        ImmutableByteArray blocksData = nbtTag.getByteArray("BlockData");
        if (blocksData == null || blocksData.getSize() == 0) throw new NBTException("Invalid Schematic: No BlockData");
        byte[] blocksDataArr = blocksData.copyArray();

        int index = 0;
        int i = 0;
        int value;
        int varIntLength;
        List<String> paletteKeys = new ArrayList<>(palette.keySet());

        while (i < blocksDataArr.length) {
            value = 0;
            varIntLength = 0;

            while (true) {
                value |= (blocksDataArr[i] & 127) << (varIntLength++ * 7);
                if (varIntLength > 5) throw new NBTException("Invalid Schematic: BlockData has invalid length");
                if ((blocksDataArr[i] & 128) != 128) {
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

    private Block getBlock(@NotNull String input) {
        String namespaceId = input.split("\\[")[0];

        return Block.fromNamespaceId(namespaceId);
    }
}
