package net.crystalgames.scaffolding.schematic;

import org.jetbrains.annotations.NotNull;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;
import org.jglrxavpok.hephaistos.nbt.NBTException;

import java.util.concurrent.CompletableFuture;

public interface NBTSchematicReader {

    CompletableFuture<Schematic> read(@NotNull Schematic schematic, @NotNull NBTCompound compound) throws NBTException;
}
