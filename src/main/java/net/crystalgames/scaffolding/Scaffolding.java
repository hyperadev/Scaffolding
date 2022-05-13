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
 * A static utility class primarily used to parse schematics.
 */
@SuppressWarnings("unused")
public final class Scaffolding {

    private static final NBTSchematicReader MC_EDIT_SCHEMATIC_READER = new MCEditSchematicReader();
    private static final NBTSchematicReader SPONGE_SCHEMATIC_READER = new SpongeSchematicReader();

    private Scaffolding() {
        throw new UnsupportedOperationException();
    }

    public static CompletableFuture<Schematic> fromNbt(@NotNull NBTCompound nbtTag, @NotNull Schematic schematic) throws NBTException {
        if (nbtTag.contains("Blocks")) return MC_EDIT_SCHEMATIC_READER.read(nbtTag, schematic);
        else if (nbtTag.contains("Palette")) return SPONGE_SCHEMATIC_READER.read(nbtTag, schematic);
        else throw new IllegalArgumentException("Unknown schematic type.");
    }

    public static CompletableFuture<Schematic> fromStream(@NotNull InputStream inputStream, @NotNull Schematic schematic) throws IOException, NBTException {
        final NBTReader reader = new NBTReader(inputStream, CompressedProcesser.GZIP);
        final NBTCompound nbtTag = (NBTCompound) reader.read();

        return fromNbt(nbtTag, schematic);
    }

    public static CompletableFuture<Schematic> fromFile(@NotNull File file, @NotNull Schematic schematic) throws IOException, NBTException {
        if (!file.exists()) throw new FileNotFoundException("Invalid Schematic: File does not exist");
        return fromStream(new FileInputStream(file), schematic);
    }

    /**
     * Automatically detects the schematic format from the provided {@link NBTCompound} and parses it.
     *
     * @param nbtTag The {@link NBTCompound} to read from
     * @return A {@link CompletableFuture<Schematic>} that will complete with the schematic once it's loaded
     * @throws NBTException If the NBT tag is invalid
     */
    public static @NotNull CompletableFuture<Schematic> fromNbt(@NotNull final NBTCompound nbtTag) throws NBTException, IllegalArgumentException {
        return fromNbt(nbtTag, new Schematic());
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
        return fromStream(inputStream, new Schematic());
    }


    /**
     * Automatically detects the type of schematic and parses the file.
     *
     * @param file Schematic file
     * @return parsed schematic
     * @throws IOException              if the file is invalid
     * @throws NBTException             if the schematic is invalid
     * @throws IllegalArgumentException if the schematic is neither an MCEdit nor a Sponge schematic
     */
    public static @NotNull CompletableFuture<Schematic> fromFile(@NotNull final File file) throws IOException, NBTException, IllegalArgumentException {
        return fromFile(file, new Schematic());
    }

    public static @NotNull Schematic fromNbtSync(@NotNull NBTCompound nbtTag, @NotNull Schematic schematic) throws NBTException {
        return fromNbt(nbtTag, schematic).join();
    }

    public static @NotNull Schematic fromStreamSync(@NotNull InputStream inputStream, @NotNull Schematic schematic) throws IOException, NBTException {
        return fromStream(inputStream, schematic).join();
    }

    public static @NotNull Schematic fromFileSync(@NotNull File file, @NotNull Schematic schematic) throws IOException, NBTException {
        return fromFile(file, schematic).join();
    }

    /**
     * Automatically detects the schematic format from the provided {@link NBTCompound} and parses it synchronously.
     *
     * @param nbtTag the {@link NBTCompound} to read from
     * @return the parsed {@link Schematic}
     * @throws NBTException if the NBT tag is invalid
     * @throws IllegalArgumentException if the schematic is neither an MCEdit nor a Sponge schematic
     */
    public static @NotNull Schematic fromNbtSync(@NotNull final NBTCompound nbtTag) throws NBTException, IllegalArgumentException {
        return fromNbt(nbtTag).join();
    }

    /**
     * @param inputStream The {@link InputStream} to read from
     * @return The parsed {@link Schematic}
     * @throws IOException            if the input stream is invalid
     * @throws NBTException           if the schematic is invalid
     * @throws IllegalArgumentException if the schematic is neither an MCEdit nor a Sponge schematic
     */
    public static @NotNull Schematic fromStreamSync(@NotNull final InputStream inputStream) throws IOException, NBTException, IllegalArgumentException {
        return fromStream(inputStream).join();
    }

    /**
     * @param file The {@link File} to read from
     * @return The parsed {@link Schematic}
     * @throws IOException             if the file is invalid
     * @throws NBTException            if the schematic is invalid
     * @throws IllegalArgumentException if the schematic is neither an MCEdit nor a Sponge schematic
     */
    public static @NotNull Schematic fromFileSync(@NotNull final File file) throws IOException, NBTException, IllegalArgumentException {
        return fromFile(file).join();
    }
}
