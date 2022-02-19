package net.crystalgames.scaffolding.schematic;

import net.crystalgames.scaffolding.region.Region;
import net.minestom.server.coordinate.Pos;
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

    default void read(InputStream inputStream) throws IOException, NBTException {
        NBTReader reader = new NBTReader(inputStream, CompressedProcesser.GZIP);
        read((NBTCompound) reader.readNamed().getSecond());
        reader.close();
        inputStream.close();
    }
    void read(NBTCompound nbtTag) throws NBTException;

    void write(OutputStream outputStream, Region region) throws IOException;
    CompletableFuture<Region> build(Instance instance, Pos position);

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
