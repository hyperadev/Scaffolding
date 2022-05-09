package net.crystalgames.scaffolding.schematic;

import org.jetbrains.annotations.NotNull;
import org.jglrxavpok.hephaistos.collections.ImmutableByteArray;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;
import org.jglrxavpok.hephaistos.nbt.NBTException;

import java.util.concurrent.CompletableFuture;

public abstract class NBTSchematicReader {

    public abstract CompletableFuture<Schematic> read(@NotNull Schematic schematic, @NotNull NBTCompound compound) throws NBTException;

    protected int getInteger(@NotNull NBTCompound compound, @NotNull String key, String exceptionMessage) throws NBTException {
        Integer value = compound.getInt(key);
        if (value == null) throw new NBTException(exceptionMessage);

        return value;
    }

    protected short getShort(@NotNull NBTCompound compound, @NotNull String key, String exceptionMessage) throws NBTException {
        Short value = compound.getShort(key);
        if (value == null) throw new NBTException(exceptionMessage);

        return value;
    }

    protected NBTCompound getCompound(@NotNull NBTCompound compound, @NotNull String key, String exceptionMessage) throws NBTException {
        NBTCompound value = compound.getCompound(key);
        if (value == null) throw new NBTException(exceptionMessage);

        return value;
    }

    protected byte getByte(@NotNull NBTCompound compound, @NotNull String key, String exceptionMessage) throws NBTException {
        Byte value = compound.getByte(key);
        if (value == null) throw new NBTException(exceptionMessage);

        return value;
    }

    protected byte[] getByteArray(@NotNull NBTCompound compound, @NotNull String key, String exceptionMessage) throws NBTException {
        ImmutableByteArray value = compound.getByteArray(key);
        if(value == null) throw new NBTException(exceptionMessage);

        return value.copyArray();
    }

    protected boolean getBoolean(@NotNull NBTCompound compound, @NotNull String key, String exceptionMessage) throws NBTException {
        Boolean value = compound.getBoolean(key);
        if (value == null) throw new NBTException(exceptionMessage);

        return value;
    }

    protected String getString(@NotNull NBTCompound compound, @NotNull String key, String exceptionMessage) throws NBTException {
        String value = compound.getString(key);
        if (value == null) throw new NBTException(exceptionMessage);

        return value;
    }
}
