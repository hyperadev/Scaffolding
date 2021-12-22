package net.crystalgames.scaffolding.schematic;

import org.jglrxavpok.hephaistos.nbt.CompressedProcesser;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;
import org.jglrxavpok.hephaistos.nbt.NBTException;
import org.jglrxavpok.hephaistos.nbt.NBTReader;

import java.io.IOException;
import java.io.InputStream;

public abstract class AbstractSchematic implements Schematic {

    protected NBTCompound nbtTag;

    public AbstractSchematic(InputStream inputStream) throws IOException, NBTException {
        this(((NBTCompound) new NBTReader(inputStream, CompressedProcesser.GZIP).readNamed().getSecond()));
    }

    public AbstractSchematic(NBTCompound nbtTag) {
        this.nbtTag = nbtTag;
    }

}
