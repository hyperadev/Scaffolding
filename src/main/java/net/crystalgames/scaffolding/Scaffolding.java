package net.crystalgames.scaffolding;

import net.crystalgames.scaffolding.schematic.NBTSchematicReader;
import net.crystalgames.scaffolding.schematic.Schematic;
import net.crystalgames.scaffolding.schematic.readers.MCEditSchematicReader;
import net.crystalgames.scaffolding.schematic.readers.SpongeSchematicReader;
import org.jetbrains.annotations.NotNull;
import org.jglrxavpok.hephaistos.nbt.CompressedProcesser;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;
import org.jglrxavpok.hephaistos.nbt.NBTException;
import org.jglrxavpok.hephaistos.nbt.NBTReader;

import java.io.*;
import java.util.concurrent.CompletableFuture;

/**
 * A static utility class primarily used to parse Schematics.
 */
public final class Scaffolding {

    private static final NBTSchematicReader MC_EDIT_SCHEMATIC_READER = new MCEditSchematicReader();
    private static final NBTSchematicReader SPONGE_SCHEMATIC_READER = new SpongeSchematicReader();

    private Scaffolding() {
        throw new UnsupportedOperationException();
    }

    /**
     * Automatically detects the schematic format from the provided {@link NBTCompound} and parses it.
     *
     * @param nbtTag The NBT tag to read from
     * @return A {@link CompletableFuture<Schematic>} that will complete with the schematic once it's loaded
     * @throws NBTException If the NBT tag is invalid
     */
    public static @NotNull CompletableFuture<Schematic> fromNbt(@NotNull final NBTCompound nbtTag) throws NBTException {
        final Schematic schematic = new Schematic();

        if (nbtTag.contains("Blocks")) return MC_EDIT_SCHEMATIC_READER.read(nbtTag, schematic);
        else if (nbtTag.contains("Palette")) return SPONGE_SCHEMATIC_READER.read(nbtTag, schematic);
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
    public static @NotNull CompletableFuture<Schematic> fromFile(@NotNull final File file) throws IOException, NBTException, IllegalArgumentException {
        if (!file.exists()) throw new FileNotFoundException("Invalid Schematic: File does not exist");
        return fromStream(new FileInputStream(file));
    }

    /**
     * Automatically detects the type of schematic and parses the input stream
     *
     * @param inputStream Schematic input
     * @return a {@link CompletableFuture<Schematic>} that will contain the schematic once loaded
     * @throws IOException              if the input stream is invalid
     * @throws NBTException             if the schematic is invalid
     * @throws IllegalArgumentException if the schematic is neither an MCEdit nor a Sponge schematic
     */
    public static @NotNull CompletableFuture<Schematic> fromStream(@NotNull final InputStream inputStream) throws IOException, NBTException, IllegalArgumentException {
        final NBTReader reader = new NBTReader(inputStream, CompressedProcesser.GZIP);
        final NBTCompound nbtTag = (NBTCompound) reader.read();

        return fromNbt(nbtTag);
    }

}
