package net.crystalgames.scaffolding;

import kotlin.Pair;
import net.crystalgames.scaffolding.schematic.NBTSchematicReader;
import net.crystalgames.scaffolding.schematic.Schematic;
import net.crystalgames.scaffolding.schematic.readers.MCEditSchematicReader;
import net.crystalgames.scaffolding.schematic.readers.SpongeSchematicReader;
import org.jetbrains.annotations.NotNull;
import org.jglrxavpok.hephaistos.nbt.*;

import java.io.*;
import java.util.concurrent.CompletableFuture;

public class Scaffolding {

    private static final NBTSchematicReader MC_EDIT_SCHEMATIC_READER = new MCEditSchematicReader();
    private static final NBTSchematicReader SPONGE_SCHEMATIC_READER = new SpongeSchematicReader();

    /**
     * Automatically detects the type of schematic and parses the input stream
     *
     * @param inputStream Schematic input
     * @return parsed schematic
     * @throws IOException              if the input stream is invalid
     * @throws NBTException             if the schematic is invalid
     * @throws IllegalArgumentException if the schematic is neither an MCEdit nor a Sponge schematic
     */
    public static @NotNull CompletableFuture<Schematic> fromStream(@NotNull InputStream inputStream) throws IOException, NBTException, IllegalArgumentException {
        NBTReader reader = new NBTReader(inputStream, CompressedProcesser.GZIP);
        Pair<String, NBT> pair = reader.readNamed();
        NBTCompound nbtTag = (NBTCompound) pair.getSecond();

        Schematic schematic = new Schematic();
        if (nbtTag.contains("Blocks")) return MC_EDIT_SCHEMATIC_READER.read(schematic, nbtTag);
        else if (nbtTag.contains("Palette")) return SPONGE_SCHEMATIC_READER.read(schematic, nbtTag);
        else throw new IllegalArgumentException("Unknown schematic type.");
    }

    /**
     * Automatically detects the type of schematic and parses the file
     *
     * @param file Schematic file
     * @return parsed schematic
     * @throws IOException              if the file is invalid
     * @throws NBTException             if the schematic is invalid
     * @throws IllegalArgumentException if the schematic is neither an MCEdit nor a Sponge schematic
     */
    public static @NotNull CompletableFuture<Schematic> fromFile(@NotNull File file) throws IOException, NBTException, IllegalArgumentException {
        if (!file.exists()) throw new FileNotFoundException("Invalid Schematic: File does not exist");
        return fromStream(new FileInputStream(file));
    }

}
