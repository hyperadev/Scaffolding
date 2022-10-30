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

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import dev.hypera.scaffolding.Scaffolding;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.io.FastBufferedInputStream;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * A static utility class containing useful methods used throughout Scaffolding.
 */
@Internal
public final class LegacyLookup {

    private static final @NotNull String LEGACY_FILE_NAME = "/legacy.json";
    private static final @NotNull Int2ObjectMap<Short> LEGACY_LOOKUP = new Int2ObjectOpenHashMap<>();
    private static final @NotNull Logger LOGGER = LoggerFactory.getLogger(LegacyLookup.class);

    static {
        LOGGER.info(LegacyLookup.class.getResource(LEGACY_FILE_NAME).getFile());
        try(BufferedReader reader = new BufferedReader(new InputStreamReader(LegacyLookup.class.getResourceAsStream(LEGACY_FILE_NAME), StandardCharsets.UTF_8))) {
            JsonArray obj = JsonParser.parseReader(reader).getAsJsonArray();


            for (JsonElement jsonElement : obj) {
                JsonObject object = jsonElement.getAsJsonObject();

                LEGACY_LOOKUP.computeIfAbsent(
                        getLookupId(object.get("block").getAsInt(), object.get("data").getAsByte()
                        ),
                        integer -> object.get("state").getAsShort()
                );
            }
            LOGGER.info("Loaded legacy data.");
        } catch (IOException e) {
            LOGGER.error("Error while initializing Legacy data", e);
        }
    }

    private LegacyLookup() {
    }


    /**
     * @param legacyBlockId   The legacy block ID
     * @param legacyBlockData The legacy block data
     * @return The modern state ID for the given legacy block ID and data
     */
    public static short stateIdFromLegacy(int legacyBlockId, byte legacyBlockData) {
        return LEGACY_LOOKUP.get(getLookupId(legacyBlockId, legacyBlockData));
    }


    /**
     * Used to get the lookup ID for the given block ID and data from the legacy lookup table.
     *
     * @param legacyBlockId   the legacy block ID
     * @param legacyBlockData the legacy block data
     * @return the lookup ID
     */
    @Contract(pure = true)
    private static int getLookupId(int legacyBlockId, byte legacyBlockData) {
        return legacyBlockId << 8 | legacyBlockData;
    }

}
