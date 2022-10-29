/*
 * Scaffolding - Schematic library for Minestom
 *  Copyright (c) 2022-latest The Scaffolding Library Authors
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the “Software”), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in
 *  all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  THE SOFTWARE.
 */
package dev.hypera.scaffolding.editor;

import dev.hypera.scaffolding.editor.commands.CopyCommand;
import dev.hypera.scaffolding.editor.commands.LoadCommand;
import dev.hypera.scaffolding.editor.commands.PasteCommand;
import dev.hypera.scaffolding.editor.features.SelectionFeature;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
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
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.utils.NamespaceID;
import net.minestom.server.world.DimensionType;

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
        globalEventHandler.addListener(PlayerSpawnEvent.class, event -> {
            Player player = event.getPlayer();

            ItemStack wand = ItemStack.builder(Material.WOODEN_AXE)
                    .amount(1)
                    .displayName(Component.text("Selection Tool", NamedTextColor.GOLD))
                    .lore(
                            Component.text("Use this to edit the world.", NamedTextColor.GRAY), Component.empty(),
                            Clipboard.FIRST_POINT_COMPONENT.append(Component.text(" - left click", NamedTextColor.GRAY)),
                            Clipboard.SECOND_POINT_COMPONENT.append(Component.text(" - right click", NamedTextColor.GRAY))
                    )
                    .build();
            player.getInventory().addItemStack(wand);

            player.setGameMode(GameMode.CREATIVE);
        });

        new SelectionFeature().hook(instance.eventNode());

        server.start("0.0.0.0", 25565);
        OpenToLAN.open();
    }

    public static Clipboard getClipboard(Player player) {
        return clipboards.get(player);
    }
}
