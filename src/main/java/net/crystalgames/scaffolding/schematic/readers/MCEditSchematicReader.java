package net.crystalgames.scaffolding.schematic.readers;

import net.crystalgames.scaffolding.schematic.NBTSchematicReader;
import net.crystalgames.scaffolding.schematic.ScaffoldingUtils;
import net.crystalgames.scaffolding.schematic.Schematic;
import org.jetbrains.annotations.NotNull;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;
import org.jglrxavpok.hephaistos.nbt.NBTException;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

// https://github.com/EngineHub/WorldEdit/blob/master/worldedit-core/src/main/java/com/sk89q/worldedit/extent/clipboard/io/MCEditSchematicReader.java
public class MCEditSchematicReader extends NBTSchematicReader {

    @Override
    public CompletableFuture<Schematic> read(@NotNull Schematic schematic, @NotNull NBTCompound nbtTag) {
        schematic.reset();

        return CompletableFuture.supplyAsync(() -> {
            try {
                if (!nbtTag.containsKey("Blocks")) throw new NBTException("Invalid Schematic: No Blocks");

                readSizes(schematic, nbtTag);
                readBlocksData(schematic, nbtTag);
                readOffsets(schematic, nbtTag);

                schematic.setLocked(false);

                return schematic;
            } catch (Exception e) {
                throw new CompletionException(e);
            }
        });
    }

    private void readOffsets(@NotNull Schematic schematic, @NotNull NBTCompound nbtTag) throws NBTException {
        int weOffsetX = getInteger(nbtTag, "WEOffsetX", "Invalid Schematic: No WEOffsetX");
        int weOffsetY = getInteger(nbtTag, "WEOffsetY", "Invalid Schematic: No WEOffsetY");
        int weOffsetZ = getInteger(nbtTag, "WEOffsetZ", "Invalid Schematic: No WEOffsetZ");

        schematic.setOffset(weOffsetX, weOffsetY, weOffsetZ);
    }

    private void readSizes(@NotNull Schematic schematic, @NotNull NBTCompound nbtTag) throws NBTException {
        short width = getShort(nbtTag, "Width", "Invalid Schematic: No Width");
        short height = getShort(nbtTag, "Height", "Invalid Schematic: No Height");
        short length = getShort(nbtTag, "Length", "Invalid Schematic: No Length");

        schematic.setSize(width, height, length);
    }


    private void readBlocksData(@NotNull Schematic schematic, @NotNull NBTCompound nbtTag) throws NBTException {
        String materials = getString(nbtTag, "Materials", "Invalid Schematic: No Materials");
        if (!materials.equals("Alpha")) throw new NBTException("Invalid Schematic: Invalid Materials");

        byte[] blocks = getByteArray(nbtTag, "Blocks", "Invalid Schematic: No Blocks");
        byte[] blockData = getByteArray(nbtTag, "Data", "Invalid Schematic: No Block Data");

        // Each "add block" contains the upper 4 bits for 2 blocks packed in one byte
        // addBlocks.length / 2 = number of blocks
        byte[] addBlocks = nbtTag.containsKey("AddBlocks") ? Objects.requireNonNull(nbtTag.getByteArray("AddBlocks")).copyArray() : new byte[0];

        short[] outdatedBlockIds = new short[schematic.getArea()];

        for (int index = 0; index < blocks.length; index++) {
            final int halfIndex = index >> 1; // same as 'index / 2'
            short addAmount = 0;

            if (halfIndex < addBlocks.length) {
                final short rawAdd = (short) (addBlocks[halfIndex] & 0b11111111);
                // If index is even, we want to shift 8 bits (a full byte) to the left, otherwise 4 since a single byte holds 2 blocks.
                // The MCEdit format is weird and uses the upper 4 bits for even blocks and the lower 4 bits for odd blocks
                final int leftShiftAmount = (index % 2 == 0) ? 8 : 4;
                addAmount = (short) (rawAdd << leftShiftAmount);
            }

            outdatedBlockIds[index] = (short) (addAmount + (blocks[index] & 0b11111111));
        }

        for (int x = 0; x < schematic.getWidth(); ++x) {
            for (int y = 0; y < schematic.getHeight(); ++y) {
                for (int z = 0; z < schematic.getLength(); ++z) {
                    int index = schematic.getIndex(x, y, z);
                    // Let's just ignore unknown blocks for now
                    // TODO: log when unknown blocks are encountered?
                    short stateId = ScaffoldingUtils.stateIdFromLegacy(outdatedBlockIds[index], blockData[index]);

                    schematic.setBlock(x, y, z, stateId);
                }
            }
        }
    }
}
