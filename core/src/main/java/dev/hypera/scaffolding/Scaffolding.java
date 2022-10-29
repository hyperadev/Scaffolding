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
package dev.hypera.scaffolding;

import dev.hypera.scaffolding.schematic.NBTSchematicReader;
import dev.hypera.scaffolding.schematic.Schematic;
import dev.hypera.scaffolding.schematic.readers.MCEditSchematicReader;
import dev.hypera.scaffolding.schematic.readers.SpongeSchematicReader;
import org.jetbrains.annotations.NotNull;
import org.jglrxavpok.hephaistos.nbt.CompressedProcesser;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;
import org.jglrxavpok.hephaistos.nbt.NBTException;
import org.jglrxavpok.hephaistos.nbt.NBTReader;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

/**
 * A static utility class primarily used to parse schematics.
 */
@SuppressWarnings("unused")
public final class Scaffolding {

    private static final @NotNull NBTSchematicReader MC_EDIT_SCHEMATIC_READER = new MCEditSchematicReader();
    private static final @NotNull NBTSchematicReader SPONGE_SCHEMATIC_READER = new SpongeSchematicReader();

    private Scaffolding() {
    }


    /**
     * Automatically detects the type of schematic and parses the file.
     *
     * @param path Schematic path
     * @return parsed schematic
     * @throws IOException              if the file is invalid
     * @throws NBTException             if the schematic is invalid
     * @throws IllegalArgumentException if the schematic is neither an MCEdit nor a Sponge schematic
     */
    public static @NotNull CompletableFuture<Schematic> fromPath(@NotNull Path path) throws IOException, NBTException, IllegalArgumentException {
        return fromPath(path, new Schematic());
    }

