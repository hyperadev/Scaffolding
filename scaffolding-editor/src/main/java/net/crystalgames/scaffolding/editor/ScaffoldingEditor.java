package net.crystalgames.scaffolding.editor;

import me.alexpanov.net.FreePortFinder;
import net.crystalgames.scaffolding.Scaffolding;
import net.crystalgames.scaffolding.editor.commands.CopyCommand;
import net.crystalgames.scaffolding.editor.commands.LoadCommand;
import net.crystalgames.scaffolding.editor.commands.PasteCommand;
import net.crystalgames.scaffolding.editor.features.SelectionFeature;
import net.crystalgames.scaffolding.schematic.Schematic;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandManager;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.player.PlayerDisconnectEvent;
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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

public class ScaffoldingEditor {

    public static final Path SCHEMATICS_PATH = Paths.get("schematics");
    public static final DimensionType FULL_BRIGHT_DIMENSION = DimensionType.builder(NamespaceID.from("scaffolding_editor:full_bright"))
            .ambientLight(2.0f)
            .build();
    public static final HashMap<Player, Clipboard> clipboards = new HashMap<>();
    private static final Logger LOGGER = LoggerFactory.getLogger(ScaffoldingEditor.class);

    public static void main(String[] args) throws IOException {
        if (!Files.isDirectory(SCHEMATICS_PATH)) Files.createDirectory(SCHEMATICS_PATH);

        MinecraftServer server = MinecraftServer.init();

        MinecraftServer.getDimensionTypeManager().addDimension(FULL_BRIGHT_DIMENSION);
        InstanceContainer instance = MinecraftServer.getInstanceManager().createInstanceContainer(FULL_BRIGHT_DIMENSION);
        instance.setGenerator((unit -> unit.modifier().fillHeight(0, 6, Block.SMOOTH_QUARTZ)));

        CommandManager commandManager = MinecraftServer.getCommandManager();
        commandManager.register(new LoadCommand());
        commandManager.register(new CopyCommand());
        commandManager.register(new PasteCommand());

        GlobalEventHandler globalEventHandler = MinecraftServer.getGlobalEventHandler();
        globalEventHandler.addListener(PlayerLoginEvent.class, event -> {
            Player player = event.getPlayer();

            clipboards.put(player, new Clipboard(player));
            player.setRespawnPoint(new Pos(0, 6, 0));
            event.setSpawningInstance(instance);
        });
        globalEventHandler.addListener(PlayerDisconnectEvent.class, event -> clipboards.remove(event.getPlayer()).cleanup());
        globalEventHandler.addListener(PlayerSpawnEvent.class, event -> event.getPlayer().setGameMode(GameMode.CREATIVE));

        new SelectionFeature().hook(instance.eventNode());

        int port = FreePortFinder.findFreeLocalPort(25565 + 1);
        LOGGER.info("Starting server on port {}", port);
        server.start("0.0.0.0", port);
        OpenToLAN.open();
    }

    public static Clipboard getClipboard(Player player) {
        return clipboards.get(player);
    }
}
