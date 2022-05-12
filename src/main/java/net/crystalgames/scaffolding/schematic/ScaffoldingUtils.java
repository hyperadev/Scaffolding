package net.crystalgames.scaffolding.schematic;

import net.crystalgames.scaffolding.region.Region;
import net.crystalgames.scaffolding.schematic.readers.MCEditSchematicReader;
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
     * @param legacyBlockId The legacy block ID
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
     * @return a {@link CompletableFuture<Void>} that will complete once all chunks in the region have been loaded
     */
    public static @NotNull CompletableFuture<Void> loadChunks(@NotNull final Region region) {
        final Instance instance = region.instance();

        final int lengthX = region.upperChunkX() - region.lowerChunkX() + 1;
        final int lengthZ = region.upperChunkZ() - region.lowerChunkZ() + 1;

        final CompletableFuture<?>[] futures = new CompletableFuture[lengthX * lengthZ];
        int index = 0;

        for (int x = region.lowerChunkX(); x <= region.upperChunkX(); ++x) {
            for (int z = region.lowerChunkZ(); z <= region.upperChunkZ(); ++z) {
                futures[index++] = instance.loadChunk(x, z);
            }
        }

        return CompletableFuture.allOf(futures);
    }
}