    /**
     * @param path      the {@link Path} to read from
     * @param schematic the {@link Schematic} to load the data into
     * @return a {@link CompletableFuture<Schematic>} that will be completed when the schematic is loaded
     * @throws IOException  if the file is invalid
     * @throws NBTException if the NBT tag is invalid
     */
    public static @NotNull CompletableFuture<Schematic> fromPath(@NotNull Path path, @NotNull Schematic schematic) throws IOException, NBTException {
        if (!Files.exists(path)) throw new FileNotFoundException("Invalid Schematic: File does not exist");
        return fromStream(Files.newInputStream(path), schematic);
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
    public static @NotNull CompletableFuture<Schematic> fromFile(@NotNull File file) throws IOException, NBTException, IllegalArgumentException {
        return fromFile(file, new Schematic());
    }

    /**
     * @param file      the {@link File} to read from
     * @param schematic the {@link Schematic} to load the data into
     * @return a {@link CompletableFuture<Schematic>} that will be completed when the schematic is loaded
     * @throws IOException  if the file is invalid
     * @throws NBTException if the NBT tag is invalid
     */
    public static @NotNull CompletableFuture<Schematic> fromFile(@NotNull File file, @NotNull Schematic schematic) throws IOException, NBTException {
        if (!file.exists()) throw new FileNotFoundException("Invalid Schematic: File does not exist");
        return fromStream(new FileInputStream(file), schematic);
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
    public static @NotNull CompletableFuture<Schematic> fromStream(@NotNull InputStream inputStream) throws IOException, NBTException, IllegalArgumentException {
        return fromStream(inputStream, new Schematic());
    }

    /**
     * @param inputStream the {@link InputStream} to read from
     * @param schematic   the {@link Schematic} to load the data into
     * @return a {@link CompletableFuture<Schematic>} that will be completed when the schematic is loaded
     * @throws IOException  if the input stream is invalid
     * @throws NBTException if the NBT tag is invalid
     */
    public static @NotNull CompletableFuture<Schematic> fromStream(@NotNull InputStream inputStream, @NotNull Schematic schematic) throws IOException, NBTException {
        return fromNBT((NBTCompound) new NBTReader(inputStream, CompressedProcesser.GZIP).read(), schematic);
    }


    /**
     * Automatically detects the schematic format from the provided {@link NBTCompound} and parses it.
     *
     * @param nbtTag The {@link NBTCompound} to read from
     * @return A {@link CompletableFuture<Schematic>} that will complete with the schematic once it's loaded
     * @throws NBTException If the NBT tag is invalid
     */
    public static @NotNull CompletableFuture<Schematic> fromNBT(@NotNull NBTCompound nbtTag) throws NBTException, IllegalArgumentException {
        return fromNBT(nbtTag, new Schematic());
    }

    /**
     * @param nbtTag    The NBT tag to parse
     * @param schematic The {@link Schematic} to load the data into
     * @return a {@link CompletableFuture<Schematic>} that will be completed when the schematic is loaded
     * @throws NBTException if the NBT tag is invalid
     */
    public static @NotNull CompletableFuture<Schematic> fromNBT(@NotNull NBTCompound nbtTag, @NotNull Schematic schematic) throws NBTException {
        if (MC_EDIT_SCHEMATIC_READER.isReadable(nbtTag)) {
            return MC_EDIT_SCHEMATIC_READER.read(nbtTag, schematic);
        } else if (SPONGE_SCHEMATIC_READER.isReadable(nbtTag)) {
            return SPONGE_SCHEMATIC_READER.read(nbtTag, schematic);
        } else {
            throw new IllegalArgumentException("Unknown schematic type");
        }
    }


    /**
     * @param path The {@link Path} to read from
     * @return The parsed {@link Schematic}
     * @throws IOException              if the file is invalid
     * @throws NBTException             if the schematic is invalid
     * @throws IllegalArgumentException if the schematic is neither an MCEdit nor a Sponge schematic
     */
    public static @NotNull Schematic fromPathSync(@NotNull Path path) throws IOException, NBTException, IllegalArgumentException {
        return fromPath(path).join();
    }

    /**
     * @param path      the {@link Path} to read from
     * @param schematic the {@link Schematic} to load the data into
     * @return the parsed {@link Schematic}
     * @throws IOException  if the file is invalid
     * @throws NBTException if the NBT tag is invalid
     */
    public static @NotNull Schematic fromPathSync(@NotNull Path path, @NotNull Schematic schematic) throws IOException, NBTException {
        return fromPath(path, schematic).join();
    }


    /**
     * @param file The {@link File} to read from
     * @return The parsed {@link Schematic}
     * @throws IOException              if the file is invalid
     * @throws NBTException             if the schematic is invalid
     * @throws IllegalArgumentException if the schematic is neither an MCEdit nor a Sponge schematic
     */
    public static @NotNull Schematic fromFileSync(@NotNull File file) throws IOException, NBTException, IllegalArgumentException {
        return fromFile(file).join();
    }

    /**
     * @param file      the {@link File} to read from
     * @param schematic the {@link Schematic} to load the data into
     * @return the parsed {@link Schematic}
     * @throws IOException  if the file is invalid
     * @throws NBTException if the NBT tag is invalid
     */
    public static @NotNull Schematic fromFileSync(@NotNull File file, @NotNull Schematic schematic) throws IOException, NBTException {
        return fromFile(file, schematic).join();
    }


    /**
     * Automatically detects the schematic format from the provided {@link NBTCompound} and parses it synchronously.
     *
     * @param nbtTag the {@link NBTCompound} to read from
     * @return the parsed {@link Schematic}
     * @throws NBTException             if the NBT tag is invalid
     * @throws IllegalArgumentException if the schematic is neither an MCEdit nor a Sponge schematic
     */
    public static @NotNull Schematic fromNBTSync(@NotNull NBTCompound nbtTag) throws NBTException, IllegalArgumentException {
        return fromNBT(nbtTag).join();
    }

    /**
     * @param nbtTag    The NBT tag to parse
     * @param schematic The {@link Schematic} to load the data into
     * @return the parsed {@link Schematic}
     * @throws NBTException if the NBT tag is invalid
     */
    public static @NotNull Schematic fromNBTSync(@NotNull NBTCompound nbtTag, @NotNull Schematic schematic) throws NBTException {
        return fromNBT(nbtTag, schematic).join();
    }


    /**
     * @param inputStream The {@link InputStream} to read from
     * @return The parsed {@link Schematic}
     * @throws IOException              if the input stream is invalid
     * @throws NBTException             if the schematic is invalid
     * @throws IllegalArgumentException if the schematic is neither an MCEdit nor a Sponge schematic
     */
    public static @NotNull Schematic fromStreamSync(@NotNull InputStream inputStream) throws IOException, NBTException, IllegalArgumentException {
        return fromStream(inputStream).join();
    }

    /**
     * @param inputStream the {@link InputStream} to read from
     * @param schematic   the {@link Schematic} to load the data into
     * @return the parsed {@link Schematic}
     * @throws IOException  if the input stream is invalid
     * @throws NBTException if the NBT tag is invalid
     */
    public static @NotNull Schematic fromStreamSync(@NotNull InputStream inputStream, @NotNull Schematic schematic) throws IOException, NBTException {
        return fromStream(inputStream, schematic).join();
    }

}
