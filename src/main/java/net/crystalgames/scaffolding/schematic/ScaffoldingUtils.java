package net.crystalgames.scaffolding.schematic;

import net.crystalgames.scaffolding.region.Region;
import net.crystalgames.scaffolding.schematic.readers.MCEditSchematicReader;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

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
        // No instances
    }

    public static short stateIdFromLegacy(int legacyBlockId, byte legacyBlockData) {
        return LEGACY_LOOKUP.get(getLookupId(legacyBlockId, legacyBlockData));
    }

    private static int getLookupId(int legacyBlockId, byte legacyBlockData) {
        return legacyBlockId << 8 | legacyBlockData;
    }

    public static @NotNull CompletableFuture<Void> loadChunks(@NotNull Instance instance, @NotNull Region region) {
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
