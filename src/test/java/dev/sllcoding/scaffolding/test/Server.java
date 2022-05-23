package dev.sllcoding.scaffolding.test;

import dev.sllcoding.scaffolding.test.commands.TestCommand;
import dev.sllcoding.scaffolding.test.generator.Generator;
import net.crystalgames.scaffolding.Scaffolding;
import net.crystalgames.scaffolding.instance.SchematicChunkLoader;
import net.crystalgames.scaffolding.schematic.Schematic;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.GameMode;
import net.minestom.server.event.player.PlayerLoginEvent;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.utils.NamespaceID;
import net.minestom.server.world.DimensionType;
import org.jglrxavpok.hephaistos.nbt.NBTException;

import java.io.File;
import java.io.IOException;

public class Server {

    private static final DimensionType FULLBRIGHT_DIMENSTION = DimensionType.builder(NamespaceID.from("scaffolding:fullbright"))
            .ambientLight(2.0f)
            .build();

    public static void main(String[] args) {
        MinecraftServer server = MinecraftServer.init();

        MinecraftServer.getDimensionTypeManager().addDimension(FULLBRIGHT_DIMENSTION);
        InstanceContainer instance = MinecraftServer.getInstanceManager().createInstanceContainer(FULLBRIGHT_DIMENSTION);
        instance.setChunkGenerator(new Generator());
        // Load schematic for schematic chunk loader
        try {
            Schematic schematic = Scaffolding.fromFile(new File("schematic.schematic"));
            SchematicChunkLoader chunkLoader = SchematicChunkLoader.builder()
                    .addSchematic(schematic)
                    .build();
            instance.setChunkLoader(chunkLoader);
        } catch (IOException | NBTException e) {
            e.printStackTrace();
        }


        MinecraftServer.getCommandManager().register(new TestCommand());

        MinecraftServer.getGlobalEventHandler().addListener(PlayerLoginEvent.class, event -> {
            event.setSpawningInstance(instance);
            event.getPlayer().setRespawnPoint(new Pos(0, 42, 0));
        }).addListener(PlayerSpawnEvent.class, event -> {
            event.getPlayer().setGameMode(GameMode.CREATIVE);
        });

        server.start("0.0.0.0", 25565);
    }

}