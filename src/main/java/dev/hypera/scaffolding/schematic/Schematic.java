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
package dev.hypera.scaffolding.schematic;

import dev.hypera.scaffolding.region.Region;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jglrxavpok.hephaistos.nbt.CompressedProcesser;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;
import org.jglrxavpok.hephaistos.nbt.NBTException;
import org.jglrxavpok.hephaistos.nbt.NBTReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.CompletableFuture;

public interface Schematic {

    default void read(@NotNull InputStream inputStream) throws IOException, NBTException {
        NBTReader reader = new NBTReader(inputStream, CompressedProcesser.GZIP);
        read((NBTCompound) reader.readNamed().getSecond());
        reader.close();
        inputStream.close();
    }
    void read(@NotNull NBTCompound nbtTag) throws NBTException;
    void write(@NotNull OutputStream outputStream, @NotNull Region region) throws IOException;

    CompletableFuture<Region> build(Instance instance, Point position);

    short getWidth();
    short getHeight();
    short getLength();

    int getOffsetX();
    int getOffsetY();
    int getOffsetZ();

    /**
     * Applies the schematic to the given block setter.
     * @param setter the block setter
     */
    void apply(@NotNull Block.Setter setter);

}
