/*
 * Scaffolding - Schematic library for Minestom
 *  Copyright (c) 2022-latest The Scaffolding Library Authors
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the “Software”), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in
 *  all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  THE SOFTWARE.
 */
package dev.hypera.scaffolding.schematic.readers;

import dev.hypera.scaffolding.schematic.LegacyLookup;
import dev.hypera.scaffolding.schematic.NBTSchematicReader;
import dev.hypera.scaffolding.schematic.Schematic;
import org.jetbrains.annotations.NotNull;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;
import org.jglrxavpok.hephaistos.nbt.NBTException;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

/**
 * A parser for MCEdit schematics. (.schematic files)
 *
 * <br><br><a href="https://minecraft.fandom.com/wiki/Schematic_file_format">MCEdit format specification</a>
 * <br><a href="https://github.com/EngineHub/WorldEdit/blob/version/5.x/src/main/java/com/sk89q/worldedit/schematic/MCEditSchematicFormat.java">Reference parser</a>
 */
public class MCEditSchematicReader extends NBTSchematicReader {

    @Override
    public boolean isReadable(@NotNull NBTCompound compound) {
        // TODO: Improve this
        return compound.contains("Blocks");
    }

    @Override
    public CompletableFuture<Schematic> read(@NotNull NBTCompound nbtTag, @NotNull Schematic schematic) {
        schematic.reset();

        return CompletableFuture.supplyAsync(() -> {
            try {
                if (!nbtTag.containsKey("Blocks")) {
                    throw new NBTException("Invalid Schematic: No Blocks");
                }

                readSizes(schematic, nbtTag);
                readOffsets(schematic, nbtTag);
                readBlocksData(schematic, nbtTag);

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
        int weOffsetX = getInteger(nbtTag, "WEOffsetX", "Invalid Schematic: No WEOffsetX");
        int weOffsetY = getInteger(nbtTag, "WEOffsetY", "Invalid Schematic: No WEOffsetY");
        int weOffsetZ = getInteger(nbtTag, "WEOffsetZ", "Invalid Schematic: No WEOffsetZ");

        schematic.setOffset(weOffsetX, weOffsetY, weOffsetZ);
    }

    private void readBlocksData(@NotNull Schematic schematic, @NotNull NBTCompound nbtTag) throws NBTException {
        String materials = getString(nbtTag, "Materials", "Invalid Schematic: No Materials");
        if (!materials.equals("Alpha")) {
            throw new NBTException("Invalid Schematic: Invalid Materials");
        }

        byte[] blocks = getByteArray(nbtTag, "Blocks", "Invalid Schematic: No Blocks");
        byte[] blockData = getByteArray(nbtTag, "Data", "Invalid Schematic: No Block Data");

        // Each "add block" contains the upper 4 bits for 2 blocks packed in one byte
        // addBlocks.length / 2 = number of blocks
        byte[] addBlocks = nbtTag.containsKey("AddBlocks") ? Objects.requireNonNull(nbtTag.getByteArray("AddBlocks")).copyArray() : new byte[0];

        short[] outdatedBlockIds = new short[schematic.getArea()];

        for (int index = 0; index < blocks.length; index++) {
            int halfIndex = index >> 1; // same as 'index / 2'
            short addAmount = 0;

            if (halfIndex < addBlocks.length) {
                short rawAdd = (short) (addBlocks[halfIndex] & 0b11111111);
                // If index is even, we want to shift 8 bits (a full byte) to the left, otherwise 4 since a single byte holds 2 blocks.
                int leftShiftAmount = (index % 2 == 0) ? 8 : 4;
                addAmount = (short) (rawAdd << leftShiftAmount);
            }

            outdatedBlockIds[index] = (short) (addAmount + (blocks[index] & 0b11111111));
        }

        for (int x = 0; x < schematic.getWidth(); ++x) {
            for (int y = 0; y < schematic.getHeight(); ++y) {
                for (int z = 0; z < schematic.getLength(); ++z) {
                    int index = schematic.getBlockIndex(x, y, z);
                    // Let's just ignore unknown blocks for now
                    // TODO: log when unknown blocks are encountered?
                    short stateId = LegacyLookup.stateIdFromLegacy(outdatedBlockIds[index], blockData[index]);

                    schematic.setBlock(x, y, z, stateId);
                }
            }
        }
    }

}
