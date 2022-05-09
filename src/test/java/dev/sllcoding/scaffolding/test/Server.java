package dev.sllcoding.scaffolding.test;

import dev.sllcoding.scaffolding.test.commands.TestCommand;
import me.alexpanov.net.FreePortFinder;
import net.crystalgames.scaffolding.Scaffolding;
import net.crystalgames.scaffolding.schematic.Schematic;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.GameMode;
import net.minestom.server.event.player.PlayerChatEvent;
import net.minestom.server.event.player.PlayerLoginEvent;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.extras.lan.OpenToLAN;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.block.Block;
import net.minestom.server.utils.NamespaceID;
import net.minestom.server.world.DimensionType;
import org.jglrxavpok.hephaistos.nbt.NBTException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Objects;

public class Server {

    private static final Logger LOGGER = LoggerFactory.getLogger(Server.class);

    private static final DimensionType FULL_BRIGHT_DIMENSION = DimensionType.builder(NamespaceID.from("scaffolding:full_bright"))
            .ambientLight(2.0f)
            .build();

    public static void main(String[] args) throws IOException, NBTException {
        MinecraftServer server = MinecraftServer.init();

        MinecraftServer.getDimensionTypeManager().addDimension(FULL_BRIGHT_DIMENSION);
        InstanceContainer instance = MinecraftServer.getInstanceManager().createInstanceContainer(FULL_BRIGHT_DIMENSION);
        instance.setGenerator((unit -> unit.modifier().fillHeight(0, 40, Block.SMOOTH_QUARTZ)));
        // Load schematic for schematic chunk loader
//        try {
//            Schematic schematic = Scaffolding.fromStream(Objects.requireNonNull(Schematic.class.getClassLoader().getResourceAsStream("_.schematic")));
//
//            SchematicChunkLoader chunkLoader = SchematicChunkLoader.builder()
//                    .addSchematic(schematic)
//                    .build();
//            instance.setChunkLoader(chunkLoader);
//        } catch (IOException | NBTException e) {
//            e.printStackTrace();
//        }

        Schematic schematic = Scaffolding.fromStream(Objects.requireNonNull(Schematic.class.getClassLoader().getResourceAsStream("_.schematic")));

        System.out.println(schematic.getWidth() + " " + schematic.getHeight() + " " + schematic.getLength());
        System.out.println(schematic.getOffsetX() + " " + schematic.getOffsetY() + " " + schematic.getOffsetZ());

        MinecraftServer.getCommandManager().register(new TestCommand());

        MinecraftServer.getGlobalEventHandler().addListener(PlayerLoginEvent.class, event -> {
                    event.setSpawningInstance(instance);
                    event.getPlayer().setRespawnPoint(new Pos(0, 42, 0));
                }).addListener(PlayerSpawnEvent.class, event -> event.getPlayer().setGameMode(GameMode.CREATIVE))
                .addListener(PlayerChatEvent.class, event -> schematic.build(Objects.requireNonNull(event.getPlayer().getInstance()), event.getPlayer().getPosition()));

        int port = FreePortFinder.findFreeLocalPort(25565 + 1);
        LOGGER.info("Starting server on port {}", port);
        server.start("0.0.0.0", port);
        OpenToLAN.open();
    }
}
