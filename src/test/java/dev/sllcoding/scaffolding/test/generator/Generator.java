package dev.sllcoding.scaffolding.test.generator;

import net.minestom.server.instance.ChunkGenerator;
import net.minestom.server.instance.ChunkPopulator;
import net.minestom.server.instance.batch.ChunkBatch;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class Generator implements ChunkGenerator {

    @Override
    public void generateChunkData(@NotNull ChunkBatch chunkBatch, int chunkX, int chunkZ) {
        for (int x = 0; x < 16; x++)
            for (int z = 0; z < 16; z++)
                for (int y = 0; y < 40; y++)
                    chunkBatch.setBlock(x, y, z, Block.STONE);
    }

    @Override
    public @Nullable List<ChunkPopulator> getPopulators() {
        return null;
    }

}
