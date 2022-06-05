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
