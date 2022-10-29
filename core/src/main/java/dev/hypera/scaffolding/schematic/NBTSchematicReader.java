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

import org.jetbrains.annotations.NotNull;
import org.jglrxavpok.hephaistos.collections.ImmutableByteArray;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;
import org.jglrxavpok.hephaistos.nbt.NBTException;

import java.util.concurrent.CompletableFuture;

/**
 * A parser for schematics that uses NBT to store data.
 */
public abstract class NBTSchematicReader {

    /**
     * Checks if the provided NBT tag can be read by this reader.
     *
     * @param compound The {@link NBTCompound} to check
     * @return whether this reader can read the provided tag.
     */
    public abstract boolean isReadable(@NotNull NBTCompound compound);

    /**
     * Parses data from the provided NBT tag and stores it in the provided schematic.
     *
     * @param compound  The {@link NBTCompound} to read from
     * @param schematic The {@link Schematic} to read into
     * @return a {@link CompletableFuture<Schematic>} that will be completed with the {@link Schematic}
     * @throws NBTException If the provided NBT tag is invalid
     */
    public abstract CompletableFuture<Schematic> read(@NotNull NBTCompound compound, @NotNull Schematic schematic) throws NBTException;

    /**
     * @param compound         The {@link NBTCompound} to read from
     * @param key              The key to look for
     * @param exceptionMessage The exception message to throw if the key is not found
     * @return The value of the key
     * @throws NBTException If the provided NBT tag is invalid
     */
    protected final int getInteger(@NotNull NBTCompound compound, @NotNull String key, String exceptionMessage) throws NBTException {
        Integer value = compound.getInt(key);
        if (value == null) {
            throw new NBTException(exceptionMessage);
        }

        return value;
    }

    /**
     * @param compound         The {@link NBTCompound} to read from
     * @param key              The key to look for
     * @param exceptionMessage The exception message to throw if the key is not found
     * @return The value of the key
     * @throws NBTException If the provided NBT tag is invalid
     */
    protected final short getShort(@NotNull NBTCompound compound, @NotNull String key, String exceptionMessage) throws NBTException {
        Short value = compound.getShort(key);
        if (value == null) {
            throw new NBTException(exceptionMessage);
        }

        return value;
    }

    /**
     * @param compound         The {@link NBTCompound} to read from
     * @param key              The key to look for
     * @param exceptionMessage The exception message to throw if the key is not found
     * @return The value of the key
     * @throws NBTException If the provided NBT tag is invalid
     */
    protected final NBTCompound getCompound(@NotNull NBTCompound compound, @NotNull String key, String exceptionMessage) throws NBTException {
        NBTCompound value = compound.getCompound(key);
        if (value == null) {
            throw new NBTException(exceptionMessage);
        }

        return value;
    }

    /**
     * @param compound         The {@link NBTCompound} to read from
     * @param key              The key to look for
     * @param exceptionMessage The exception message to throw if the key is not found
     * @return The value of the key
     * @throws NBTException If the provided NBT tag is invalid
     */
    protected final byte getByte(@NotNull NBTCompound compound, @NotNull String key, String exceptionMessage) throws NBTException {
        Byte value = compound.getByte(key);
        if (value == null) {
            throw new NBTException(exceptionMessage);
        }

        return value;
    }

    /**
     * @param compound         The {@link NBTCompound} to read from
     * @param key              The key to look for
     * @param exceptionMessage The exception message to throw if the key is not found
     * @return The value of the key
     * @throws NBTException If the provided NBT tag is invalid
     */
    protected final byte[] getByteArray(@NotNull NBTCompound compound, @NotNull String key, String exceptionMessage) throws NBTException {
        ImmutableByteArray value = compound.getByteArray(key);
        if (value == null) {
            throw new NBTException(exceptionMessage);
        }

        return value.copyArray();
    }

    /**
     * @param compound         The {@link NBTCompound} to read from
     * @param key              The key to look for
     * @param exceptionMessage The exception message to throw if the key is not found
     * @return The value of the key
     * @throws NBTException If the provided NBT tag is invalid
     */
    protected final boolean getBoolean(@NotNull NBTCompound compound, @NotNull String key, String exceptionMessage) throws NBTException {
        Boolean value = compound.getBoolean(key);
        if (value == null) {
            throw new NBTException(exceptionMessage);
        }

        return value;
    }

    /**
     * @param compound         The {@link NBTCompound} to read from
     * @param key              The key to look for
     * @param exceptionMessage The exception message to throw if the key is not found
     * @return The value of the key
     * @throws NBTException If the provided NBT tag is invalid
     */
    protected final String getString(@NotNull NBTCompound compound, @NotNull String key, String exceptionMessage) throws NBTException {
        String value = compound.getString(key);
        if (value == null) {
            throw new NBTException(exceptionMessage);
        }

        return value;
    }

}
