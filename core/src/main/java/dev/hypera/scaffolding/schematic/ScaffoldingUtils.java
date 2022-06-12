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
package dev.hypera.scaffolding.schematic;

import dev.hypera.scaffolding.region.Region;
import dev.hypera.scaffolding.schematic.readers.MCEditSchematicReader;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * A static utility class containing useful methods used throughout Scaffolding.
 */
@ApiStatus.Internal
public final class ScaffoldingUtils {

    // TODO: Replace with a collection that doesn't require autoboxing
    private static final HashMap<Integer, Short> LEGACY_LOOKUP = new HashMap<>();

    // This is awful, but it'll work for now. TODO: rewrite
    static {
        try {
            // Load state IDS from lookup table
            InputStream is = MCEditSchematicReader.class.getClassLoader().getResourceAsStream("LegacyLookupTable.txt");
            BufferedInputStream bis = new BufferedInputStream(Objects.requireNonNull(is));
            String raw = new String(bis.readAllBytes());
            for (String line : raw.split("\n")) {
                String[] split = line.split("=");
                String[] key = split[0].split(":");

                int blockId = Integer.parseInt(key[0]);
                byte blockData = Byte.parseByte(key[1]);

                LEGACY_LOOKUP.put(getLookupId(blockId, blockData), Short.parseShort(split[1]));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private ScaffoldingUtils() {
        throw new UnsupportedOperationException();
    }

    /**
     * @param legacyBlockId   The legacy block ID
     * @param legacyBlockData The legacy block data
     * @return The modern state ID for the given legacy block ID and data
     */
    public static short stateIdFromLegacy(final int legacyBlockId, final byte legacyBlockData) {
        final int lookupId = getLookupId(legacyBlockId, legacyBlockData);
        return LEGACY_LOOKUP.get(lookupId);
    }

    /**
     * Used to get the lookup ID for the given block ID and data from the legacy lookup table.
     *
     * @param legacyBlockId   the legacy block ID
     * @param legacyBlockData the legacy block data
     * @return the lookup ID
     */
    @Contract(pure = true)
    private static int getLookupId(final int legacyBlockId, final byte legacyBlockData) {
        return legacyBlockId << 8 | legacyBlockData;
    }

    /**
     * Force loads all {@link Chunk}s in the given {@link Region}.
     *
     * @param region the {@link Region} in which to load chunks
     * @return a {@link CompletableFuture<Region>} that will complete once all chunks in the region have been loaded. The future will give the region as the result so that you can chain it.
     */
    public static @NotNull CompletableFuture<Region> loadChunks(@NotNull final Region region) {
        final Instance instance = region.getInstance();

        final int lengthX = region.getUpperChunkX() - region.getLowerChunkX() + 1;
        final int lengthZ = region.getUpperChunkZ() - region.getLowerChunkZ() + 1;

        final CompletableFuture<?>[] futures = new CompletableFuture[lengthX * lengthZ];
        int index = 0;

        for (int x = region.getLowerChunkX(); x <= region.getUpperChunkX(); ++x) {
            for (int z = region.getLowerChunkZ(); z <= region.getUpperChunkZ(); ++z) {
                futures[index++] = instance.loadChunk(x, z);
            }
        }

        return CompletableFuture.allOf(futures).thenApply(v -> region);
    }
}
