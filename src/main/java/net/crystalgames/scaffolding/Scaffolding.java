package net.crystalgames.scaffolding;

import kotlin.Pair;
import net.crystalgames.scaffolding.schematic.Schematic;
import net.crystalgames.scaffolding.schematic.impl.MCEditSchematic;
import net.crystalgames.scaffolding.schematic.impl.SpongeSchematic;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jglrxavpok.hephaistos.nbt.*;

import java.io.*;

public class Scaffolding {

    /**
     * Automatically detects the type of schematic and parses the input stream
     *
     * @param inputStream Schematic input
     * @return parsed schematic
     * @throws IOException  if the input stream is invalid
     * @throws NBTException if the schematic is invalid
     * @throws IllegalArgumentException if the schematic is neither an MCEdit nor a Sponge schematic
     */
    public static @NotNull Schematic fromStream(@NotNull InputStream inputStream) throws IOException, NBTException, IllegalArgumentException {
        NBTReader reader = new NBTReader(inputStream, CompressedProcesser.GZIP);
        Pair<String, NBT> pair = reader.readNamed();
        NBTCompound nbtTag = (NBTCompound) pair.getSecond();

        Schematic schematic;
        if (nbtTag.contains("Blocks")) schematic = new MCEditSchematic();
        else if (nbtTag.contains("Palette")) schematic = new SpongeSchematic();
        else throw new IllegalArgumentException("Unknown schematic type.");

        schematic.read(nbtTag);
        return schematic;
    }

    /**
     * Automatically detects the type of schematic and parses the file
     *
     * @param file Schematic file
     * @return parsed schematic
     * @throws IOException  if the file is invalid
     * @throws NBTException if the schematic is invalid
     * @throws IllegalArgumentException if the schematic is neither an MCEdit nor a Sponge schematic
     */
    public static @NotNull Schematic fromFile(@NotNull File file) throws IOException, NBTException, IllegalArgumentException {
        if (!file.exists()) throw new FileNotFoundException("Invalid Schematic: File does not exist");
        return fromStream(new FileInputStream(file));
    }

}
