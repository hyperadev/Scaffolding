package net.crystalgames.scaffolding.schematic;

import net.crystalgames.scaffolding.region.Region;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import org.jglrxavpok.hephaistos.nbt.NBTException;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public interface Schematic {

    void read() throws IOException, NBTException;
    void write(Region region) throws IOException;
    CompletableFuture<Region> build(Instance instance, Pos position);

}
