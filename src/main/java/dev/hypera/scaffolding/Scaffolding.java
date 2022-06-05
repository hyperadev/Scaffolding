/*
 * Scaffolding - Schematic library for Minestom
 *  Copyright (c) 2022 SLLCoding <luisjk266@gmail.com>
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

import java.nio.file.Files;
import java.nio.file.Path;
import kotlin.Pair;
import dev.hypera.scaffolding.schematic.Schematic;
import dev.hypera.scaffolding.schematic.impl.MCEditSchematic;
import dev.hypera.scaffolding.schematic.impl.SpongeSchematic;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jglrxavpok.hephaistos.nbt.*;

import java.io.*;

public class Scaffolding {

    /**
     * Automatically detects the type of schematic and parses the input stream
     * @param inputStream Schematic input
     * @return parsed schematic
     * @throws IOException if the input stream is invalid
     * @throws NBTException if the schematic is invalid
     */
    public static @Nullable Schematic fromStream(@NotNull InputStream inputStream) throws IOException, NBTException {
        NBTReader reader = new NBTReader(inputStream, CompressedProcesser.GZIP);
        Pair<String, NBT> pair = reader.readNamed();
        NBTCompound nbtTag = (NBTCompound) pair.getSecond();

        Schematic schematic = null;
        if (nbtTag.contains("Blocks")) schematic = new MCEditSchematic();
        else if (nbtTag.contains("Palette")) schematic = new SpongeSchematic();

        if (schematic != null) schematic.read(nbtTag);
        return schematic;
    }

    /**
     * Automatically detects the type of schematic and parses the file
     * @param path Schematic path
     * @return parsed schematic
     * @throws IOException if the file is invalid
     * @throws NBTException if the schematic is invalid
     */
    public static @Nullable Schematic fromPath(@NotNull Path path) throws IOException, NBTException {
        if (!Files.exists(path)) throw new FileNotFoundException("Invalid Schematic: File does not exist");
        return fromStream(Files.newInputStream(path));
    }

    /**
     * Automatically detects the type of schematic and parses the file
     * @param file Schematic file
     * @return parsed schematic
     * @throws IOException if the file is invalid
     * @throws NBTException if the schematic is invalid
     */
    public static @Nullable Schematic fromFile(@NotNull File file) throws IOException, NBTException {
        if (!file.exists()) throw new FileNotFoundException("Invalid Schematic: File does not exist");
        return fromStream(new FileInputStream(file));
    }

}
